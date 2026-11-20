package com.finanzasApp.finanzasapp.data.model

/** Datos almacenados o calculados localmente. */
data class Categoria(
    val id: Long,
    val nombre: String,
    val tipo: TipoMovimiento
)
