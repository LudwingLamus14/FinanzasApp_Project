package com.finanzasApp.finanzasapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.finanzasApp.finanzasapp.data.model.Categoria
import com.finanzasApp.finanzasapp.data.model.Movimiento
import com.finanzasApp.finanzasapp.data.model.MovimientoRequest
import com.finanzasApp.finanzasapp.data.model.TipoMovimiento
import com.finanzasApp.finanzasapp.data.repository.FinanzasRepository
import com.finanzasApp.finanzasapp.data.repository.Resultado
import com.finanzasApp.finanzasapp.util.Formato
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

/** Estado del formulario de movimientos. */
data class AgregarUiState(
    val tipo: TipoMovimiento = TipoMovimiento.GASTO,   // siempre hay un tipo elegido
    val descripcion: String = "",
    val monto: String = "",
    val categoriaId: Long? = null,
    val fecha: String = Formato.hoy(),                 // dd/MM/yyyy
    val categorias: List<Categoria> = emptyList(),
    val cargando: Boolean = true,
    val movimientoOriginal: Movimiento? = null,
    val guardando: Boolean = false,
    val eliminando: Boolean = false,
    val finalizado: Boolean = false,
    val errorDescripcion: String? = null,
    val errorMonto: String? = null,
    val errorCategoria: String? = null,
    val errorFecha: String? = null,
    val errorGeneral: String? = null
) {
    val ocupado: Boolean get() = guardando || eliminando || finalizado

    /** Solo se ofrecen las categorias del tipo elegido (la categoría debe coincidir). */
    val categoriasDelTipo: List<Categoria>
        get() = categorias.filter { it.tipo == tipo }
}

class AgregarViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    val movimientoId = savedStateHandle.get<Long>("movimientoId")
    private val formatoMontoEdicion = Regex("[0-9]{0,12}([.,][0-9]{0,2})?")
    private val repository = FinanzasRepository(application)

    private val _uiState = MutableStateFlow(AgregarUiState())
    val uiState: StateFlow<AgregarUiState> = _uiState.asStateFlow()

    init {
        cargarFormulario()
    }

    fun cargarFormulario() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, errorGeneral = null) }
            if (movimientoId != null && _uiState.value.movimientoOriginal == null) {
                when (val resultado = repository.obtenerMovimiento(movimientoId)) {
                    is Resultado.Error -> {
                        _uiState.update { it.copy(cargando = false, errorGeneral = resultado.mensaje) }
                        return@launch
                    }
                    is Resultado.Exito -> {
                        val movimiento = resultado.datos
                        _uiState.update {
                            it.copy(
                                movimientoOriginal = movimiento,
                                tipo = movimiento.tipo,
                                descripcion = movimiento.descripcion,
                                monto = movimiento.monto.stripTrailingZeros().toPlainString(),
                                categoriaId = movimiento.categoriaId,
                                fecha = Formato.fecha(movimiento.fecha)
                            )
                        }
                    }
                }
            }
            when (val resultado = repository.obtenerCategorias()) {
                is Resultado.Exito ->
                    _uiState.update { it.copy(cargando = false, categorias = resultado.datos) }
                is Resultado.Error ->
                    _uiState.update { it.copy(cargando = false, errorGeneral = resultado.mensaje) }
            }
        }
    }

    fun cambiarTipo(tipo: TipoMovimiento) {
        // Al cambiar de tipo la categoria elegida ya no aplica
        _uiState.update {
            if (it.tipo == tipo) it else it.copy(tipo = tipo, categoriaId = null, errorCategoria = null)
        }
    }

    fun cambiarDescripcion(texto: String) {
        _uiState.update { it.copy(descripcion = texto, errorDescripcion = null) }
    }

    fun cambiarMonto(texto: String) {
        val monto = if (movimientoId == null) {
            texto.filter { it.isDigit() }.take(12)
        } else {
            if (!formatoMontoEdicion.matches(texto)) return
            texto
        }
        _uiState.update { it.copy(monto = monto, errorMonto = null) }
    }

    fun seleccionarCategoria(id: Long) {
        _uiState.update { it.copy(categoriaId = id, errorCategoria = null) }
    }

    fun cambiarFecha(texto: String) {
        _uiState.update { it.copy(fecha = texto, errorFecha = null) }
    }

    fun guardar() {
        val estado = _uiState.value
        if (estado.ocupado || estado.cargando || estado.categorias.isEmpty()) return
        if (movimientoId != null && estado.movimientoOriginal == null) return

        // ---- Validaciones basicas ----
        val monto: BigDecimal? = estado.monto.replace(',', '.').toBigDecimalOrNull()
        val fecha = Formato.parseFecha(estado.fecha)
        val categoriaId = estado.categoriaId

        val errorDescripcion =
            if (estado.descripcion.isBlank()) "La descripción es obligatoria." else null
        val errorMonto = when {
            estado.monto.isBlank() -> "El monto es obligatorio."
            monto == null || monto <= BigDecimal.ZERO -> "El monto debe ser mayor que 0."
            else -> null
        }
        val errorCategoria = if (categoriaId == null) "Selecciona una categoría." else null
        val errorFecha =
            if (fecha == null) "Fecha inválida. Usa el formato dd/MM/aaaa (ej. 19/09/2026)." else null

        if (errorDescripcion != null || errorMonto != null || errorCategoria != null ||
            errorFecha != null || monto == null || fecha == null || categoriaId == null
        ) {
            _uiState.update {
                it.copy(
                    errorDescripcion = errorDescripcion,
                    errorMonto = errorMonto,
                    errorCategoria = errorCategoria,
                    errorFecha = errorFecha,
                    errorGeneral = null
                )
            }
            return
        }

        // ---- Guardado en SQLite ----
        _uiState.update {
            it.copy(
                guardando = true,
                errorDescripcion = null,
                errorMonto = null,
                errorCategoria = null,
                errorFecha = null,
                errorGeneral = null
            )
        }

        val request = MovimientoRequest(
            tipo = estado.tipo,
            descripcion = estado.descripcion.trim(),
            monto = monto,
            categoriaId = categoriaId,
            fecha = fecha.toString() // LocalDate.toString() = "yyyy-MM-dd"
        )

        viewModelScope.launch {
            val resultado = if (movimientoId == null) repository.crearMovimiento(request)
                else repository.actualizarMovimiento(movimientoId, request)
            when (resultado) {
                is Resultado.Exito ->
                    _uiState.update { it.copy(guardando = false, finalizado = true) }
                is Resultado.Error ->
                    _uiState.update { it.copy(guardando = false, errorGeneral = resultado.mensaje) }
            }
        }
    }

    fun eliminar() {
        val id = movimientoId ?: return
        val estado = _uiState.value
        if (estado.ocupado || estado.cargando || estado.movimientoOriginal == null) return

        _uiState.update { it.copy(eliminando = true, errorGeneral = null) }
        viewModelScope.launch {
            when (val resultado = repository.eliminarMovimiento(id)) {
                is Resultado.Exito ->
                    _uiState.update { it.copy(eliminando = false, finalizado = true) }
                is Resultado.Error ->
                    _uiState.update { it.copy(eliminando = false, errorGeneral = resultado.mensaje) }
            }
        }
    }
}
