package com.finanzasApp.finanzasapp.data.model

import java.math.BigDecimal

/** GET /api/resumen */
data class Resumen(
    val totalIngresos: BigDecimal,
    val totalGastos: BigDecimal,
    val saldo: BigDecimal
)
