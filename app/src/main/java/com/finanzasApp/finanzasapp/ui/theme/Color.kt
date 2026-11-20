package com.finanzasApp.finanzasapp.ui.theme

import androidx.compose.ui.graphics.Color

// =====================================================================
//  Paleta "Azul medianoche"
//  - Fondo: azul casi negro (no gris ni negro puro).
//  - Superficies (tarjetas): un escalon mas claro que el fondo, mismo tono.
//  - Acento: azul vivo, para botones, selecciones y enlaces.
// =====================================================================

// Fondo y superficies (de mas oscuro a mas claro; Material 3 usa varios
// escalones de "surfaceContainer" para cosas como el menu inferior)
val AzulMedianocheBaja  = Color(0xFF070B18) // el mas oscuro de todos (poco usado)
val AzulMedianoche      = Color(0xFF0A1021) // fondo de toda la app
val AzulSuperficieBaja  = Color(0xFF0F1830) // un escalon sobre el fondo (menu inferior)
val AzulSuperficie      = Color(0xFF121B33) // tarjetas normales
val AzulSuperficieAlta  = Color(0xFF1B2745) // tarjetas destacadas (saldo, categoría con mayor gasto)
val AzulSuperficieMasAlta = Color(0xFF223058) // el mas claro (elementos seleccionados)
val AzulBorde           = Color(0xFF2A3A63) // bordes y divisores sutiles

// Acento (azul vivo)
val AzulPrimario       = Color(0xFF4C8DFF) // botones, selector activo, enlaces
val AzulPrimarioOscuro = Color(0xFF1E3A78) // fondo de chips/botones tonales
val AzulClaro          = Color(0xFF9CC0FF) // texto/iconos sobre el acento oscuro

// Texto
val TextoPrimario   = Color(0xFFEAF0FF) // texto principal sobre fondo oscuro
val TextoSecundario = Color(0xFFA9B8DD) // texto secundario y etiquetas

// Estados (se distinguen del azul para no confundirse con el acento)
val VerdeIngreso        = Color(0xFF4ADE80) // ingresos
val RojoGasto           = Color(0xFFFF6B81) // gastos y errores
val RojoGastoOscuro     = Color(0xFF2B0A10) // texto/icono sobre el rojo
val RojoGastoContenedor = Color(0xFF3B1420) // fondo de tarjetas de error
