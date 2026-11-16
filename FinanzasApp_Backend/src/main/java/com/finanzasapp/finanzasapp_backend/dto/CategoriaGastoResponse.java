package com.finanzasapp.finanzasapp_backend.dto;

import java.math.BigDecimal;

/** Gasto total de una categoria dentro de un periodo. */
public record CategoriaGastoResponse(Long categoriaId, String categoriaNombre, BigDecimal total) {
}
