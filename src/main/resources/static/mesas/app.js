const API_URL = '/api/mesas';
const API_VENTAS = '/api/ventas';
let mesaSeleccionadaId = null;
let pedidoActivoId = null;
let pedidoCache = null;
let productosCache = [];
let categoriasCache = [];
let categoriaActual = null;
let cajaAbierta = false;

// Interceptor global para fetch (Redirigir a login si la sesión expira)
const originalFetch = window.fetch;
window.fetch = async function() {
    const response = await originalFetch.apply(this, arguments);
    if (response.url && response.url.includes('/login.html')) {
        window.location.href = '/mesas/login.html';
        return new Promise(() => {}); // never resolves
    }
    return response;
}

async function checkCaja() {
    try {
        const response = await fetch('/api/caja/estado');
        const btnCaja = document.getElementById('btnCaja');
        if (response.status === 200) {
            cajaAbierta = true;
            btnCaja.classList.replace('btn-outline-warning', 'btn-success');
            btnCaja.innerHTML = '<i data-lucide="unlock" class="w-4 h-4"></i> Caja Abierta';
        } else {
            cajaAbierta = false;
            btnCaja.classList.replace('btn-success', 'btn-outline-warning');
            btnCaja.innerHTML = '<i data-lucide="lock" class="w-4 h-4"></i> Caja Cerrada';
        }
        lucide.createIcons();
    } catch (e) {
        console.error('Error comprobando caja:', e);
    }
}

async function toggleCaja() {
    if (cajaAbierta) {
        const monto = prompt('Ingrese el monto final de cierre:');
        if (monto === null) return;
        await fetch('/api/caja/cerrar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ montoFinal: parseFloat(monto) })
        });
    } else {
        const monto = prompt('Ingrese el monto inicial para abrir:');
        if (monto === null) return;
        await fetch('/api/caja/abrir', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ montoInicial: parseFloat(monto) })
        });
    }
    checkCaja();
}

document.addEventListener('DOMContentLoaded', () => {
    cargarMesas();
    cargarProductos();
    checkCaja();

    if (document.getElementById('btnAgregarMesa')) {
        document.getElementById('btnAgregarMesa').addEventListener('click', agregarMesa);
    }
    document.getElementById('btnCobrarMesa').addEventListener('click', procesarCobro);
    
    // Listeners para método de pago
    document.querySelectorAll('input[name="metodoPago"]').forEach(radio => {
        radio.addEventListener('change', handleMetodoPagoChange);
    });
    document.getElementById('montoPagado').addEventListener('input', calcularVuelto);
    document.getElementById('montoPropina').addEventListener('input', calcularVuelto);
    
    // Limpiar modal al cerrar ticket
    document.getElementById('modalTicket').addEventListener('hidden.bs.modal', () => {
        const modalVenta = bootstrap.Modal.getInstance(document.getElementById('modalVenta'));
        if (modalVenta) modalVenta.hide();
        cargarMesas();
    });
});

async function cargarProductos() {
    try {
        const response = await fetch('/api/productos');
        productosCache = await response.json();
        extraerCategorias();
    } catch (error) {
        console.error('Error cargando productos:', error);
    }
}

function extraerCategorias() {
    const cats = new Map();
    productosCache.forEach(p => {
        if (p.categoria) {
            cats.set(p.categoria.id, p.categoria);
        }
    });
    categoriasCache = Array.from(cats.values());
    if (categoriasCache.length > 0) {
        categoriaActual = categoriasCache[0].id;
    }
}

async function cargarMesas() {
    mostrarLoading(true);
    try {
        const response = await fetch(API_URL);
        const mesas = await response.json();
        renderizarMesas(mesas);
    } catch (error) {
        console.error('Error cargando mesas:', error);
        alert('Error al cargar las mesas');
    } finally {
        mostrarLoading(false);
    }
}

