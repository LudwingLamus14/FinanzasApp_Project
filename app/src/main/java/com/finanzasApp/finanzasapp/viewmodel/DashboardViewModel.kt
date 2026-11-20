package com.finanzasApp.finanzasapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.finanzasApp.finanzasapp.data.model.Movimiento
import com.finanzasApp.finanzasapp.data.model.Resumen
import com.finanzasApp.finanzasapp.data.repository.FinanzasRepository
import com.finanzasApp.finanzasapp.data.repository.Resultado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Todo lo que necesita pintar el Dashboard. */
data class DashboardUiState(
    val cargando: Boolean = true,
    val resumen: Resumen? = null,
    val recientes: List<Movimiento> = emptyList(),
    val error: String? = null
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FinanzasRepository(application)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    /** Consulta el resumen y los movimientos. La pantalla la llama cada vez que se muestra. */
    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }

            val resultadoResumen = repository.obtenerResumen()
            val resultadoMovimientos = repository.obtenerMovimientos()

            val resumen = (resultadoResumen as? Resultado.Exito)?.datos
            val movimientos = (resultadoMovimientos as? Resultado.Exito)?.datos
            val error = (resultadoResumen as? Resultado.Error)?.mensaje
                ?: (resultadoMovimientos as? Resultado.Error)?.mensaje

            _uiState.update {
                it.copy(
                    cargando = false,
                    // Si una consulta falla, se conservan los datos anteriores
                    resumen = resumen ?: it.resumen,
                    recientes = movimientos?.take(5) ?: it.recientes,
                    error = error
                )
            }
        }
    }
}
