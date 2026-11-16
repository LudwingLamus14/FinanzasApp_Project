package com.finanzasapp.finanzasapp_backend.controller;

import com.finanzasapp.finanzasapp_backend.dto.EstadisticasResponse;
import com.finanzasapp.finanzasapp_backend.entity.PeriodoEstadistica;
import com.finanzasapp.finanzasapp_backend.service.EstadisticasService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estadisticas")
public class EstadisticasController {

    private final EstadisticasService service;

    public EstadisticasController(EstadisticasService service) {
        this.service = service;
    }

    /**
     * GET /api/estadisticas?periodo=DIA|SEMANA|ANIO (por defecto DIA).
     * Un valor invalido (ej. ?periodo=MES) ya lo captura GlobalExceptionHandler
     * (MethodArgumentTypeMismatchException) y responde 400.
     */
    @GetMapping
    public EstadisticasResponse obtener(
            @RequestParam(defaultValue = "DIA") PeriodoEstadistica periodo) {
        return service.obtener(periodo);
    }
}