function renderizarMesas(mesas) {
    const grid = document.getElementById('gridMesas');
    grid.innerHTML = '';
    
    if (mesas.length === 0) {
        grid.innerHTML = '<div class="col-12 text-center text-muted"><p>No hay mesas configuradas.</p></div>';
        grid.classList.remove('d-none');
        return;
    }

    mesas.forEach(mesa => {
        const col = document.createElement('div');
        col.className = 'col position-relative';
        
        let claseEstado = '';
        let textoEstado = '';
        
        switch(mesa.estado) {
            case 'LIBRE':
                claseEstado = 'wrapper-green';
                textoEstado = 'Libre';
                break;
            case 'OCUPADA':
                claseEstado = 'wrapper-pink';
                textoEstado = 'Ocupada';
                break;
            case 'RESERVADA':
                claseEstado = 'wrapper-yellow';
                textoEstado = 'Reservada';
                break;
        }

        let tiempoTranscurrido = '';
        if (mesa.estado === 'OCUPADA' && mesa.ventaActivaId) {
            // Asumimos que la mesa.ventaActivaId significa que hay pedido
            // El backend no nos manda la fechaApertura en la mesa por defecto, pero podríamos aproximarlo
            // O podemos mostrar solo el status
            textoEstado = 'Ocupada';
        }

        let botonAccion = '';
        if (mesa.estado !== 'LIBRE') {
            botonAccion = `
                <button class="btn-eliminar-mesa text-primary" onclick="forzarLiberarMesa(event, ${mesa.id})" title="Forzar Liberación">
                    <i data-lucide="unlock" class="w-4 h-4"></i>
                </button>
            `;
        }

        col.innerHTML = `
            <div class="card-3d-wrapper ${claseEstado}" onclick="handleMesaClick(${mesa.id}, '${mesa.estado}', ${mesa.ventaActivaId})" style="height: 110px;">
                <div class="card-3d-content d-flex flex-column justify-content-between p-3 position-relative">
                    <div class="d-flex justify-content-between align-items-start h-100">
                        <span class="fs-1 fw-bold text-dark lh-1">${mesa.numero}</span>
                        ${botonAccion}
                    </div>
                </div>
                <div class="card-3d-footer">
                    ${textoEstado}
                </div>
            </div>
        `;
        
        grid.appendChild(col);
    });
    
    lucide.createIcons();
    grid.classList.remove('d-none');
}

