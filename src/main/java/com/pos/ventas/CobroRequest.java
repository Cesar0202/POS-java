package com.pos.ventas;

public class CobroRequest {
    private MetodoPago metodoPago;
    private Double montoPagado;

    private Double propina;

    // Getters y Setters
    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }
    public Double getMontoPagado() { return montoPagado; }
    public void setMontoPagado(Double montoPagado) { this.montoPagado = montoPagado; }
    public Double getPropina() { return propina; }
    public void setPropina(Double propina) { this.propina = propina; }
}
