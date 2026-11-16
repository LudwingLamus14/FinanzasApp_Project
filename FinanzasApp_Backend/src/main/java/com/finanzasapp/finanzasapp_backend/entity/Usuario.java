package com.finanzasapp.finanzasapp_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Tabla usuarios. Solo lectura: la app usa un unico usuario de prueba. */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    protected Usuario() {
        // Constructor requerido por JPA
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
}
