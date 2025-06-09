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

    val tiposAsador = listOf("Pequeño", "Mediano", "Grande", "Todos", "No aplica")

    val snackbarHostState = remember { SnackbarHostState() }
    var mensajeSnackbar by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventario", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú", tint = Color.White)
                    }
                },
                actions = {
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(text = { Text("Ir a Movimientos") }, onClick = {
                            menuExpanded = false
                            navController.navigate("movimientos")
                        })
                        DropdownMenuItem(text = { Text("Ver Lista de Artículos") }, onClick = {
                            menuExpanded = false
                            navController.navigate("lista")
                        })
                        DropdownMenuItem(text = { Text("Calcular Asadores") }, onClick = {
                            menuExpanded = false
                            navController.navigate("calcular")
                        })
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F51B5))
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Inventario de Artículos",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(value = codigo, onValueChange = { codigo = it }, label = { Text("Código") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = valor, onValueChange = { valor = it }, label = { Text("Valor") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = existencia, onValueChange = { existencia = it }, label = { Text("Existencia") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = unidadesNecesarias, onValueChange = { unidadesNecesarias = it }, label = { Text("Unidades necesarias por asador") }, modifier = Modifier.fillMaxWidth())

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

            Button(
                onClick = { confirmandoBorrado = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Borrar Artículo", color = MaterialTheme.colorScheme.onError)
            }

            if (confirmandoBorrado) {
                var claveInput by remember { mutableStateOf("") }
                val claveGuardada = context.obtenerClave()
                var creandoClave by remember { mutableStateOf(claveGuardada == null) }

                AlertDialog(
                    onDismissRequest = { confirmandoBorrado = false },
                    title = { Text(if (creandoClave) "Crear Clave" else "Autenticación") },
                    text = {
                        Column {
                            Text(if (creandoClave) "Establece una clave para borrar artículos" else "Ingresa la clave para confirmar")
                            OutlinedTextField(
                                value = claveInput,
                                onValueChange = { claveInput = it },
                                label = { Text("Clave") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (creandoClave) {
                                if (claveInput.length < 3) {
                                    mensajeSnackbar = "La clave debe tener al menos 3 caracteres"
                                } else {
                                    context.guardarClave(claveInput)
                                    creandoClave = false
                                    mensajeSnackbar = "Clave guardada. Intenta de nuevo borrar."
                                    confirmandoBorrado = false
                                }
                            } else {
                                if (claveInput == claveGuardada) {
                                    val eliminado = dbHelper.eliminarArticuloPorCodigo(codigo)
                                    if (eliminado) {
                                        codigo = ""; nombre = ""; valor = ""; existencia = ""; tipoAsador = ""; unidadesNecesarias = ""
                                        mensajeSnackbar = "Artículo eliminado correctamente"
                                    } else {
                                        mensajeSnackbar = "No se encontró el artículo con ese código"
                                    }
                                    confirmandoBorrado = false
                                } else {
                                    mensajeSnackbar = "Clave incorrecta"
                                }
                            }
                        }) {
                            Text(if (creandoClave) "Guardar" else "Aceptar")
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
