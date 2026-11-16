package com.finanzasApp.finanzasapp.util

import com.finanzasApp.finanzasapp.data.model.TipoMovimiento
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.Locale

/** Formatos de dinero y fecha para mostrar en pantalla. */
object Formato {

    private val localeColombia: Locale = Locale.forLanguageTag("es-CO")

    // STRICT: rechaza fechas imposibles como 31/02/2026
    private val formatoFecha: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)

    /** 1250000 -> "$ 1.250.000"   (negativos: "-$ 1.000") */
    fun moneda(valor: BigDecimal): String {
        val numero = NumberFormat.getIntegerInstance(localeColombia).format(valor.abs())
        val signo = if (valor.signum() < 0) "-" else ""
        return signo + "$ " + numero
    }

    /** INGRESO -> "+$ 2.000.000"   GASTO -> "-$ 25.000" */
    fun montoConSigno(tipo: TipoMovimiento, monto: BigDecimal): String {
        val signo = if (tipo == TipoMovimiento.INGRESO) "+" else "-"
        return signo + moneda(monto.abs())
    }

    /** "2026-09-19" (formato ISO almacenado) -> "19/09/2026" */
    fun fecha(iso: String): String =
        try {
            LocalDate.parse(iso).format(formatoFecha)
        } catch (e: DateTimeParseException) {
            iso
        }

    /** Fecha de hoy como "dd/MM/yyyy". */
    fun hoy(): String = LocalDate.now().format(formatoFecha)

    /** "19/09/2026" -> LocalDate, o null si el texto no es una fecha valida. */
    fun parseFecha(texto: String): LocalDate? =
        try {
            LocalDate.parse(texto.trim(), formatoFecha)
        } catch (e: DateTimeParseException) {
            null
        }
}
