package com.finanzasApp.finanzasapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Esquema de color unico: "azul medianoche". A diferencia de la plantilla
 * original, la app YA NO cambia con el modo claro/oscuro del sistema ni usa
 * color dinamico (Material You): siempre se ve igual, para mantener una
 * identidad visual consistente.
 */
private val EsquemaMedianoche = darkColorScheme(
    primary = AzulPrimario,
    onPrimary = Color(0xFF00172E),
    primaryContainer = AzulPrimarioOscuro,
    onPrimaryContainer = AzulClaro,

    secondary = AzulClaro,
    onSecondary = Color(0xFF00172E),
    secondaryContainer = AzulSuperficieAlta,
    onSecondaryContainer = TextoPrimario,

    background = AzulMedianoche,
    onBackground = TextoPrimario,

    surface = AzulSuperficie,
    onSurface = TextoPrimario,
    surfaceVariant = AzulSuperficieAlta,
    onSurfaceVariant = TextoSecundario,

    // Sin estos, componentes como el menu inferior (NavigationBar) usan el
    // gris oscuro por defecto de Material 3 en vez de un tono de nuestro azul.
    surfaceContainerLowest = AzulMedianocheBaja,
    surfaceContainerLow = AzulSuperficieBaja,
    surfaceContainer = AzulSuperficie,
    surfaceContainerHigh = AzulSuperficieAlta,
    surfaceContainerHighest = AzulSuperficieMasAlta,
    surfaceTint = AzulPrimario,
    inverseSurface = TextoPrimario,
    inverseOnSurface = AzulMedianoche,
    inversePrimary = AzulPrimarioOscuro,

    outline = AzulBorde,
    outlineVariant = AzulBorde,

    error = RojoGasto,
    onError = RojoGastoOscuro,
    errorContainer = RojoGastoContenedor,
    onErrorContainer = RojoGasto
)

@Composable
fun FinanzasAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaMedianoche,
        typography = Typography,
        content = content
    )
}
