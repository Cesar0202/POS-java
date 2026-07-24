package com.pos.mesas;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.pos.ventas.CobroRequest;

import java.util.List;

@RestController
@RequestMapping("/api/mesas")
public class MesaController {

    private final MesaService mesaService;

    public MesaController(MesaService mesaService) {
        this.mesaService = mesaService;
    }

    @GetMapping
    public List<Mesa> listarMesas() {
        return mesaService.listarTodas();
    }

    @PostMapping
    public ResponseEntity<Mesa> agregarMesa() {
        Mesa nuevaMesa = mesaService.agregarMesa();
        return ResponseEntity.ok(nuevaMesa);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMesa(@PathVariable Long id) {
        try {
            mesaService.eliminarMesa(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/ocupar")
    public ResponseEntity<?> ocuparMesa(@PathVariable Long id) {
        try {
            Mesa mesa = mesaService.ocuparMesa(id);
            return ResponseEntity.ok(mesa);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/liberar")
    public ResponseEntity<?> liberarMesa(@PathVariable Long id, @RequestBody(required = false) CobroRequest request) {
        try {
            Mesa mesa = mesaService.liberarMesa(id, request);
            return ResponseEntity.ok(mesa);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
