package com.example.inventario.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.inventario.InventarioDBHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCalcularAsadores(dbHelper: InventarioDBHelper, navController: NavHostController) {
    val tiposAsador = listOf("Pequeño", "Mediano", "Grande", "Todos", "No aplica")
    var tipoSeleccionado by remember { mutableStateOf("") }
    var tipoExpanded by remember { mutableStateOf(false) }
    var resultado by remember { mutableStateOf("") }

    val articulos = dbHelper.obtenerTodos()

    Scaffold(
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Calcular Asadores", color = Color.White, style = MaterialTheme.typography.headlineSmall)

            ExposedDropdownMenuBox(expanded = tipoExpanded, onExpandedChange = { tipoExpanded = !tipoExpanded }) {
                OutlinedTextField(
                    value = tipoSeleccionado,
                    onValueChange = {},
                    label = { Text("Tipo de Asador") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(tipoExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = tipoExpanded, onDismissRequest = { tipoExpanded = false }) {
                    tiposAsador.forEach { tipo ->
                        DropdownMenuItem(text = { Text(tipo) }, onClick = {
                            tipoSeleccionado = tipo
                            tipoExpanded = false
                        })
                    }
                }
            }

            Button(onClick = {
                if (tipoSeleccionado.isBlank()) {
                    resultado = "Por favor selecciona un tipo de asador."
                    return@Button
                }

                val componentes = articulos.filter { it.tipoAsador == tipoSeleccionado && it.unidadesNecesarias > 0 }

                val minAsadores = if (componentes.isNotEmpty()) {
                    componentes.minOf { it.existencia / it.unidadesNecesarias }
                } else 0

                resultado = "Puedes construir $minAsadores asadores tipo $tipoSeleccionado."
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Calcular Asadores")
            }

            if (resultado.isNotBlank()) {
                Text(resultado, color = Color.White)
            }

            Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
    }
}
