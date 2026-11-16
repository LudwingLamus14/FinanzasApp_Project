package com.finanzasapp.finanzasapp_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Respuesta de GET /api/estadisticas. */
public record EstadisticasResponse(
        String periodo,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        BigDecimal totalIngresos,
        BigDecimal totalGastos,
        BigDecimal promedioGastoDiario,
        List<CategoriaGastoResponse> gastosPorCategoria,
        CategoriaGastoResponse categoriaMayorGasto) {
}
