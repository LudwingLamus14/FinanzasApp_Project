package com.finanzasApp.finanzasapp.ui.screens

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finanzasApp.finanzasapp.data.model.TipoMovimiento
import com.finanzasApp.finanzasapp.ui.components.CampoFecha
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

    // Cuando SQLite confirma el guardado, se regresa al Dashboard
    LaunchedEffect(estado.guardado) {
        if (estado.guardado) onVolver()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar movimiento") },
                navigationIcon = { TextButton(onClick = onVolver) { Text("←") } }
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

            // ---- Monto (solo pesos enteros) ----
            OutlinedTextField(
                value = estado.monto,
                onValueChange = viewModel::cambiarMonto,
                label = { Text("Monto (pesos, sin puntos ni comas)") },
                prefix = { Text("$ ") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = estado.errorMonto != null,
                supportingText = { estado.errorMonto?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            if (estado.cargandoCategorias) {
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
                    onReintentar = if (estado.categorias.isEmpty()) viewModel::cargarCategorias else null
                )
            }

            // ---- Guardar ----
            Button(
                onClick = viewModel::guardar,
                enabled = !estado.guardando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (estado.guardando) "Guardando…" else "Guardar")
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
