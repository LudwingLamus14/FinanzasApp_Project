package com.finanzasApp.finanzasapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.finanzasApp.finanzasapp.data.model.Categoria

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorCategoria(
    categorias: List<Categoria>,
    categoriaId: Long?,
    onSeleccionar: (Long?) -> Unit,
    opcionSinCategoria: String? = null,
    error: String? = null
) {
    var expandido by remember(categorias) { mutableStateOf(false) }
    val nombreSeleccionado = categorias.firstOrNull { it.id == categoriaId }?.nombre
    val habilitado = categorias.isNotEmpty() || opcionSinCategoria != null

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = nombreSeleccionado ?: opcionSinCategoria.orEmpty(),
            onValueChange = {},
            readOnly = true,
            enabled = habilitado,
            label = { Text("Categoría") },
            placeholder = { Text("Selecciona una categoría") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            isError = error != null,
            supportingText = error?.let { mensaje -> { Text(mensaje) } },
            singleLine = true,
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = habilitado)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            opcionSinCategoria?.let { nombre ->
                DropdownMenuItem(
                    text = { Text(nombre) },
                    onClick = {
                        expandido = false
                        onSeleccionar(null)
                    }
                )
            }
            categorias.forEach { categoria ->
                DropdownMenuItem(
                    text = { Text(categoria.nombre) },
                    onClick = {
                        expandido = false
                        onSeleccionar(categoria.id)
                    }
                )
            }
        }
    }
}
