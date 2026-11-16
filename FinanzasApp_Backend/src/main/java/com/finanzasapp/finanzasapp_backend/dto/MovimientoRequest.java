package com.finanzasapp.finanzasapp_backend.dto;

import com.finanzasapp.finanzasapp_backend.entity.TipoMovimiento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Cuerpo del POST /api/movimientos. La fecha llega como "yyyy-MM-dd". */
public record MovimientoRequest(

        @NotNull(message = "El tipo es obligatorio")
        TipoMovimiento tipo,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 200, message = "La descripción no puede superar 200 caracteres")
        String descripcion,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.00", inclusive = false, message = "El monto debe ser mayor que 0")
        @Digits(integer = 12, fraction = 2, message = "El monto admite hasta 12 enteros y 2 decimales")
        BigDecimal monto,

        @NotNull(message = "La categoría es obligatoria")
        Long categoriaId,

        @NotNull(message = "La fecha es obligatoria")
        LocalDate fecha) {
}
