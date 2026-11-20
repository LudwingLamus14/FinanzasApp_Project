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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finanzasApp.finanzasapp.data.model.CategoriaGasto
import com.finanzasApp.finanzasapp.data.model.Periodo
import com.finanzasApp.finanzasapp.ui.components.CampoFecha
import com.finanzasApp.finanzasapp.ui.components.GraficoIngresosGastos
import com.finanzasApp.finanzasapp.ui.components.MensajeError
import com.finanzasApp.finanzasapp.util.Formato
import com.finanzasApp.finanzasapp.viewmodel.EstadisticasViewModel
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasScreen(
    onVolver: () -> Unit,
    viewModel: EstadisticasViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargar()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas") },
                navigationIcon = { TextButton(onClick = onVolver) { Text("←") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SelectorPeriodo(periodoActual = estado.periodo, onCambiar = viewModel::cambiarPeriodo)
            }

            item {
                SelectorFecha(
                    fechaTexto = estado.fechaTexto,
                    errorFecha = estado.errorFecha,
                    esHoy = estado.esHoy,
                    onCambiarTexto = viewModel::cambiarFechaTexto,
                    onConfirmar = viewModel::confirmarFecha,
                    onAnterior = viewModel::periodoAnterior,
                    onSiguiente = viewModel::periodoSiguiente,
                    onHoy = viewModel::irAHoy
                )
            }

            if (estado.cargando) {
                item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            }

            estado.error?.let { mensaje ->
                item { MensajeError(mensaje = mensaje, onReintentar = viewModel::cargar) }
            }

            estado.datos?.let { datos ->
                // ---- Gasto total y promedio diario del periodo ----
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Gasto total del periodo",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = Formato.moneda(datos.totalGastos),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${Formato.fecha(datos.fechaInicio)} – ${Formato.fecha(datos.fechaFin)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Promedio diario: ${Formato.moneda(datos.promedioGastoDiario)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // ---- Grafico ingresos vs gastos ----
                item {
                    Text(text = "Ingresos vs. gastos", style = MaterialTheme.typography.titleMedium)
                }
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        GraficoIngresosGastos(
                            ingresos = datos.totalIngresos,
                            gastos = datos.totalGastos,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }

                // ---- Categoria con mayor gasto ----
                datos.categoriaMayorGasto?.let { categoria ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Categoría con mayor gasto",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = categoria.categoriaNombre,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = Formato.moneda(categoria.total),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // ---- Gastos por categoria ----
                item {
                    Text(text = "Gastos por categoría", style = MaterialTheme.typography.titleMedium)
                }

                if (datos.gastosPorCategoria.isEmpty()) {
                    item { Text("No hay gastos registrados en este periodo.") }
                }

                items(datos.gastosPorCategoria, key = { it.categoriaId }) { categoria ->
                    FilaCategoriaGasto(categoria = categoria)
                }
            }
        }
    }
}

/** Botones "Día / Semana / Mes / Año" que se comportan como un unico selector. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorPeriodo(periodoActual: Periodo, onCambiar: (Periodo) -> Unit) {
    val opciones = listOf(
        Periodo.DIA to "Día",
        Periodo.SEMANA to "Semana",
        Periodo.MES to "Mes",
        Periodo.ANIO to "Año"
    )
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        opciones.forEachIndexed { index, (periodo, etiqueta) ->
            SegmentedButton(
                selected = periodo == periodoActual,
                onClick = { onCambiar(periodo) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = opciones.size)
            ) {
                Text(etiqueta)
            }
        }
    }
}

/**
 * Fila "‹  [fecha editable]  ›" para navegar al dia/semana/mes/año anterior o
 * siguiente, mas los botones "Ver" (confirma lo escrito a mano) y "Hoy".
 * "fechaTexto" es cualquier dia DENTRO del periodo que se quiere ver: para
 * Semana/Mes/Año no hace falta escribir el primer dia exacto del periodo.
 */
@Composable
private fun SelectorFecha(
    fechaTexto: String,
    errorFecha: String?,
    esHoy: Boolean,
    onCambiarTexto: (String) -> Unit,
    onConfirmar: () -> Unit,
    onAnterior: () -> Unit,
    onSiguiente: () -> Unit,
    onHoy: () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onAnterior) {
                Text("‹", style = MaterialTheme.typography.headlineSmall)
            }
            CampoFecha(
                valor = fechaTexto,
                onCambiar = onCambiarTexto,
                etiqueta = "Ver fecha",
                error = errorFecha,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onSiguiente) {
                Text("›", style = MaterialTheme.typography.headlineSmall)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(onClick = onConfirmar) { Text("Ver") }
            if (!esHoy) {
                TextButton(onClick = onHoy) { Text("Hoy") }
            }
        }
    }
}

@Composable
private fun FilaCategoriaGasto(categoria: CategoriaGasto, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = categoria.categoriaNombre, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = Formato.moneda(categoria.total),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
