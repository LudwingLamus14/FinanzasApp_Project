package com.finanzasApp.finanzasapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finanzasApp.finanzasapp.data.model.TipoMovimiento
import com.finanzasApp.finanzasapp.ui.components.CampoFecha
import com.finanzasApp.finanzasapp.ui.components.ConfirmarEliminarMovimiento
import com.finanzasApp.finanzasapp.ui.components.MensajeError
import com.finanzasApp.finanzasapp.ui.components.SelectorCategoria
import com.finanzasApp.finanzasapp.viewmodel.AgregarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarMovimientoScreen(
    onVolver: () -> Unit,
    viewModel: AgregarViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()

    val editando = viewModel.movimientoId != null
    var confirmarEliminacion by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = estado.ocupado) {}
    LaunchedEffect(estado.finalizado) {
        if (estado.finalizado) onVolver()
    }

    val original = estado.movimientoOriginal
    if (confirmarEliminacion && original != null && !estado.ocupado) {
        ConfirmarEliminarMovimiento(
            movimiento = original,
            onCancelar = { confirmarEliminacion = false },
            onEliminar = {
                confirmarEliminacion = false
                viewModel.eliminar()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editando) "Editar movimiento" else "Agregar movimiento") },
                navigationIcon = {
                    TextButton(onClick = onVolver, enabled = !estado.ocupado) { Text("←") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (editando && original == null) {
                if (estado.cargando) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
                    estado.errorGeneral?.let { mensaje ->
                        MensajeError(mensaje = mensaje, onReintentar = viewModel::cargarFormulario)
                    }
                }
                return@Column
            }

            // ---- Tipo ----
            Text(text = "Tipo", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BotonTipo(
                    texto = "Gasto",
                    seleccionado = estado.tipo == TipoMovimiento.GASTO,
                    onClick = { viewModel.cambiarTipo(TipoMovimiento.GASTO) },
                    modifier = Modifier.weight(1f)
                )
                BotonTipo(
                    texto = "Ingreso",
                    seleccionado = estado.tipo == TipoMovimiento.INGRESO,
                    onClick = { viewModel.cambiarTipo(TipoMovimiento.INGRESO) },
                    modifier = Modifier.weight(1f)
                )
            }

            // ---- Descripcion ----
            OutlinedTextField(
                value = estado.descripcion,
                onValueChange = viewModel::cambiarDescripcion,
                label = { Text("Descripción") },
                singleLine = true,
                isError = estado.errorDescripcion != null,
                supportingText = { estado.errorDescripcion?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            // ---- Monto ----
            OutlinedTextField(
                value = estado.monto,
                onValueChange = viewModel::cambiarMonto,
                label = {
                    Text(if (editando) "Monto (pesos)" else "Monto (pesos, sin puntos ni comas)")
                },
                prefix = { Text("$ ") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = if (editando) KeyboardType.Decimal else KeyboardType.Number),
                isError = estado.errorMonto != null,
                supportingText = { estado.errorMonto?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            if (estado.cargando) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                SelectorCategoria(
                    categorias = estado.categoriasDelTipo,
                    categoriaId = estado.categoriaId,
                    onSeleccionar = { id -> id?.let(viewModel::seleccionarCategoria) },
                    error = estado.errorCategoria
                )
                if (estado.categoriasDelTipo.isEmpty()) {
                    Text("No hay categorías para este tipo.")
                }
            }

            // ---- Fecha ----
            CampoFecha(
                valor = estado.fecha,
                onCambiar = viewModel::cambiarFecha,
                etiqueta = "Fecha (dd/MM/aaaa)",
                error = estado.errorFecha,
                modifier = Modifier.fillMaxWidth()
            )

            // ---- Error general (almacenamiento local) ----
            estado.errorGeneral?.let { mensaje ->
                MensajeError(
                    mensaje = mensaje,
                    // Si fallo la carga de categorias, se ofrece reintentar
                    onReintentar = if (estado.categorias.isEmpty()) viewModel::cargarFormulario else null
                )
            }

            // ---- Guardar ----
            Button(
                onClick = viewModel::guardar,
                enabled = !estado.ocupado && !estado.cargando && estado.categorias.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (estado.guardando) "Guardando…" else if (editando) "Guardar cambios" else "Guardar")
            }
            if (editando) {
                OutlinedButton(
                    onClick = { confirmarEliminacion = true },
                    enabled = !estado.ocupado && !estado.cargando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (estado.eliminando) "Eliminando…" else "Eliminar movimiento",
                        color = if (estado.ocupado) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/** Boton de seleccion: relleno si esta elegido, con borde si no. */
@Composable
private fun BotonTipo(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (seleccionado) {
        Button(onClick = onClick, modifier = modifier) { Text(texto) }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier) { Text(texto) }
    }
}
