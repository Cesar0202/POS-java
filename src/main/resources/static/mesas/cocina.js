const API_COCINA = '/api/cocina';

// Interceptor para redirección de login
const originalFetch = window.fetch;
window.fetch = async function() {
    const response = await originalFetch.apply(this, arguments);
    if (response.url && response.url.includes('/login.html')) {
        window.location.href = '/mesas/login.html';
        return new Promise(() => {});
    }
    return response;
}

document.addEventListener('DOMContentLoaded', () => {
    cargarPendientes();
    setInterval(cargarPendientes, 5000); // Refrescar cada 5 segundos
});

async function cargarPendientes() {
    try {
        const response = await fetch(`${API_COCINA}/pendientes`);
        const items = await response.json();
        renderizarKanban(items);
    } catch (e) {
        console.error("Error cargando cocina", e);
    }
}

function renderizarKanban(items) {
    const colP = document.getElementById('colPendientes');
    const colPr = document.getElementById('colPreparando');
    const colL = document.getElementById('colListos');

    colP.innerHTML = '';
    colPr.innerHTML = '';
    colL.innerHTML = '';

    let cP = 0, cPr = 0, cL = 0;

    items.forEach(item => {
        const card = document.createElement('div');
        
        let headerText = item.producto.nombre;
        let mesaBadge = `<span class="badge bg-secondary">Sin Mesa</span>`;
        if (item.pedido) {
            // El backend no devuelve la mesa directamente en el item para evitar JSON infinito
            // Asumiendo que podemos tener un string o algo, si no, omitimos
            // En un sistema real deberíamos traer el ID de la mesa
        }

        let actions = '';
        if (item.estadoCocina === 'PENDIENTE') {
            card.className = 'kanban-card';
            actions = `<button class="btn btn-sm btn-warning w-100" onclick="cambiarEstado(${item.id}, 'PREPARANDO')">Preparar</button>`;
            cP++;
            colP.appendChild(card);
        } else if (item.estadoCocina === 'PREPARANDO') {
            card.className = 'kanban-card preparando';
            actions = `<button class="btn btn-sm btn-success w-100" onclick="cambiarEstado(${item.id}, 'LISTO')">Marcar Listo</button>`;
            cPr++;
            colPr.appendChild(card);
        } else if (item.estadoCocina === 'LISTO') {
            card.className = 'kanban-card listo';
            actions = `<button class="btn btn-sm btn-outline-primary w-100" onclick="cambiarEstado(${item.id}, 'ENTREGADO')">Entregar (Quitar)</button>`;
            cL++;
            colL.appendChild(card);
        }

        card.innerHTML = `
            <div class="d-flex justify-content-between align-items-start mb-2">
                <span class="fw-bold fs-5">${item.cantidad}x</span>
                <span class="fw-semibold text-end">${headerText}</span>
            </div>
            <div class="mt-2 text-muted small">
                <!-- Info extra como notas irían aquí -->
            </div>
            <div class="mt-3">
                ${actions}
            </div>
        `;
    });

    document.getElementById('countPendientes').textContent = cP;
    document.getElementById('countPreparando').textContent = cPr;
    document.getElementById('countListos').textContent = cL;
}

async function cambiarEstado(id, nuevoEstado) {
    try {
        const response = await fetch(`${API_COCINA}/items/${id}/estado`, {
            method: 'PATCH',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({estado: nuevoEstado})
        });
        if (response.ok) {
            cargarPendientes();
        }
    } catch (e) {
        console.error("Error", e);
    }
}
