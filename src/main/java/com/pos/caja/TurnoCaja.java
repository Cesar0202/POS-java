package com.pos.caja;

import com.pos.seguridad.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "turnos_caja")
public class TurnoCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fechaApertura;

    private LocalDateTime fechaCierre;

    @Column(nullable = false)
    private Double montoInicial;

    private Double montoFinal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCaja estado;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuarioApertura;

    public TurnoCaja() {}

    public TurnoCaja(Double montoInicial, Usuario usuario) {
        this.fechaApertura = LocalDateTime.now();
        this.montoInicial = montoInicial;
        this.estado = EstadoCaja.ABIERTA;
        this.usuarioApertura = usuario;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(LocalDateTime fechaApertura) { this.fechaApertura = fechaApertura; }
    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }
    public Double getMontoInicial() { return montoInicial; }
    public void setMontoInicial(Double montoInicial) { this.montoInicial = montoInicial; }
    public Double getMontoFinal() { return montoFinal; }
    public void setMontoFinal(Double montoFinal) { this.montoFinal = montoFinal; }
    public EstadoCaja getEstado() { return estado; }
    public void setEstado(EstadoCaja estado) { this.estado = estado; }
    public Usuario getUsuarioApertura() { return usuarioApertura; }
    public void setUsuarioApertura(Usuario usuarioApertura) { this.usuarioApertura = usuarioApertura; }
}
