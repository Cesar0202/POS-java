package com.pos.ventas;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import com.pos.caja.TurnoCajaRepository;
import com.pos.caja.EstadoCaja;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final TurnoCajaRepository turnoCajaRepository;

    public PedidoService(PedidoRepository pedidoRepository, ProductoRepository productoRepository, TurnoCajaRepository turnoCajaRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.turnoCajaRepository = turnoCajaRepository;
    }

    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    @Transactional
    public Pedido obtenerPedidoAbiertoPorMesa(Long mesaId) {
        return pedidoRepository.findByMesaIdAndEstado(mesaId, EstadoPedido.ABIERTO)
                .orElse(null);
    }

    @Transactional
    public Pedido crearPedidoParaMesa(Long mesaId) {
        Pedido pedido = new Pedido(mesaId, EstadoPedido.ABIERTO);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido agregarItem(Long pedidoId, AgregarItemRequest request) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
        
        if (pedido.getEstado() != EstadoPedido.ABIERTO) {
            throw new IllegalStateException("El pedido ya está cerrado.");
        }

        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        ItemPedido itemExistente = pedido.getItems().stream()
                .filter(item -> item.getProducto().getId().equals(producto.getId()))
                .findFirst()
                .orElse(null);

        int cantidadActual = (itemExistente != null) ? itemExistente.getCantidad() : 0;
        if (cantidadActual + request.getCantidad() > producto.getStock()) {
            throw new IllegalStateException("Stock insuficiente para el producto: " + producto.getNombre());
        }

        if (itemExistente != null) {
            itemExistente.incrementarCantidad(request.getCantidad());
            pedido.recalcularTotal();
        } else {
            ItemPedido nuevoItem = new ItemPedido(producto, request.getCantidad());
            pedido.addItem(nuevoItem);
        }

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido cobrarPedido(Long pedidoId, CobroRequest request) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
        
        if (pedido.getEstado() != EstadoPedido.ABIERTO) {
            throw new IllegalStateException("El pedido ya está cobrado o cancelado.");
        }

        if (turnoCajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoCaja.ABIERTA).isEmpty()) {
            throw new IllegalStateException("No hay un turno de caja abierto.");
        }

        // Validar y descontar stock
        for (ItemPedido item : pedido.getItems()) {
            Producto p = item.getProducto();
            if (p.getStock() < item.getCantidad()) {
                throw new IllegalStateException("Stock insuficiente durante el cobro para: " + p.getNombre());
            }
            p.setStock(p.getStock() - item.getCantidad());
            productoRepository.save(p);
        }
        
        pedido.setEstado(EstadoPedido.PAGADO);
        
        if (request != null) {
            pedido.setMetodoPago(request.getMetodoPago());
            
            if (request.getPropina() != null && request.getPropina() > 0) {
                pedido.setPropina(request.getPropina());
            }

            // Si es efectivo y el monto pagado es mayor o igual al total + propina
            double totalAPagar = pedido.getTotal() + pedido.getPropina();
            if (request.getMetodoPago() == MetodoPago.EFECTIVO && request.getMontoPagado() != null) {
                if (request.getMontoPagado() < totalAPagar) {
                    throw new IllegalArgumentException("El monto pagado es insuficiente.");
                }
                pedido.setMontoPagado(request.getMontoPagado());
            } else {
                // Tarjeta o transferencia, el monto pagado es exactamente el total + propina
                pedido.setMontoPagado(totalAPagar);
            }

        } else {
            // Fallback por defecto si no mandan request
            pedido.setMetodoPago(MetodoPago.EFECTIVO);
            pedido.setMontoPagado(pedido.getTotal());
        }

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido removerItem(Long pedidoId, Long productoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
        
        if (pedido.getEstado() != EstadoPedido.ABIERTO) {
            throw new IllegalStateException("El pedido ya está cerrado.");
        }

        ItemPedido itemExistente = pedido.getItems().stream()
                .filter(item -> item.getProducto().getId().equals(productoId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Producto no está en el pedido"));

        if (itemExistente.getCantidad() > 1) {
            itemExistente.incrementarCantidad(-1);
        } else {
            pedido.getItems().remove(itemExistente);
        }
        
        pedido.recalcularTotal();
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido limpiarPedido(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
        
        if (pedido.getEstado() != EstadoPedido.ABIERTO) {
            throw new IllegalStateException("El pedido ya está cerrado.");
        }

        pedido.getItems().clear();
        pedido.recalcularTotal();
        return pedidoRepository.save(pedido);
    }
}
