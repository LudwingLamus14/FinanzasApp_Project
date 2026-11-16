package com.finanzasapp.finanzasapp_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Tabla categorias. Solo lectura: se cargan desde database.sql. */
@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 7)
    private TipoMovimiento tipo;

    protected Categoria() {
        // Constructor requerido por JPA
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public TipoMovimiento getTipo() { return tipo; }
}
