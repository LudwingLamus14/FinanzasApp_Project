package com.finanzasapp.finanzasapp_backend.service;

import com.finanzasapp.finanzasapp_backend.dto.CategoriaGastoResponse;
import com.finanzasapp.finanzasapp_backend.dto.EstadisticasResponse;
import com.finanzasapp.finanzasapp_backend.entity.PeriodoEstadistica;
import com.finanzasapp.finanzasapp_backend.entity.TipoMovimiento;
import com.finanzasapp.finanzasapp_backend.repository.MovimientoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Calcula los datos de la pantalla "Estadisticas": gasto total, promedio diario,
 * gastos por categoria, ingresos vs gastos y la categoria con mayor gasto,
 * todo dentro de un periodo (dia, semana o anio).
 */
@Service
public class EstadisticasService {

    // Mismo usuario fijo que usa MovimientoService (la app no tiene login).
    private static final Long USUARIO_ID = 1L;

    private final MovimientoRepository movimientoRepository;

    public EstadisticasService(MovimientoRepository movimientoRepository) {
        this.movimientoRepository = movimientoRepository;
    }

    @Transactional(readOnly = true)
    public EstadisticasResponse obtener(PeriodoEstadistica periodo) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicio;
        LocalDate fin;

        // Periodos de calendario completos (semana de lunes a domingo, mes completo,
        // anio de enero a diciembre), no "ultimos N dias": asi el promedio se divide
        // siempre entre un numero fijo de dias y es facil de explicar.
        switch (periodo) {
            case DIA -> {
                inicio = hoy;
                fin = hoy;
            }
            case SEMANA -> {
                inicio = hoy.with(DayOfWeek.MONDAY);
                fin = inicio.plusDays(6);
            }
            case MES -> {
                inicio = hoy.withDayOfMonth(1);
                fin = hoy.withDayOfMonth(hoy.lengthOfMonth());
            }
            default -> { // ANIO
                inicio = hoy.withDayOfYear(1);
                fin = LocalDate.of(hoy.getYear(), 12, 31);
            }
        }

        long dias = ChronoUnit.DAYS.between(inicio, fin) + 1;

        BigDecimal totalGastos = sumar(TipoMovimiento.GASTO, inicio, fin);
        BigDecimal totalIngresos = sumar(TipoMovimiento.INGRESO, inicio, fin);
        BigDecimal promedioGastoDiario = totalGastos.divide(BigDecimal.valueOf(dias), 2, RoundingMode.HALF_UP);

        List<CategoriaGastoResponse> gastosPorCategoria = movimientoRepository
                .gastosPorCategoriaEntreFechas(USUARIO_ID, TipoMovimiento.GASTO, inicio, fin);

        CategoriaGastoResponse categoriaMayorGasto =
                gastosPorCategoria.isEmpty() ? null : gastosPorCategoria.get(0);

        return new EstadisticasResponse(
                periodo.name(),
                inicio,
                fin,
                totalIngresos,
                totalGastos,
                promedioGastoDiario,
                gastosPorCategoria,
                categoriaMayorGasto);
    }

    private BigDecimal sumar(TipoMovimiento tipo, LocalDate inicio, LocalDate fin) {
        BigDecimal total = movimientoRepository.sumarPorTipoEntreFechas(USUARIO_ID, tipo, inicio, fin);
        return total != null ? total : BigDecimal.ZERO;
    }
}
