package com.pos.ventas;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ventas")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }



    @GetMapping("/mesas/{mesaId}/pedido-abierto")
    public ResponseEntity<Pedido> obtenerPedidoAbierto(@PathVariable Long mesaId) {
        Pedido pedido = pedidoService.obtenerPedidoAbiertoPorMesa(mesaId);
        if (pedido == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pedido);
    }

    @PostMapping("/pedidos/{pedidoId}/items")
    public ResponseEntity<?> agregarItem(@PathVariable Long pedidoId, @RequestBody AgregarItemRequest request) {
        try {
            Pedido pedido = pedidoService.agregarItem(pedidoId, request);
            return ResponseEntity.ok(pedido);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/pedidos/{pedidoId}/items/{productoId}")
    public ResponseEntity<?> removerItem(@PathVariable Long pedidoId, @PathVariable Long productoId) {
        try {
            Pedido pedido = pedidoService.removerItem(pedidoId, productoId);
            return ResponseEntity.ok(pedido);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/pedidos/{pedidoId}/items")
    public ResponseEntity<?> limpiarPedido(@PathVariable Long pedidoId) {
        try {
            Pedido pedido = pedidoService.limpiarPedido(pedidoId);
            return ResponseEntity.ok(pedido);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