async function agregarMesa() {
    try {
        const response = await fetch(API_URL, {
            method: 'POST'
        });
        if (response.ok) {
            cargarMesas();
        } else {
            alert('Error al agregar mesa');
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

async function forzarLiberarMesa(event, id) {
    event.stopPropagation();
    if (confirm('¿Estás seguro de que deseas liberar esta mesa? Esto cerrará cualquier orden activa sin cobrarla.')) {
        try {
            const response = await fetch(`${API_URL}/${id}/liberar`, {
                method: 'PATCH'
            });
            if (response.ok) {
                cargarMesas();
            } else {
                alert('No se pudo liberar la mesa.');
            }
        } catch (error) {
            console.error('Error:', error);
        }
    }
}

async function handleMesaClick(id, estado, ventaActivaId) {
    if (estado === 'LIBRE') {
        // Ocupar mesa
        if (confirm('¿Abrir cuenta y ocupar esta mesa?')) {
            try {
                const response = await fetch(`${API_URL}/${id}/ocupar`, {
                    method: 'PATCH'
                });
                if (response.ok) {
                    cargarMesas();
                } else {
                    alert('Error al ocupar la mesa');
                }
            } catch (error) {
                console.error('Error:', error);
            }
        }
    } else if (estado === 'OCUPADA' || estado === 'RESERVADA') {
        // Mostrar detalle de venta
        mesaSeleccionadaId = id;
        document.getElementById('spanMesaActiva').textContent = id;
        document.getElementById('montoVuelto').textContent = '0.00';
        document.getElementById('labelVuelto').classList.add('d-none');
        document.getElementById('pagoEfectivo').checked = true;
        
        await cargarDetallePedido(id);
        renderizarGridProductos();
        
        const modal = new bootstrap.Modal(document.getElementById('modalVenta'));
        modal.show();
    }
}

async function cargarDetallePedido(mesaId) {
    try {
        const response = await fetch(`${API_VENTAS}/mesas/${mesaId}/pedido-abierto`);
        if (response.ok) {
            const pedido = await response.json();
            pedidoActivoId = pedido.id;
            document.getElementById('pedidoActivoId').textContent = pedido.id;
            renderizarItemsPedido(pedido);
        }
    } catch (error) {
        console.error('Error cargando pedido:', error);
    }
}

function renderizarGridProductos() {
    const tabs = document.getElementById('tabsCategorias');
    tabs.innerHTML = '';
    
    categoriasCache.forEach(cat => {
        const li = document.createElement('li');
        li.className = 'nav-item';
        const isActive = cat.id === categoriaActual;
        li.innerHTML = `<button class="nav-link ${isActive ? 'active bg-secondary' : 'text-dark fw-medium'}" onclick="seleccionarCategoria(${cat.id})">${cat.nombre}</button>`;
        tabs.appendChild(li);
    });

    const liSin = document.createElement('li');
    liSin.className = 'nav-item';
    liSin.innerHTML = `<button class="nav-link ${!categoriaActual ? 'active bg-secondary' : 'text-dark fw-medium'}" onclick="seleccionarCategoria(null)">Otros</button>`;
    tabs.appendChild(liSin);

    const grid = document.getElementById('gridProductos');
    grid.innerHTML = '';
    
    const productosFiltrados = productosCache.filter(p => {
        if (categoriaActual) {
            return p.categoria && p.categoria.id === categoriaActual;
        } else {
            return !p.categoria;
        }
    });
    
    productosFiltrados.forEach(prod => {
        const col = document.createElement('div');
        col.className = 'col';

        col.innerHTML = `
            <div class="card-3d-wrapper wrapper-neutral" onclick="agregarProductoAlPedido(${prod.id})">
                <div class="card-3d-content d-flex flex-column justify-content-between">
                    <span class="fw-bold d-block mb-3 text-dark" style="font-size: 0.95rem;">${prod.nombre}</span>
                    <span class="fw-bolder d-block text-end" style="color: #4b5563;">S/${prod.precio.toFixed(2)}</span>
                </div>
                <div class="card-3d-footer">
                    Añadir Pedido
                </div>
            </div>
        `;
        grid.appendChild(col);
    });
}

function seleccionarCategoria(catId) {
    categoriaActual = catId;
    renderizarGridProductos();
}

async function agregarProductoAlPedido(productoId) {
    if (!pedidoActivoId) return;
    
    try {
        const response = await fetch(`${API_VENTAS}/pedidos/${pedidoActivoId}/items`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ productoId: productoId, cantidad: 1 })
        });
        
        if (response.ok) {
            const pedidoActualizado = await response.json();
            renderizarItemsPedido(pedidoActualizado);
        }
    } catch (error) {
        console.error('Error agregando producto:', error);
    }
}

function renderizarItemsPedido(pedido) {
    pedidoCache = pedido;
    const tbody = document.getElementById('tablaItemsPedido');
    tbody.innerHTML = '';
    
    if (!pedido.items || pedido.items.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted py-4">Aún no hay consumo en esta mesa</td></tr>';
    } else {
        pedido.items.forEach(item => {
            tbody.innerHTML += `
                <tr>
                    <td class="align-middle" style="width: 100px;">
                        <div class="d-flex align-items-center gap-2">
                            <button class="btn btn-sm btn-light btn-qty border" onclick="reducirItem(${item.producto.id})">-</button>
                            <span class="fw-semibold">${item.cantidad}</span>
                            <button class="btn btn-sm btn-light btn-qty border" onclick="agregarProductoAlPedido(${item.producto.id})">+</button>
                        </div>
                    </td>
                    <td class="align-middle fw-medium">${item.producto.nombre}</td>
                    <td class="align-middle text-end text-muted">S/${item.producto.precio.toFixed(2)}</td>
                    <td class="align-middle text-end fw-semibold text-dark">S/${item.subtotal.toFixed(2)}</td>
                </tr>
            `;
        });
    }
    
    document.getElementById('totalPedido').textContent = pedido.total.toFixed(2);
    calcularVuelto();
}

