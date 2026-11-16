package com.finanzasapp.finanzasapp_backend.dto;

import com.finanzasapp.finanzasapp_backend.entity.Categoria;
import com.finanzasapp.finanzasapp_backend.entity.TipoMovimiento;

public record CategoriaResponse(Long id, String nombre, TipoMovimiento tipo) {

    public static CategoriaResponse desde(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNombre(), c.getTipo());
    }
}
