package com.finanzasApp.finanzasapp.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.finanzasApp.finanzasapp.ui.theme.VerdeIngreso

/**
 * El tema ahora es fijo ("azul medianoche", ver ui/theme/), asi que estos
 * colores ya no dependen del modo claro/oscuro del sistema.
 */

/** Verde para ingresos (se distingue del azul de acento de la app). */
@Composable
fun colorIngreso(): Color = VerdeIngreso

/** Rojo para gastos y errores. */
@Composable
fun colorGasto(): Color = MaterialTheme.colorScheme.error
