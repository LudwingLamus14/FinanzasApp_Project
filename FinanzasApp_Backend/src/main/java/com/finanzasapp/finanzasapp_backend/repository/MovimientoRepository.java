package com.finanzasapp.finanzasapp_backend.repository;

import com.finanzasapp.finanzasapp_backend.dto.CategoriaGastoResponse;
import com.finanzasapp.finanzasapp_backend.entity.Movimiento;
import com.finanzasapp.finanzasapp_backend.entity.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    /** Movimientos del usuario, los mas recientes primero. "join fetch" trae la categoria en la misma consulta. */
    @Query("select m from Movimiento m join fetch m.categoria "
            + "where m.usuario.id = :usuarioId "
            + "order by m.fecha desc, m.id desc")
    List<Movimiento> listarPorUsuario(@Param("usuarioId") Long usuarioId);

    /** Suma de montos de un tipo. Devuelve null si no hay movimientos de ese tipo. */
    @Query("select sum(m.monto) from Movimiento m "
            + "where m.usuario.id = :usuarioId and m.tipo = :tipo")
    BigDecimal sumarPorTipo(@Param("usuarioId") Long usuarioId,
                            @Param("tipo") TipoMovimiento tipo);

    /** Igual que sumarPorTipo, pero solo entre dos fechas (incluidas). Usada por /api/estadisticas. */
    @Query("select sum(m.monto) from Movimiento m "
            + "where m.usuario.id = :usuarioId and m.tipo = :tipo "
            + "and m.fecha between :inicio and :fin")
    BigDecimal sumarPorTipoEntreFechas(@Param("usuarioId") Long usuarioId,
                                       @Param("tipo") TipoMovimiento tipo,
                                       @Param("inicio") LocalDate inicio,
                                       @Param("fin") LocalDate fin);

    /**
     * Gasto total por cada categoria dentro de un rango de fechas, de mayor a menor.
     * El "select new" arma el DTO directamente desde la consulta (constructor expression de JPQL).
     */
    @Query("select new com.finanzasapp.finanzasapp_backend.dto.CategoriaGastoResponse("
            + "c.id, c.nombre, sum(m.monto)) "
            + "from Movimiento m join m.categoria c "
            + "where m.usuario.id = :usuarioId and m.tipo = :tipo "
            + "and m.fecha between :inicio and :fin "
            + "group by c.id, c.nombre "
            + "order by sum(m.monto) desc")
    List<CategoriaGastoResponse> gastosPorCategoriaEntreFechas(@Param("usuarioId") Long usuarioId,
                                                                @Param("tipo") TipoMovimiento tipo,
                                                                @Param("inicio") LocalDate inicio,
                                                                @Param("fin") LocalDate fin);
}
