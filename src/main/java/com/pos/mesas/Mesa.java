package com.pos.mesas;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mesas")
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer numero;

    @Enumerated(EnumType.STRING)
    private EstadoMesa estado;

    private Long ventaActivaId; // ID de la venta asociada cuando está ocupada

    public Mesa() {
    }

    public Mesa(Integer numero, EstadoMesa estado) {
        this.numero = numero;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public EstadoMesa getEstado() {
        return estado;
    }

    public void setEstado(EstadoMesa estado) {
        this.estado = estado;
    }

    public Long getVentaActivaId() {
        return ventaActivaId;
    }

    public void setVentaActivaId(Long ventaActivaId) {
        this.ventaActivaId = ventaActivaId;
    }
}
