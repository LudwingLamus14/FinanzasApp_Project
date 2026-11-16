package com.finanzasApp.finanzasapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.finanzasApp.finanzasapp.data.model.Resumen
import com.finanzasApp.finanzasapp.util.Formato

/**
 * Saldo grande arriba; ingresos y gastos debajo, lado a lado.
 * Es la tarjeta mas importante del Dashboard, asi que usa "surfaceVariant"
 * (un azul un escalon mas claro que el fondo) para distinguirse del resto.
 */
@Composable
fun TarjetaResumen(resumen: Resumen, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Saldo", style = MaterialTheme.typography.labelLarge)
            Text(
                text = Formato.moneda(resumen.saldo),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Ingresos", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = Formato.moneda(resumen.totalIngresos),
                        style = MaterialTheme.typography.titleMedium,
                        color = colorIngreso()
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Gastos", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = Formato.moneda(resumen.totalGastos),
                        style = MaterialTheme.typography.titleMedium,
                        color = colorGasto()
                    )
                }
            }
        }
    }
}
