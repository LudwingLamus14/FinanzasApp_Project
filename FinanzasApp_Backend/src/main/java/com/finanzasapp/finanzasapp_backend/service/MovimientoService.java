package com.finanzasapp.finanzasapp_backend.service;

import com.finanzasapp.finanzasapp_backend.dto.MovimientoRequest;
import com.finanzasapp.finanzasapp_backend.dto.MovimientoResponse;
import com.finanzasapp.finanzasapp_backend.dto.ResumenResponse;
import com.finanzasapp.finanzasapp_backend.entity.Categoria;
import com.finanzasapp.finanzasapp_backend.entity.Movimiento;
import com.finanzasapp.finanzasapp_backend.entity.TipoMovimiento;
import com.finanzasapp.finanzasapp_backend.entity.Usuario;
import com.finanzasapp.finanzasapp_backend.exception.DatosInvalidosException;
import com.finanzasapp.finanzasapp_backend.exception.RecursoNoEncontradoException;
import com.finanzasapp.finanzasapp_backend.repository.CategoriaRepository;
import com.finanzasapp.finanzasapp_backend.repository.MovimientoRepository;
import com.finanzasapp.finanzasapp_backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/** Logica de negocio de los movimientos y del resumen. */
@Service
public class MovimientoService {

    /** La app no tiene login: todos los movimientos son del usuario de prueba (id 1 en database.sql). */
    private static final Long USUARIO_ID = 1L;

    private final MovimientoRepository movimientoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;

    public MovimientoService(MovimientoRepository movimientoRepository,
                             CategoriaRepository categoriaRepository,
                             UsuarioRepository usuarioRepository) {
        this.movimientoRepository = movimientoRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<MovimientoResponse> listar() {
        return movimientoRepository.listarPorUsuario(USUARIO_ID).stream()
                .map(MovimientoResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public MovimientoResponse obtener(Long id) {
        return MovimientoResponse.desde(buscar(id));
    }

    @Transactional
    public MovimientoResponse crear(MovimientoRequest request) {
        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new DatosInvalidosException("La categoría indicada no existe."));

        // Regla de negocio: el tipo del movimiento debe coincidir con el de la categoria.
        if (categoria.getTipo() != request.tipo()) {
            throw new DatosInvalidosException(
                    "La categoría '" + categoria.getNombre() + "' es de tipo " + categoria.getTipo()
                            + " y no puede usarse en un movimiento de tipo " + request.tipo() + ".");
        }

        Usuario usuario = usuarioRepository.findById(USUARIO_ID)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe el usuario con id " + USUARIO_ID + ". Ejecuta database.sql."));

        Movimiento movimiento = new Movimiento(
                usuario,
                categoria,
                request.tipo(),
                request.descripcion().trim(),
                request.monto(),
                request.fecha());

        return MovimientoResponse.desde(movimientoRepository.save(movimiento));
    }

    @Transactional
    public void eliminar(Long id) {
        movimientoRepository.delete(buscar(id));
    }

    @Transactional(readOnly = true)
    public ResumenResponse resumen() {
        BigDecimal ingresos = sumar(TipoMovimiento.INGRESO);
        BigDecimal gastos = sumar(TipoMovimiento.GASTO);
        return new ResumenResponse(ingresos, gastos, ingresos.subtract(gastos));
    }

    private BigDecimal sumar(TipoMovimiento tipo) {
        BigDecimal total = movimientoRepository.sumarPorTipo(USUARIO_ID, tipo);
        return total != null ? total : BigDecimal.ZERO;
    }

    private Movimiento buscar(Long id) {
        return movimientoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el movimiento con id " + id + "."));
    }
}
