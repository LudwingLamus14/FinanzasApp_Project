package com.finanzasApp.finanzasapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.finanzasApp.finanzasapp.util.Formato
import java.math.BigDecimal

/**
 * Grafico de barras simple (dibujado con Canvas, sin libreria externa) que
 * compara el total de ingresos contra el total de gastos de un periodo.
 * La barra mas alta de las dos ocupa toda la altura disponible.
 */
@Composable
fun GraficoIngresosGastos(
    ingresos: BigDecimal,
    gastos: BigDecimal,
    modifier: Modifier = Modifier
) {
    val maximo = maxOf(ingresos, gastos, BigDecimal.ONE) // evita dividir por 0 si ambos son 0
    val alturaMaxima = 140.dp

    Row(
        modifier = modifier.height(alturaMaxima + 48.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        Barra(
            etiqueta = "Ingresos",
            valor = ingresos,
            proporcion = (ingresos.toDouble() / maximo.toDouble()).toFloat(),
            color = colorIngreso(),
            alturaMaxima = alturaMaxima
        )
        Barra(
            etiqueta = "Gastos",
            valor = gastos,
            proporcion = (gastos.toDouble() / maximo.toDouble()).toFloat(),
            color = colorGasto(),
            alturaMaxima = alturaMaxima
        )
    }
}

@Composable
private fun Barra(
    etiqueta: String,
    valor: BigDecimal,
    proporcion: Float,
    color: Color,
    alturaMaxima: Dp
) {
    // Minimo 3% de altura para que la barra se vea aunque el valor sea 0
    val alturaBarra = alturaMaxima * proporcion.coerceIn(0.03f, 1f)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = Formato.moneda(valor), style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(4.dp))
        Canvas(modifier = Modifier.width(56.dp).height(alturaBarra)) {
            drawRect(color = color, size = Size(size.width, size.height))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = etiqueta, style = MaterialTheme.typography.labelMedium)
    }
}
