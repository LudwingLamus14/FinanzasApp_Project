package com.finanzasApp.finanzasapp.ui.components

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.finanzasApp.finanzasapp.R
import com.finanzasApp.finanzasapp.util.Formato
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoFecha(
    valor: String,
    onCambiar: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    esInvalida: Boolean = error != null
) {
    var mostrarCalendario by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = valor,
        onValueChange = { texto ->
            if (texto.none { it.isWhitespace() }) onCambiar(texto)
        },
        label = { Text(etiqueta) },
        placeholder = { Text("dd/MM/aaaa") },
        singleLine = true,
        isError = esInvalida,
        supportingText = error?.let { mensaje -> { Text(mensaje) } },
        trailingIcon = {
            IconButton(onClick = { mostrarCalendario = true }) {
                Icon(
                    painter = painterResource(R.drawable.ic_calendario),
                    contentDescription = "Abrir calendario para ${etiqueta.lowercase()}"
                )
            }
        },
        modifier = modifier
    )

    if (mostrarCalendario) {
        val fecha = Formato.parseFecha(valor)
        val fechaVisible = fecha ?: LocalDate.now()
        val rango = DatePickerDefaults.YearRange
        val calendario = rememberDatePickerState(
            initialSelectedDateMillis = fecha?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
            initialDisplayedMonthMillis = fechaVisible.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            yearRange = minOf(rango.first, fechaVisible.year)..maxOf(rango.last, fechaVisible.year)
        )

        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(
                    enabled = calendario.selectedDateMillis != null,
                    onClick = {
                        calendario.selectedDateMillis?.let { millis ->
                            val seleccion = Instant.ofEpochMilli(millis)
                                .atOffset(ZoneOffset.UTC)
                                .toLocalDate()
                            onCambiar(Formato.fecha(seleccion.toString()))
                        }
                        mostrarCalendario = false
                    }
                ) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendario = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(
                state = calendario,
                modifier = Modifier.verticalScroll(rememberScrollState()),
                title = null,
                showModeToggle = false
            )
        }
    }
}
