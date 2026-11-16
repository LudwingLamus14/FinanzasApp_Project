package com.finanzasApp.finanzasapp.data.model

import java.math.BigDecimal

/**
 * GET /api/movimientos
 * - monto: BigDecimal (nunca Double) para no perder precision con el dinero.
 * - fecha: llega como texto "yyyy-MM-dd"; se formatea al mostrarla.
 */
data class Movimiento(
    val id: Long,
    val tipo: TipoMovimiento,
    val descripcion: String,
    val monto: BigDecimal,
    val fecha: String,
    val categoriaId: Long,
    val categoriaNombre: String
)
