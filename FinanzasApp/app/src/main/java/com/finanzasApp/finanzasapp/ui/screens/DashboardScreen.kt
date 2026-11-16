package com.finanzasApp.finanzasapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finanzasApp.finanzasapp.ui.components.ItemMovimiento
import com.finanzasApp.finanzasapp.ui.components.MensajeError
import com.finanzasApp.finanzasapp.ui.components.TarjetaResumen
import com.finanzasApp.finanzasapp.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAgregar: () -> Unit,
    onVerTodos: () -> Unit,
    onEstadisticas: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()

    // Se ejecuta cada vez que la pantalla aparece (al abrir la app y al volver de "Agregar")
    LaunchedEffect(Unit) {
        viewModel.cargar()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("FinanzasApp") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAgregar) {
                Text("+ Agregar")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            // start, top, end, bottom (bottom grande para que el boton no tape el ultimo item)
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (estado.cargando) {
                item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            }

            estado.error?.let { mensaje ->
                item { MensajeError(mensaje = mensaje, onReintentar = viewModel::cargar) }
            }

            estado.resumen?.let { resumen ->
                item { TarjetaResumen(resumen = resumen) }
            }

            item {
                Text(
                    text = "Movimientos recientes",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (estado.recientes.isEmpty() && !estado.cargando && estado.error == null) {
                item { Text("Aún no hay movimientos. Pulsa «+ Agregar» para crear el primero.") }
            }

            items(estado.recientes, key = { it.id }) { movimiento ->
                ItemMovimiento(movimiento = movimiento)
            }

        }
    }
}
