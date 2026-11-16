package com.finanzasApp.finanzasapp.data

import android.content.Context
import com.finanzasApp.finanzasapp.data.local.DatabaseHelper
import com.finanzasApp.finanzasapp.data.model.*
import com.finanzasApp.finanzasapp.data.repository.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.SQLiteMode
import java.math.BigDecimal
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@SQLiteMode(SQLiteMode.Mode.LEGACY)
class FinanzasSqliteTest {
    private lateinit var context: Context
    private lateinit var db: DatabaseHelper
    private lateinit var repository: FinanzasRepository

    @Before fun preparar() {
        context = RuntimeEnvironment.getApplication()
        context.deleteDatabase("prueba-finanzas.db")
        db = DatabaseHelper(context, "prueba-finanzas.db")
        repository = FinanzasRepository(db)
    }

    @After fun cerrar() {
        db.close()
        context.deleteDatabase("prueba-finanzas.db")
    }

    private fun agregar(tipo: TipoMovimiento, monto: String, fecha: String, otraCategoria: Boolean = false): Movimiento {
        val categorias = db.obtenerCategorias().filter { it.tipo == tipo }
        return db.crearMovimiento(MovimientoRequest(tipo, "Prueba ' SQLite", BigDecimal(monto),
            (if (otraCategoria) categorias.last() else categorias.first()).id, fecha))
    }

    private fun <T> exito(resultado: Resultado<T>): T {
        assertTrue(resultado.toString(), resultado is Resultado.Exito)
        return (resultado as Resultado.Exito).datos
    }

    @Test fun conservaDatosAlReabrirSinDuplicarCategorias() {
        val categorias = db.obtenerCategorias()
        val m = agregar(TipoMovimiento.GASTO, "0.10", "2024-02-29")
        db.close()
        db = DatabaseHelper(context, "prueba-finanzas.db")
        assertEquals(categorias, db.obtenerCategorias())
        assertEquals(listOf(m), db.obtenerMovimientos())
    }

    @Test fun resumenExactoYEliminacionPersistente() = runBlocking {
        agregar(TipoMovimiento.INGRESO, "1.00", "2024-02-29")
        agregar(TipoMovimiento.GASTO, "0.10", "2024-02-29")
        val m = agregar(TipoMovimiento.GASTO, "0.20", "2024-02-29")
        assertEquals(BigDecimal("0.70"), exito(repository.obtenerResumen()).saldo)
        exito(repository.eliminarMovimiento(m.id))
        assertEquals(BigDecimal("0.90"), exito(repository.obtenerResumen()).saldo)
        assertTrue(repository.eliminarMovimiento(m.id) is Resultado.Error)
    }

    @Test fun filtraPorTipoCategoriaYFechasInclusivasEnOrdenDescendente() {
        agregar(TipoMovimiento.INGRESO, "100", "2024-03-01")
        agregar(TipoMovimiento.GASTO, "10", "2024-02-28")
        val a = agregar(TipoMovimiento.GASTO, "20", "2024-02-29")
        val b = agregar(TipoMovimiento.GASTO, "30", "2024-02-29")
        agregar(TipoMovimiento.GASTO, "40", "2024-03-01", true)
        val lista = db.obtenerMovimientos(TipoMovimiento.GASTO, a.categoriaId,
            LocalDate.parse("2024-02-29"), LocalDate.parse("2024-03-01"))
        assertEquals(listOf(b.id, a.id), lista.map { it.id })
    }

    @Test fun estadisticasDelMesBisiestoUsanLaFechaElegida() = runBlocking {
        agregar(TipoMovimiento.GASTO, "29", "2024-02-29")
        agregar(TipoMovimiento.GASTO, "58", "2024-02-01", true)
        agregar(TipoMovimiento.INGRESO, "100", "2024-02-10")
        agregar(TipoMovimiento.GASTO, "999", "2024-03-01")
        val stats = exito(repository.obtenerEstadisticas(Periodo.MES, LocalDate.parse("2024-02-15")))
        assertEquals("2024-02-01", stats.fechaInicio)
        assertEquals("2024-02-29", stats.fechaFin)
        assertEquals(BigDecimal("87.00"), stats.totalGastos)
        assertEquals(BigDecimal("3.00"), stats.promedioGastoDiario)
        assertEquals(BigDecimal("100.00"), stats.totalIngresos)
        assertEquals(BigDecimal("58.00"), stats.categoriaMayorGasto?.total)
    }

    @Test fun periodosDeCalendarioYBaseVacia() = runBlocking {
        val fecha = LocalDate.parse("2024-12-31")
        val semana = exito(repository.obtenerEstadisticas(Periodo.SEMANA, fecha))
        assertEquals("2024-12-30", semana.fechaInicio)
        assertEquals("2025-01-05", semana.fechaFin)
        val anio = exito(repository.obtenerEstadisticas(Periodo.ANIO, fecha))
        assertEquals("2024-01-01", anio.fechaInicio)
        assertEquals("2024-12-31", anio.fechaFin)
        assertEquals(BigDecimal("0.00"), anio.promedioGastoDiario)
        assertTrue(anio.gastosPorCategoria.isEmpty())
        assertNull(anio.categoriaMayorGasto)
        val dia = exito(repository.obtenerEstadisticas(Periodo.DIA, fecha))
        assertEquals(dia.fechaInicio, dia.fechaFin)
    }

    @Test fun rechazaDatosInvalidosSinInsertarFilas() = runBlocking {
        val ingreso = db.obtenerCategorias().first { it.tipo == TipoMovimiento.INGRESO }
        val valido = MovimientoRequest(TipoMovimiento.INGRESO, "Ingreso", BigDecimal("10"), ingreso.id, "2024-02-29")
        listOf(valido.copy(monto = BigDecimal("0")), valido.copy(monto = BigDecimal("-1")),
            valido.copy(monto = BigDecimal("1.001")), valido.copy(monto = BigDecimal("1000000000000")),
            valido.copy(categoriaId = -1), valido.copy(tipo = TipoMovimiento.GASTO),
            valido.copy(descripcion = " "), valido.copy(descripcion = "a".repeat(201)),
            valido.copy(fecha = "2024-02-30")
        ).forEach { assertTrue(repository.crearMovimiento(it) is Resultado.Error) }
        assertTrue(db.obtenerMovimientos().isEmpty())
        assertTrue(repository.obtenerMovimientos(fechaInicio = LocalDate.parse("2024-03-01"),
            fechaFin = LocalDate.parse("2024-02-01")) is Resultado.Error)
    }
}
