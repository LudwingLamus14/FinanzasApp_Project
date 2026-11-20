package com.finanzasApp.finanzasapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.finanzasApp.finanzasapp.data.model.Movimiento
import com.finanzasApp.finanzasapp.data.model.TipoMovimiento
import com.finanzasApp.finanzasapp.util.Formato

/**
 * Una fila de movimiento: categoria, descripcion, tipo, fecha y monto con signo.
 * Si se pasa onEliminar, muestra el boton "Eliminar".
 */
@Composable
fun ItemMovimiento(
    movimiento: Movimiento,
    modifier: Modifier = Modifier,
    onEditar: () -> Unit,
    onEliminar: (() -> Unit)? = null
) {
    val colorMonto =
        if (movimiento.tipo == TipoMovimiento.INGRESO) colorIngreso() else colorGasto()
    val tipoTexto = movimiento.tipo.name.lowercase().replaceFirstChar { it.uppercase() }

    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movimiento.categoriaNombre,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(text = movimiento.descripcion, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "${Formato.fecha(movimiento.fecha)} · $tipoTexto",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = Formato.montoConSigno(movimiento.tipo, movimiento.monto),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorMonto
                )
                TextButton(onClick = onEditar) { Text("Editar") }
                if (onEliminar != null) {
                    TextButton(onClick = onEliminar) {
                        Text(text = "Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
