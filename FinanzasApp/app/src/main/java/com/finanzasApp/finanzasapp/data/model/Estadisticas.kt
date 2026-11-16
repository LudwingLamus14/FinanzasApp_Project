package com.finanzasApp.finanzasapp.data.model

import java.math.BigDecimal

/** Datos almacenados o calculados localmente. */
data class Estadisticas(
    val periodo: String,
    val fechaInicio: String,
    val fechaFin: String,
    val totalIngresos: BigDecimal,
    val totalGastos: BigDecimal,
    val promedioGastoDiario: BigDecimal,
    val gastosPorCategoria: List<CategoriaGasto>,
    val categoriaMayorGasto: CategoriaGasto?
)
