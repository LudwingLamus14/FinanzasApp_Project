package com.finanzasApp.finanzasapp.data.model

/** Formato de error del backend: {"status": 400, "mensaje": "..."} */
data class ErrorResponse(
    val status: Int? = null,
    val mensaje: String? = null
)
