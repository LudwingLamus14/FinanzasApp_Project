package com.finanzasApp.finanzasapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.finanzasApp.finanzasapp.data.model.Estadisticas
import com.finanzasApp.finanzasapp.data.model.Periodo
import com.finanzasApp.finanzasapp.data.repository.FinanzasRepository
import com.finanzasApp.finanzasapp.data.repository.Resultado
import com.finanzasApp.finanzasapp.util.Formato
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class EstadisticasUiState(
    val periodo: Periodo = Periodo.DIA,
    val fechaTexto: String = Formato.hoy(),  // dd/MM/aaaa; cualquier dia DENTRO del periodo a ver
    val errorFecha: String? = null,
    val cargando: Boolean = true,
    val datos: Estadisticas? = null,
    val error: String? = null
) {
    /** La fecha de hoy, para saber si mostrar o no el boton "Hoy". */
    val esHoy: Boolean get() = fechaTexto == Formato.hoy()
}

class EstadisticasViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FinanzasRepository(application)

    private val _uiState = MutableStateFlow(EstadisticasUiState())
    val uiState: StateFlow<EstadisticasUiState> = _uiState.asStateFlow()

    /** Cambia el periodo elegido (Dia/Semana/Mes/Anio) y recarga con el nuevo, sin tocar la fecha. */
    fun cambiarPeriodo(periodo: Periodo) {
        if (periodo == _uiState.value.periodo) return
        _uiState.update { it.copy(periodo = periodo) }
        cargar()
    }

    /**
     * El usuario escribe directamente en el campo de fecha. Mientras el texto
     * no sea todavia una fecha completa (dd/MM/aaaa) no se recarga, para no
     * consultar la base local con una fecha a medio escribir; en cuanto SI lo es, se
     * aplica sola (el boton "Ver" queda solo como respaldo, no es obligatorio).
     */
    fun cambiarFechaTexto(texto: String) {
        _uiState.update { it.copy(fechaTexto = texto, errorFecha = null) }
        if (Formato.parseFecha(texto) != null) cargar()
    }

    /** Se llama al terminar de editar la fecha (foco perdido o tecla "Listo"): valida y recarga. */
    fun confirmarFecha() {
        if (Formato.parseFecha(_uiState.value.fechaTexto) == null) {
            _uiState.update { it.copy(errorFecha = "Fecha inválida. Usa el formato dd/MM/aaaa.") }
            return
        }
        cargar()
    }

    /** Vuelve a la fecha de hoy y recarga. */
    fun irAHoy() {
        _uiState.update { it.copy(fechaTexto = Formato.hoy(), errorFecha = null) }
        cargar()
    }

    /** Retrocede un periodo completo (un dia, una semana, un mes o un anio, segun lo elegido). */
    fun periodoAnterior() = mover(-1)

    /** Avanza un periodo completo. */
    fun periodoSiguiente() = mover(1)

    private fun mover(pasos: Long) {
        val actual = Formato.parseFecha(_uiState.value.fechaTexto) ?: LocalDate.now()
        val nueva = when (_uiState.value.periodo) {
            Periodo.DIA -> actual.plusDays(pasos)
            Periodo.SEMANA -> actual.plusWeeks(pasos)
            Periodo.MES -> actual.plusMonths(pasos)
            Periodo.ANIO -> actual.plusYears(pasos)
        }
        // nueva.toString() = "yyyy-MM-dd" (ISO); Formato.fecha lo pasa a "dd/MM/aaaa"
        _uiState.update { it.copy(fechaTexto = Formato.fecha(nueva.toString()), errorFecha = null) }
        cargar()
    }

    fun cargar() {
        val estado = _uiState.value
        val fecha = Formato.parseFecha(estado.fechaTexto)
        if (fecha == null) {
            // No hay una fecha valida: no se consulta la base local, solo se avisa.
            _uiState.update {
                it.copy(cargando = false, errorFecha = "Fecha inválida. Usa el formato dd/MM/aaaa.")
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            when (val resultado = repository.obtenerEstadisticas(estado.periodo, fecha)) {
                is Resultado.Exito ->
                    _uiState.update { it.copy(cargando = false, datos = resultado.datos) }
                is Resultado.Error ->
                    _uiState.update { it.copy(cargando = false, error = resultado.mensaje) }
            }
        }
    }
}
