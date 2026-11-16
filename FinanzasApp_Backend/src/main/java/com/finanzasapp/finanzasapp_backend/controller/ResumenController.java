package com.finanzasapp.finanzasapp_backend.controller;

import com.finanzasapp.finanzasapp_backend.dto.ResumenResponse;
import com.finanzasapp.finanzasapp_backend.service.MovimientoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resumen")
public class ResumenController {

    private final MovimientoService service;

    public ResumenController(MovimientoService service) {
        this.service = service;
    }

    /** GET /api/resumen -> {"totalIngresos": ..., "totalGastos": ..., "saldo": ...} */
    @GetMapping
    public ResumenResponse resumen() {
        return service.resumen();
    }
}
