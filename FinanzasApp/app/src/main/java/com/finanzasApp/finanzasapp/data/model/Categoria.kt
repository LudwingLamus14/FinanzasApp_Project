package com.finanzasApp.finanzasapp.data.model

/** GET /api/categorias */
data class Categoria(
    val id: Long,
    val nombre: String,
    val tipo: TipoMovimiento
)
