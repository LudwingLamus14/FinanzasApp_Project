package com.finanzasApp.finanzasapp.data.repository

import android.content.Context
import com.finanzasApp.finanzasapp.data.local.DatabaseHelper
import com.finanzasApp.finanzasapp.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.coroutines.cancellation.CancellationException

/** Acceso local a SQLite fuera del hilo de interfaz; no necesita servidor ni conexión. */
class FinanzasRepository(private val database: DatabaseHelper) {
    constructor(context: Context) : this(DatabaseHelper.getInstance(context))

    suspend fun obtenerResumen(): Resultado<Resumen> = ejecutar("No se pudo cargar el resumen.") {
        val movimientos = database.obtenerMovimientos()
        val ingresos = sumar(movimientos, TipoMovimiento.INGRESO)
        val gastos = sumar(movimientos, TipoMovimiento.GASTO)
        Resumen(ingresos, gastos, ingresos - gastos)
    }

    suspend fun obtenerMovimientos(
        tipo: TipoMovimiento? = null, categoriaId: Long? = null,
        fechaInicio: LocalDate? = null, fechaFin: LocalDate? = null
    ): Resultado<List<Movimiento>> = ejecutar("No se pudieron cargar los movimientos.") {
        database.obtenerMovimientos(tipo, categoriaId, fechaInicio, fechaFin)
    }

    suspend fun obtenerMovimiento(id: Long): Resultado<Movimiento> =
        ejecutar("No se pudo cargar el movimiento.") {
            requireNotNull(database.obtenerMovimientos(id = id).singleOrNull()) {
                "El movimiento no existe (quizá ya fue eliminado)."
            }
        }

    suspend fun obtenerCategorias(): Resultado<List<Categoria>> =
        ejecutar("No se pudieron cargar las categorías.") { database.obtenerCategorias() }

    suspend fun crearMovimiento(request: MovimientoRequest): Resultado<Movimiento> =
        ejecutar("No se pudo guardar el movimiento.") { database.crearMovimiento(request) }

    suspend fun actualizarMovimiento(id: Long, request: MovimientoRequest): Resultado<Movimiento> =
        ejecutar("No se pudo guardar el movimiento.") { database.actualizarMovimiento(id, request) }

    suspend fun eliminarMovimiento(id: Long): Resultado<Unit> =
        ejecutar("No se pudo eliminar el movimiento.") { database.eliminarMovimiento(id) }

    suspend fun obtenerEstadisticas(periodo: Periodo, fecha: LocalDate? = null): Resultado<Estadisticas> =
        ejecutar("No se pudieron cargar las estadísticas.") {
            val dia = fecha ?: LocalDate.now()
            val inicio = when (periodo) {
                Periodo.DIA -> dia
                Periodo.SEMANA -> dia.with(DayOfWeek.MONDAY)
                Periodo.MES -> dia.withDayOfMonth(1)
                Periodo.ANIO -> dia.withDayOfYear(1)
            }
            val fin = when (periodo) {
                Periodo.DIA -> inicio
                Periodo.SEMANA -> inicio.plusDays(6)
                Periodo.MES -> inicio.plusMonths(1).minusDays(1)
                Periodo.ANIO -> inicio.plusYears(1).minusDays(1)
            }
            val movimientos = database.obtenerMovimientos(fechaInicio = inicio, fechaFin = fin)
            val gastos = sumar(movimientos, TipoMovimiento.GASTO)
            val porCategoria = movimientos.filter { it.tipo == TipoMovimiento.GASTO }
                .groupBy { it.categoriaId }.map { (id, registros) ->
                    CategoriaGasto(id, registros.first().categoriaNombre, sumar(registros, TipoMovimiento.GASTO))
                }.sortedWith(compareByDescending<CategoriaGasto> { it.total }.thenBy { it.categoriaId })
            Estadisticas(periodo.name, inicio.toString(), fin.toString(),
                sumar(movimientos, TipoMovimiento.INGRESO), gastos,
                gastos.divide(BigDecimal.valueOf(ChronoUnit.DAYS.between(inicio, fin) + 1), 2, RoundingMode.HALF_UP),
                porCategoria, porCategoria.firstOrNull())
        }

    private fun sumar(movimientos: List<Movimiento>, tipo: TipoMovimiento): BigDecimal =
        movimientos.filter { it.tipo == tipo }.fold(BigDecimal("0.00")) { suma, m -> suma + m.monto }

    private suspend fun <T> ejecutar(mensaje: String, bloque: () -> T): Resultado<T> =
        withContext(Dispatchers.IO) {
            try { Resultado.Exito(bloque()) }
            catch (e: CancellationException) { throw e }
            catch (e: IllegalArgumentException) { Resultado.Error(e.message ?: mensaje) }
            catch (_: Exception) { Resultado.Error(mensaje) }
        }
}
