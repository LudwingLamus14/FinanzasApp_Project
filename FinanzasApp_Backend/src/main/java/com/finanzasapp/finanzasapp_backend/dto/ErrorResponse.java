package com.finanzasapp.finanzasapp_backend.dto;

/** Formato unico de error: {"status": 400, "mensaje": "..."} */
public record ErrorResponse(int status, String mensaje) {
}
