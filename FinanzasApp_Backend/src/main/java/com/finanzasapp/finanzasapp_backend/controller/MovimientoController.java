package com.finanzasapp.finanzasapp_backend.controller;

import com.finanzasapp.finanzasapp_backend.dto.MovimientoRequest;
import com.finanzasapp.finanzasapp_backend.dto.MovimientoResponse;
import com.finanzasapp.finanzasapp_backend.service.MovimientoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoController {

    private final MovimientoService service;

    public MovimientoController(MovimientoService service) {
        this.service = service;
    }

    /** GET /api/movimientos -> 200, mas recientes primero */
    @GetMapping
    public List<MovimientoResponse> listar() {
        return service.listar();
    }

    /** GET /api/movimientos/{id} -> 200 o 404 */
    @GetMapping("/{id}")
    public MovimientoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    /** POST /api/movimientos -> 201, o 400 si los datos son invalidos */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoResponse crear(@Valid @RequestBody MovimientoRequest request) {
        return service.crear(request);
    }

    /** DELETE /api/movimientos/{id} -> 204 o 404 */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
