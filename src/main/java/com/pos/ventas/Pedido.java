package com.pos.ventas;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long mesaId;

    @Enumerated(EnumType.STRING)
    private EstadoPedido estado;

    private Double total = 0.0;
    
    private Double propina = 0.0;

    private LocalDateTime fechaApertura;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    private Double montoPagado;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ItemPedido> items = new ArrayList<>();

    public Pedido() {}

    public Pedido(Long mesaId, EstadoPedido estado) {
        this.mesaId = mesaId;
        this.estado = estado;
        this.fechaApertura = LocalDateTime.now();
    }

    public void recalcularTotal() {
        this.total = items.stream().mapToDouble(ItemPedido::getSubtotal).sum();
    }

    public void addItem(ItemPedido item) {
        items.add(item);
        item.setPedido(this);
        recalcularTotal();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMesaId() { return mesaId; }
    public void setMesaId(Long mesaId) { this.mesaId = mesaId; }
    public Double getPropina() { return propina; }
    public void setPropina(Double propina) { this.propina = propina; }
    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(LocalDateTime fechaApertura) { this.fechaApertura = fechaApertura; }
    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }
    public Double getMontoPagado() { return montoPagado; }
    public void setMontoPagado(Double montoPagado) { this.montoPagado = montoPagado; }

    public List<ItemPedido> getItems() { return items; }
    public void setItems(List<ItemPedido> items) { this.items = items; }
}
