package com.finanzasApp.finanzasapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Aviso de error. Si se pasa onReintentar, muestra un boton "Reintentar". */
@Composable
fun MensajeError(
    mensaje: String,
    modifier: Modifier = Modifier,
    onReintentar: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = mensaje, color = MaterialTheme.colorScheme.onErrorContainer)
            if (onReintentar != null) {
                TextButton(onClick = onReintentar) {
                    Text("Reintentar")
                }
            }
        }
    }
}
