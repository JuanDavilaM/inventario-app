package com.example.inventario.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.inventario.InventarioDBHelper

fun Context.guardarClave(clave: String) {
    getSharedPreferences("config", Context.MODE_PRIVATE)
        .edit().putString("clave_borrado", clave).apply()
}

fun Context.obtenerClave(): String? {
    return getSharedPreferences("config", Context.MODE_PRIVATE)
        .getString("clave_borrado", null)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInventario(dbHelper: InventarioDBHelper, navController: NavHostController) {
    val context = LocalContext.current

    var codigo by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }
    var existencia by remember { mutableStateOf("") }
    var unidadesNecesarias by remember { mutableStateOf("") }
    var tipoAsador by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var confirmandoBorrado by remember { mutableStateOf(false) }
    var codigoBorrar by remember { mutableStateOf("") }

    val tiposAsador = listOf("Pequeño", "Mediano", "Grande", "Todos", "No aplica")

    val snackbarHostState = remember { SnackbarHostState() }
    var mensajeSnackbar by remember { mutableStateOf("") }

    // Función para generar el siguiente código basado en el tipo de asador
    fun generarSiguienteCodigo(tipo: String): String {
        val prefijo = when (tipo) {
            "Pequeño" -> "1"
            "Mediano" -> "2"
            "Grande" -> "3"
            else -> "0"
        }
        
        val articulos = dbHelper.obtenerTodos()
        val codigosExistentes = articulos
            .filter { it.codigo.startsWith(prefijo) }
            .map { it.codigo.substring(1).toIntOrNull() ?: 0 }
            .toSet()
        
        var siguienteNumero = 1
        while (siguienteNumero in codigosExistentes) {
            siguienteNumero++
        }
        
        return "$prefijo${String.format("%02d", siguienteNumero)}"
    }

    Scaffold(
        topBar = {
            Header(
                title = "Inventario",
                navController = navController
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFE6F0FF)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE6F0FF))
                .padding(16.dp)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Inventario de Artículos",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = codigo,
                onValueChange = { codigo = it },
                label = { Text("Código") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = valor,
                onValueChange = { valor = it },
                label = { Text("Valor") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = existencia,
                onValueChange = { existencia = it },
                label = { Text("Existencia") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = unidadesNecesarias,
                onValueChange = { unidadesNecesarias = it },
                label = { Text("Unidades necesarias por asador") },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = tipoAsador,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de Asador") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    tiposAsador.forEach { tipo ->
                        DropdownMenuItem(text = { Text(tipo) }, onClick = {
                            tipoAsador = tipo
                            if (tipo != "Todos" && tipo != "No aplica") {
                                codigo = generarSiguienteCodigo(tipo)
                            }
                            expanded = false
                        })
                    }
                }
            }

            Button(
                onClick = {
                    if (
                        codigo.isBlank() || nombre.isBlank() || valor.isBlank() ||
                        existencia.isBlank() || tipoAsador.isBlank() || unidadesNecesarias.isBlank() ||
                        valor.toDoubleOrNull() == null || existencia.toIntOrNull() == null || unidadesNecesarias.toIntOrNull() == null
                    ) {
                        mensajeSnackbar = "Por favor completa todos los campos correctamente."
                    } else {
                        val articulo = Articulo(codigo, nombre, valor.toDouble(), existencia.toInt(), tipoAsador, unidadesNecesarias.toInt())
                        val insertado = dbHelper.insertarArticulo(articulo)
                        if (insertado) {
                            codigo = ""; nombre = ""; valor = ""; existencia = ""; tipoAsador = ""; unidadesNecesarias = ""
                            mensajeSnackbar = "Artículo guardado exitosamente"
                        } else {
                            mensajeSnackbar = "Error al guardar artículo"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Artículo")
            }

            // Campo para ingresar el código del artículo a borrar
            OutlinedTextField(
                value = codigoBorrar,
                onValueChange = { codigoBorrar = it },
                label = { Text("Código del artículo a borrar") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (codigoBorrar.isBlank()) {
                        mensajeSnackbar = "Por favor ingresa el código del artículo a borrar"
                    } else {
                        confirmandoBorrado = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Borrar Artículo", color = MaterialTheme.colorScheme.onError)
            }

            if (confirmandoBorrado) {
                AlertDialog(
                    onDismissRequest = { confirmandoBorrado = false },
                    title = { Text("Confirmar borrado") },
                    text = { Text("¿Estás seguro de que deseas borrar el artículo con código: $codigoBorrar?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val eliminado = dbHelper.eliminarArticuloPorCodigo(codigoBorrar)
                                if (eliminado) {
                                    mensajeSnackbar = "Artículo eliminado correctamente"
                                    codigoBorrar = ""
                                } else {
                                    mensajeSnackbar = "No se encontró el artículo con ese código"
                                }
                                confirmandoBorrado = false
                            }
                        ) {
                            Text("Borrar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { confirmandoBorrado = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            LaunchedEffect(mensajeSnackbar) {
                if (mensajeSnackbar.isNotBlank()) {
                    snackbarHostState.showSnackbar(mensajeSnackbar)
                    mensajeSnackbar = ""
                }
            }
        }
    }
}