async function reducirItem(productoId) {
    if (!pedidoActivoId) return;
    
    try {
        const response = await fetch(`${API_VENTAS}/pedidos/${pedidoActivoId}/items/${productoId}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            const pedidoActualizado = await response.json();
            renderizarItemsPedido(pedidoActualizado);
        }
    } catch (error) {
        console.error('Error quitando producto:', error);
    }
}

async function limpiarCuenta() {
    if (!pedidoActivoId) return;
    
    if (confirm('¿Estás seguro de que quieres limpiar toda la cuenta actual?')) {
        try {
            const response = await fetch(`${API_VENTAS}/pedidos/${pedidoActivoId}/items`, {
                method: 'DELETE'
            });
            
            if (response.ok) {
                const pedidoActualizado = await response.json();
                renderizarItemsPedido(pedidoActualizado);
            }
        } catch (error) {
            console.error('Error limpiando cuenta:', error);
        }
    }
}

function handleMetodoPagoChange(e) {
    const cajaEfectivo = document.getElementById('cajaEfectivo');
    if (e.target.value === 'EFECTIVO') {
        cajaEfectivo.classList.remove('d-none');
    } else {
        cajaEfectivo.classList.add('d-none');
    }
}

function calcularVuelto() {
    if (!pedidoCache) return;
    
    const montoPagado = parseFloat(document.getElementById('montoPagado').value || 0);
    const propina = parseFloat(document.getElementById('montoPropina').value || 0);
    const total = pedidoCache.total + propina;
    const labelVuelto = document.getElementById('labelVuelto');
    const spanVuelto = document.getElementById('montoVuelto');
    
    if (montoPagado > total) {
        labelVuelto.classList.remove('d-none');
        spanVuelto.textContent = (montoPagado - total).toFixed(2);
    } else {
        labelVuelto.classList.add('d-none');
    }
}

async function procesarCobro() {
    if (!mesaSeleccionadaId || !pedidoCache) return;
    
    const metodoPago = document.querySelector('input[name="metodoPago"]:checked').value;
    let montoPagado = null;
    const propina = parseFloat(document.getElementById('montoPropina').value || 0);
    const totalConPropina = pedidoCache.total + propina;
    
    if (metodoPago === 'EFECTIVO') {
        montoPagado = parseFloat(document.getElementById('montoPagado').value || 0);
        if (montoPagado < totalConPropina) {
            alert('El monto ingresado es insuficiente para cubrir el total más propina.');
            return;
        }
    }

    if (confirm('¿Procesar el pago y liberar la mesa?')) {
        try {
            const requestBody = {
                metodoPago: metodoPago,
                montoPagado: montoPagado,
                propina: propina
            };


            const response = await fetch(`${API_URL}/${mesaSeleccionadaId}/liberar`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestBody)
            });

            if (response.ok) {
                generarTicket();
                
                const modalTicket = new bootstrap.Modal(document.getElementById('modalTicket'));
                modalTicket.show();
                
                mesaSeleccionadaId = null;
                pedidoActivoId = null;
                pedidoCache = null;
                document.getElementById('montoPagado').value = '';
            } else {
                const errorMsg = await response.text();
                alert('Error al procesar el cobro: ' + (errorMsg || 'Verifique caja y stock'));
            }
        } catch (error) {
            console.error('Error:', error);
        }
    }
}

function generarTicket() {
    const metodoPago = document.querySelector('input[name="metodoPago"]:checked').value;
    const montoPagado = parseFloat(document.getElementById('montoPagado').value || 0);
    
    let html = `
        <div class="fw-bold mb-2 text-center border-bottom pb-2">TICKET DE VENTA</div>
        <div class="mb-2">Mesa: ${document.getElementById('spanMesaActiva').textContent}</div>
        <div class="mb-2">Pedido: #${pedidoActivoId}</div>
        <div class="border-bottom mb-2"></div>
    `;
    
    pedidoCache.items.forEach(item => {
        html += `
            <div class="d-flex justify-content-between mb-1">
                <span>${item.cantidad}x ${item.producto.nombre}</span>
                <span>S/${item.subtotal.toFixed(2)}</span>
            </div>
        `;
    });
    
    html += `
        <div class="border-bottom my-2"></div>
        <div class="d-flex justify-content-between fw-bold mb-2">
            <span>TOTAL:</span>
            <span>S/${pedidoCache.total.toFixed(2)}</span>
        </div>
        <div class="d-flex justify-content-between text-muted">
            <span>Pago (${metodoPago}):</span>
            <span>S/${metodoPago === 'EFECTIVO' ? montoPagado.toFixed(2) : pedidoCache.total.toFixed(2)}</span>
        </div>
    `;
    
    const propina = parseFloat(document.getElementById('montoPropina').value || 0);
    const totalConPropina = pedidoCache.total + propina;

    if (metodoPago === 'EFECTIVO' && montoPagado > totalConPropina) {
        html += `
            <div class="d-flex justify-content-between mt-1 fw-semibold text-success">
                <span>Vuelto:</span>
                <span>S/${(montoPagado - totalConPropina).toFixed(2)}</span>
            </div>
        `;
    }
    
    document.getElementById('ticketContent').innerHTML = html;
    
    // Preparar el ticket para impresión real
    let printHtml = `
        <div class="ticket-header">
            <strong>RESTAURANTE POS</strong><br>
            Ticket #${pedidoActivoId}<br>
            Mesa: ${document.getElementById('spanMesaActiva').textContent}
        </div>
        <table class="ticket-table">
            <thead>
                <tr>
                    <th>Cant</th>
                    <th>Prod</th>
                    <th>Subt</th>
                </tr>
            </thead>
            <tbody>
    `;
    pedidoCache.items.forEach(item => {
        printHtml += `
            <tr>
                <td>${item.cantidad}</td>
                <td>${item.producto.nombre}</td>
                <td>S/${item.subtotal.toFixed(2)}</td>
            </tr>
        `;
    });
    printHtml += `
            </tbody>
        </table>
        <div class="ticket-footer">
            <strong>TOTAL: S/${pedidoCache.total.toFixed(2)}</strong><br>
            ${propina > 0 ? `<strong>PROPINA: S/${propina.toFixed(2)}</strong><br>` : ''}
            <strong>TOTAL PAGADO: S/${totalConPropina.toFixed(2)}</strong><br>
            Pago (${metodoPago}): S/${metodoPago === 'EFECTIVO' ? montoPagado.toFixed(2) : totalConPropina.toFixed(2)}<br>
            ${(metodoPago === 'EFECTIVO' && montoPagado > totalConPropina) ? 'Vuelto: S/' + (montoPagado - totalConPropina).toFixed(2) : ''}
            <br><br>
            ¡Gracias por su compra!
        </div>
    `;
    
    document.getElementById('ticketPrint').innerHTML = printHtml;
}

function imprimirTicket() {
    window.print();
    const modalTicket = bootstrap.Modal.getInstance(document.getElementById('modalTicket'));
    if (modalTicket) modalTicket.hide();
}

function mostrarLoading(show) {
    const loading = document.getElementById('loading');
    const grid = document.getElementById('gridMesas');
    
    if (show) {
        loading.classList.remove('d-none');
        grid.classList.add('d-none');
    } else {
        loading.classList.add('d-none');
    }
}

async function checkUserRole() {
    try {
        const response = await fetch('/api/auth/me');
        if (response.ok) {
            const user = await response.json();
            if (user.rol !== 'ROLE_ADMIN' && user.rol !== 'ADMIN') {
                const linkInv = document.getElementById('linkInventario');
                const linkRep = document.getElementById('linkReportes');
                const btnMesa = document.getElementById('btnAgregarMesa');
                
                if (linkInv) linkInv.classList.add('d-none');
                if (linkRep) linkRep.classList.add('d-none');
                if (btnMesa) btnMesa.classList.add('d-none');
            }
        }
    } catch (e) {
        console.error('Error checking role:', e);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    checkUserRole();
    checkCaja();
    cargarMesas();
    cargarProductos();
    setInterval(cargarMesas, 5000);
});
