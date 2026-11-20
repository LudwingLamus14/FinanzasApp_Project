package com.finanzasApp.finanzasapp.data.model

import java.math.BigDecimal

/** Datos almacenados o calculados localmente. */
data class Resumen(
    val totalIngresos: BigDecimal,
    val totalGastos: BigDecimal,
    val saldo: BigDecimal
)
