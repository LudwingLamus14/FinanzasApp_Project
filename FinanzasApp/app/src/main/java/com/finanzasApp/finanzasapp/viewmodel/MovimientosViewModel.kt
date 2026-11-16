package com.finanzasApp.finanzasapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzasApp.finanzasapp.data.model.Categoria
import com.finanzasApp.finanzasapp.data.model.Movimiento
import com.finanzasApp.finanzasapp.data.model.TipoMovimiento
import com.finanzasApp.finanzasapp.data.repository.FinanzasRepository
import com.finanzasApp.finanzasapp.data.repository.Resultado
import com.finanzasApp.finanzasapp.util.Formato
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MovimientosUiState(
    val cargando: Boolean = true,
    val movimientos: List<Movimiento> = emptyList(),
    val categorias: List<Categoria> = emptyList(),
    val error: String? = null,
    // ---- Filtros (todos opcionales; null/vacio = no se aplica) ----
    val filtroTipo: TipoMovimiento? = null,
    val filtroCategoriaId: Long? = null,
    val filtroDesdeTexto: String = "",
    val filtroHastaTexto: String = ""
) {
    /** Hay al menos un filtro activo (para mostrar "Limpiar filtros" y el contador). */
    val hayFiltrosActivos: Boolean
        get() = filtroTipo != null || filtroCategoriaId != null ||
            filtroDesdeTexto.isNotBlank() || filtroHastaTexto.isNotBlank()
}

class MovimientosViewModel : ViewModel() {

    private val repository = FinanzasRepository()

    private val _uiState = MutableStateFlow(MovimientosUiState())
    val uiState: StateFlow<MovimientosUiState> = _uiState.asStateFlow()

    /** Carga categorias (para el filtro) y movimientos (ya filtrados) a la vez. */
    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }

            val estado = _uiState.value
            val desde = Formato.parseFecha(estado.filtroDesdeTexto)
            val hasta = Formato.parseFecha(estado.filtroHastaTexto)

            val resultadoMovimientos = repository.obtenerMovimientos(
                tipo = estado.filtroTipo,
                categoriaId = estado.filtroCategoriaId,
                fechaInicio = desde,
                fechaFin = hasta
            )
            // Las categorias solo hacen falta para pintar el filtro; si ya estan
            // cargadas no hace falta volver a pedirlas cada vez que se filtra.
            val resultadoCategorias =
                if (estado.categorias.isEmpty()) repository.obtenerCategorias() else null

            val movimientos = (resultadoMovimientos as? Resultado.Exito)?.datos
            val categorias = (resultadoCategorias as? Resultado.Exito)?.datos
            val error = (resultadoMovimientos as? Resultado.Error)?.mensaje
                ?: (resultadoCategorias as? Resultado.Error)?.mensaje

            _uiState.update {
                it.copy(
                    cargando = false,
                    movimientos = movimientos ?: it.movimientos,
                    categorias = categorias ?: it.categorias,
                    error = error
                )
            }
        }
    }

    fun eliminar(id: Long) {
        viewModelScope.launch {
            when (val resultado = repository.eliminarMovimiento(id)) {
                is Resultado.Exito -> cargar() // Recarga (con los filtros actuales) ya sin el movimiento
                is Resultado.Error -> _uiState.update { it.copy(error = resultado.mensaje) }
            }
        }
    }

    // ---- Filtros: cada cambio vuelve a pedir la lista al backend ----

    fun cambiarFiltroTipo(tipo: TipoMovimiento?) {
        _uiState.update { it.copy(filtroTipo = tipo) }
        cargar()
    }

    fun cambiarFiltroCategoria(categoriaId: Long?) {
        _uiState.update { it.copy(filtroCategoriaId = categoriaId) }
        cargar()
    }

    /**
     * Mientras el texto no forma una fecha completa (dd/MM/aaaa) todavia, solo
     * se guarda lo escrito y NO se recarga (para no consultar la API con una
     * fecha a medio escribir, como "15/0"). En cuanto el texto SI es una fecha
     * valida completa, se aplica de inmediato, igual que Tipo y Categoria.
     * Si se borra el campo (texto vacio), tambien se recarga, para quitar ese filtro.
     */
    fun cambiarFiltroDesde(texto: String) {
        _uiState.update { it.copy(filtroDesdeTexto = texto) }
        if (texto.isBlank() || Formato.parseFecha(texto) != null) cargar()
    }

    fun cambiarFiltroHasta(texto: String) {
        _uiState.update { it.copy(filtroHastaTexto = texto) }
        if (texto.isBlank() || Formato.parseFecha(texto) != null) cargar()
    }

    /** Boton "Aplicar fechas": por si se quiere forzar la busqueda sin cambiar el texto. */
    fun aplicarFiltroFechas() = cargar()

    fun limpiarFiltros() {
        _uiState.update {
            it.copy(
                filtroTipo = null,
                filtroCategoriaId = null,
                filtroDesdeTexto = "",
                filtroHastaTexto = ""
            )
        }
        cargar()
    }
}
