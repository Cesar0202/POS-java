package com.pos.caja;

import com.pos.seguridad.Usuario;
import com.pos.seguridad.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/caja")
public class CajaController {

    private final TurnoCajaRepository turnoCajaRepository;
    private final UsuarioRepository usuarioRepository;

    public CajaController(TurnoCajaRepository turnoCajaRepository, UsuarioRepository usuarioRepository) {
        this.turnoCajaRepository = turnoCajaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/estado")
    public ResponseEntity<TurnoCaja> estadoCaja() {
        return turnoCajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoCaja.ABIERTA)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/abrir")
    public ResponseEntity<?> abrirCaja(@RequestBody Map<String, Double> payload) {
        if (turnoCajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoCaja.ABIERTA).isPresent()) {
            return ResponseEntity.badRequest().body("Ya existe una caja abierta.");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Double montoInicial = payload.getOrDefault("montoInicial", 0.0);
        TurnoCaja turno = new TurnoCaja(montoInicial, usuario);
        return ResponseEntity.ok(turnoCajaRepository.save(turno));
    }

    @PostMapping("/cerrar")
    public ResponseEntity<?> cerrarCaja(@RequestBody Map<String, Double> payload) {
        TurnoCaja turnoAbierto = turnoCajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoCaja.ABIERTA)
                .orElse(null);

        if (turnoAbierto == null) {
            return ResponseEntity.badRequest().body("No hay una caja abierta.");
        }

        Double montoFinal = payload.get("montoFinal");
        if (montoFinal == null) {
            return ResponseEntity.badRequest().body("Se requiere el monto final.");
        }

        turnoAbierto.setEstado(EstadoCaja.CERRADA);
        turnoAbierto.setFechaCierre(LocalDateTime.now());
        turnoAbierto.setMontoFinal(montoFinal);

        return ResponseEntity.ok(turnoCajaRepository.save(turnoAbierto));
    }
}
