package com.finanzasapp.finanzasapp_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Tabla movimientos: un ingreso o un gasto del usuario. */
@Entity
@Table(name = "movimientos")
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 7)
    private TipoMovimiento tipo;

    @Column(name = "descripcion", nullable = false, length = 200)
    private String descripcion;

    // BigDecimal <-> NUMERIC(14,2): nunca float/double para dinero
    @Column(name = "monto", nullable = false, precision = 14, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    // La rellena la base de datos (DEFAULT CURRENT_TIMESTAMP)
    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    protected Movimiento() {
        // Constructor requerido por JPA
    }

    public Movimiento(Usuario usuario, Categoria categoria, TipoMovimiento tipo,
                      String descripcion, BigDecimal monto, LocalDate fecha) {
        this.usuario = usuario;
        this.categoria = categoria;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.monto = monto;
        this.fecha = fecha;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public Categoria getCategoria() { return categoria; }
    public TipoMovimiento getTipo() { return tipo; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getMonto() { return monto; }
    public LocalDate getFecha() { return fecha; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}
