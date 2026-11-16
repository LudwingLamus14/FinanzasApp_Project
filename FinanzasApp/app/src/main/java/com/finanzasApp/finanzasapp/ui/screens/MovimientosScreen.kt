package com.finanzasApp.finanzasapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finanzasApp.finanzasapp.data.model.Categoria
import com.finanzasApp.finanzasapp.data.model.Movimiento
import com.finanzasApp.finanzasapp.data.model.TipoMovimiento
import com.finanzasApp.finanzasapp.ui.components.ItemMovimiento
import com.finanzasApp.finanzasapp.ui.components.MensajeError
import com.finanzasApp.finanzasapp.util.Formato
import com.finanzasApp.finanzasapp.viewmodel.MovimientosUiState
import com.finanzasApp.finanzasapp.viewmodel.MovimientosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovimientosScreen(
    onVolver: () -> Unit,
    viewModel: MovimientosViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()
    var pendienteDeEliminar by remember { mutableStateOf<Movimiento?>(null) }
    var mostrarFiltros by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.cargar()
    }

    // Confirmacion antes de borrar (accion que no se puede deshacer)
    pendienteDeEliminar?.let { movimiento ->
        AlertDialog(
            onDismissRequest = { pendienteDeEliminar = null },
            title = { Text("Eliminar movimiento") },
            text = {
                Text(
                    "¿Eliminar «${movimiento.descripcion}» por " +
                        "${Formato.moneda(movimiento.monto)}? Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminar(movimiento.id)
                    pendienteDeEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { pendienteDeEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Movimientos") },
                navigationIcon = { TextButton(onClick = onVolver) { Text("←") } },
                actions = {
                    TextButton(onClick = { mostrarFiltros = !mostrarFiltros }) {
                        Text(
                            if (mostrarFiltros) "Ocultar filtros"
                            else if (estado.hayFiltrosActivos) "Filtros ●"
                            else "Filtrar"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (mostrarFiltros) {
                item { PanelFiltros(estado = estado, viewModel = viewModel) }
            }

            if (estado.cargando) {
                item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            }

            estado.error?.let { mensaje ->
                item { MensajeError(mensaje = mensaje, onReintentar = viewModel::cargar) }
            }

            if (estado.movimientos.isEmpty() && !estado.cargando && estado.error == null) {
                item {
                    Text(
                        if (estado.hayFiltrosActivos) "Ningún movimiento coincide con los filtros."
                        else "No hay movimientos registrados."
                    )
                }
            }

            items(estado.movimientos, key = { it.id }) { movimiento ->
                ItemMovimiento(
                    movimiento = movimiento,
                    onEliminar = { pendienteDeEliminar = movimiento }
                )
            }
        }
    }
}

/**
 * Panel de filtros: tipo (Todos/Ingreso/Gasto), categoria (lista de radio-botones,
 * igual que en "Agregar movimiento") y rango de fechas (dos campos de texto).
 * Cada filtro se aplica apenas se elige, salvo las fechas, que esperan al boton "Aplicar".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PanelFiltros(estado: MovimientosUiState, viewModel: MovimientosViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Filtros", style = MaterialTheme.typography.titleMedium)

            // ---- Tipo ----
            Text(text = "Tipo", style = MaterialTheme.typography.labelLarge)
            val opcionesTipo = listOf<Pair<TipoMovimiento?, String>>(
                null to "Todos",
                TipoMovimiento.INGRESO to "Ingreso",
                TipoMovimiento.GASTO to "Gasto"
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                opcionesTipo.forEachIndexed { index, (tipo, etiqueta) ->
                    SegmentedButton(
                        selected = estado.filtroTipo == tipo,
                        onClick = { viewModel.cambiarFiltroTipo(tipo) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = opcionesTipo.size)
                    ) { Text(etiqueta) }
                }
            }

            // ---- Categoria ----
            Text(text = "Categoría", style = MaterialTheme.typography.labelLarge)
            Column {
                FilaFiltroCategoria(
                    nombre = "Todas",
                    seleccionada = estado.filtroCategoriaId == null,
                    onClick = { viewModel.cambiarFiltroCategoria(null) }
                )
                estado.categorias.forEach { categoria: Categoria ->
                    FilaFiltroCategoria(
                        nombre = categoria.nombre,
                        seleccionada = estado.filtroCategoriaId == categoria.id,
                        onClick = { viewModel.cambiarFiltroCategoria(categoria.id) }
                    )
                }
            }

            // ---- Rango de fechas ----
            Text(text = "Rango de fechas", style = MaterialTheme.typography.labelLarge)
            Text(
                text = "Se aplica apenas completes una fecha (dd/MM/aaaa).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = estado.filtroDesdeTexto,
                    onValueChange = viewModel::cambiarFiltroDesde,
                    label = { Text("Desde") },
                    placeholder = { Text("dd/MM/aaaa") },
                    singleLine = true,
                    isError = estado.filtroDesdeTexto.isNotBlank() &&
                        Formato.parseFecha(estado.filtroDesdeTexto) == null,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = estado.filtroHastaTexto,
                    onValueChange = viewModel::cambiarFiltroHasta,
                    label = { Text("Hasta") },
                    placeholder = { Text("dd/MM/aaaa") },
                    singleLine = true,
                    isError = estado.filtroHastaTexto.isNotBlank() &&
                        Formato.parseFecha(estado.filtroHastaTexto) == null,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = viewModel::aplicarFiltroFechas) { Text("Aplicar fechas") }
                if (estado.hayFiltrosActivos) {
                    TextButton(onClick = viewModel::limpiarFiltros) { Text("Limpiar filtros") }
                }
            }
        }
    }
}

@Composable
private fun FilaFiltroCategoria(nombre: String, seleccionada: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = seleccionada, onClick = onClick, role = Role.RadioButton),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = seleccionada, onClick = null)
        Text(text = nombre, modifier = Modifier.padding(start = 8.dp))
    }
}
