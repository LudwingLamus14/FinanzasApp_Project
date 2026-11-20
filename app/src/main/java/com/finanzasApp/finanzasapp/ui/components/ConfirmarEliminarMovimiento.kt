package com.finanzasApp.finanzasapp.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.finanzasApp.finanzasapp.data.model.Movimiento
import com.finanzasApp.finanzasapp.util.Formato

@Composable
fun ConfirmarEliminarMovimiento(
    movimiento: Movimiento,
    onCancelar: () -> Unit,
    onEliminar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Eliminar movimiento") },
        text = {
            Text(
                "¿Eliminar «${movimiento.descripcion}» por " +
                    "${Formato.moneda(movimiento.monto)}? Esta acción no se puede deshacer."
            )
        },
        confirmButton = { TextButton(onClick = onEliminar) { Text("Eliminar") } },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}
