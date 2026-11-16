package com.finanzasapp.finanzasapp_backend.dto;

import java.math.BigDecimal;

/** Respuesta de GET /api/resumen. saldo = totalIngresos - totalGastos. */
public record ResumenResponse(BigDecimal totalIngresos, BigDecimal totalGastos, BigDecimal saldo) {
}
