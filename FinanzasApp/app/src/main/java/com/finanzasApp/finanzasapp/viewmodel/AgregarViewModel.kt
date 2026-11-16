package com.finanzasApp.finanzasapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzasApp.finanzasapp.data.model.Categoria
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

/** Estado del formulario "Agregar movimiento". */
data class AgregarUiState(
    val tipo: TipoMovimiento = TipoMovimiento.GASTO,   // siempre hay un tipo elegido
    val descripcion: String = "",
    val monto: String = "",                            // solo digitos (pesos enteros)
    val categoriaId: Long? = null,
    val fecha: String = Formato.hoy(),                 // dd/MM/yyyy
    val categorias: List<Categoria> = emptyList(),
    val cargandoCategorias: Boolean = true,
    val guardando: Boolean = false,
    val guardado: Boolean = false,
    val errorDescripcion: String? = null,
    val errorMonto: String? = null,
    val errorCategoria: String? = null,
    val errorFecha: String? = null,
    val errorGeneral: String? = null
) {
    /** Solo se ofrecen las categorias del tipo elegido (el backend lo exige). */
    val categoriasDelTipo: List<Categoria>
        get() = categorias.filter { it.tipo == tipo }
}

class AgregarViewModel : ViewModel() {

    private val repository = FinanzasRepository()

    private val _uiState = MutableStateFlow(AgregarUiState())
    val uiState: StateFlow<AgregarUiState> = _uiState.asStateFlow()

    init {
        cargarCategorias()
    }

    fun cargarCategorias() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargandoCategorias = true, errorGeneral = null) }
            when (val resultado = repository.obtenerCategorias()) {
                is Resultado.Exito ->
                    _uiState.update { it.copy(cargandoCategorias = false, categorias = resultado.datos) }
                is Resultado.Error ->
                    _uiState.update { it.copy(cargandoCategorias = false, errorGeneral = resultado.mensaje) }
            }
        }
    }

    fun cambiarTipo(tipo: TipoMovimiento) {
        // Al cambiar de tipo la categoria elegida ya no aplica
        _uiState.update { it.copy(tipo = tipo, categoriaId = null, errorCategoria = null) }
    }

    fun cambiarDescripcion(texto: String) {
        _uiState.update { it.copy(descripcion = texto, errorDescripcion = null) }
    }

    fun cambiarMonto(texto: String) {
        // Solo digitos: evita ambiguedades como "25.000" (miles) frente a "25.000" (decimal)
        val soloDigitos = texto.filter { it.isDigit() }.take(12)
        _uiState.update { it.copy(monto = soloDigitos, errorMonto = null) }
    }

    fun seleccionarCategoria(id: Long) {
        _uiState.update { it.copy(categoriaId = id, errorCategoria = null) }
    }

    fun cambiarFecha(texto: String) {
        _uiState.update { it.copy(fecha = texto, errorFecha = null) }
    }

    fun guardar() {
        val estado = _uiState.value
        if (estado.guardando) return

        // ---- Validaciones basicas ----
        val monto: BigDecimal? = estado.monto.toBigDecimalOrNull()
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

        // ---- Envio al backend ----
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
            when (val resultado = repository.crearMovimiento(request)) {
                is Resultado.Exito ->
                    _uiState.update { it.copy(guardando = false, guardado = true) }
                is Resultado.Error ->
                    _uiState.update { it.copy(guardando = false, errorGeneral = resultado.mensaje) }
            }
        }
    }
}
