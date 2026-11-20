package com.finanzasApp.finanzasapp.data.model

import java.math.BigDecimal

/** Datos para guardar un movimiento local. */
data class MovimientoRequest(
    val tipo: TipoMovimiento,
    val descripcion: String,
    val monto: BigDecimal,
    val categoriaId: Long,
    val fecha: String   // "yyyy-MM-dd"
)
