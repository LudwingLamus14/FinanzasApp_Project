package com.finanzasapp.finanzasapp_backend.controller;

import com.finanzasapp.finanzasapp_backend.dto.CategoriaResponse;
import com.finanzasapp.finanzasapp_backend.repository.CategoriaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaRepository repository;

    public CategoriaController(CategoriaRepository repository) {
        this.repository = repository;
    }

    /** GET /api/categorias -> todas, ordenadas por tipo y nombre (sin logica: va directo al repositorio) */
    @GetMapping
    public List<CategoriaResponse> listar() {
        return repository.findAllByOrderByTipoAscNombreAsc().stream()
                .map(CategoriaResponse::desde)
                .toList();
    }
}
