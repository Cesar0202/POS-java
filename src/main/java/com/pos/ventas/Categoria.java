package com.pos.ventas;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    private String color; // ej. #ff0000 o bg-primary
    
    public Categoria() {}

    public Categoria(String nombre, String color) {
        this.nombre = nombre;
        this.color = color;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
