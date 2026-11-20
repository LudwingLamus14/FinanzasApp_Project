package com.finanzasApp.finanzasapp.data.model

import java.math.BigDecimal

/** Gasto total de una categoria dentro de un periodo (parte de Estadisticas). */
data class CategoriaGasto(
    val categoriaId: Long,
    val categoriaNombre: String,
    val total: BigDecimal
)
