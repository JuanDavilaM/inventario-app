package com.example.inventario.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
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
    var valorIngresado by remember { mutableStateOf("") }
    var cantidadAgregar by remember { mutableStateOf("") }
    var asadoresCreadoTotal by remember { mutableStateOf(0) }
    var cantidadPosible by remember { mutableStateOf(0) }

    val articulos = dbHelper.obtenerTodos()

    Scaffold(
        topBar = {
            Header(
                title = "Calcular Asadores",
                navController = navController
            )
        },
        containerColor = Color(0xFFE6F0FF)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dropdown para seleccionar tipo de asador
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

            // Campo para ingresar el valor del asador
            if (tipoSeleccionado.isNotBlank()) {
                TextField(
                    value = valorIngresado,
                    onValueChange = { valorIngresado = it },
                    label = { Text("Ingresa el valor para $tipoSeleccionado") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
            }

            // Campo para ingresar la cantidad a agregar
            TextField(
                value = cantidadAgregar,
                onValueChange = { cantidadAgregar = it },
                label = { Text("Cantidad a agregar") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )

            // Botón para calcular cuántos asadores puedes crear
            Button(onClick = {
                if (tipoSeleccionado.isBlank() || valorIngresado.isBlank() || cantidadAgregar.isBlank()) {
                    resultado = "Por favor, completa todos los campos."
                    return@Button
                }

                val cantidad = cantidadAgregar.toIntOrNull() ?: 0
                val tipoCodigo = when (tipoSeleccionado) {
                    "Pequeño" -> 1
                    "Mediano" -> 2
                    "Grande" -> 3
                    else -> 0
                }

                // Calcular cuántos asadores se pueden hacer con los componentes disponibles
                val componentes = articulos.filter { it.tipoAsador == tipoSeleccionado && it.unidadesNecesarias > 0 }
                val minAsadores = if (componentes.isNotEmpty()) {
                    componentes.minOf { it.existencia / it.unidadesNecesarias }
                } else 0

                cantidadPosible = minAsadores

                if (cantidad > cantidadPosible) {
                    resultado = "No puedes crear esa cantidad de asadores. Solo puedes crear $cantidadPosible asadores."
                } else {
                    // Restar las unidades necesarias para crear los asadores
                    componentes.forEach { articulo ->
                        val asadoresAConstruir = cantidad
                        val unidadesRestantes = articulo.existencia - (asadoresAConstruir * articulo.unidadesNecesarias)
                        dbHelper.actualizarExistencia(articulo.codigo, unidadesRestantes)
                    }

                    // Añadir los asadores creados al inventario con el valor ingresado
                    val valorNumerico = valorIngresado.toDoubleOrNull() ?: 0.0
                    dbHelper.agregarAhumadoresCreados(cantidad, tipoSeleccionado, tipoCodigo, valorNumerico)

                    asadoresCreadoTotal += cantidad

                    resultado = "Se han creado $cantidad asadores tipo $tipoSeleccionado con un valor de \$${valorNumerico} cada uno."
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Calcular Asadores")
            }

            if (resultado.isNotBlank()) {
                Text(resultado, color = Color.Black)
            }

            // Mostrar el valor total de los asadores creados
            if (asadoresCreadoTotal > 0) {
                val valorTotal = valorIngresado.toDoubleOrNull() ?: 0.0 * asadoresCreadoTotal
                Text("Valor Total de los Asadores Creado: \$${valorTotal}")
            }

            // Botón para ver la lista de asadores
            Button(onClick = { navController.navigate("pantalla_lista_asadores") }, modifier = Modifier.fillMaxWidth()) {
                Text("Ver Lista de Asadores")
            }
        }
    }
}
