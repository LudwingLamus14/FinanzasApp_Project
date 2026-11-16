package com.finanzasapp.finanzasapp_backend.dto;

import com.finanzasapp.finanzasapp_backend.entity.Movimiento;
import com.finanzasapp.finanzasapp_backend.entity.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Movimiento tal como lo ve la app Android. */
public record MovimientoResponse(
        Long id,
        TipoMovimiento tipo,
        String descripcion,
        BigDecimal monto,
        LocalDate fecha,
        Long categoriaId,
        String categoriaNombre) {

    public static MovimientoResponse desde(Movimiento m) {
        return new MovimientoResponse(
                m.getId(),
                m.getTipo(),
                m.getDescripcion(),
                m.getMonto(),
                m.getFecha(),
                m.getCategoria().getId(),
                m.getCategoria().getNombre());
    }
}
