package com.finanzasApp.finanzasapp.data.model

import java.math.BigDecimal

/** Cuerpo del POST /api/movimientos. */
data class MovimientoRequest(
    val tipo: TipoMovimiento,
    val descripcion: String,
    val monto: BigDecimal,
    val categoriaId: Long,
    val fecha: String   // "yyyy-MM-dd"
)
