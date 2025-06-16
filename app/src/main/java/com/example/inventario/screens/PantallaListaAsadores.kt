package com.example.inventario.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.inventario.InventarioDBHelper
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaListaAsadores(dbHelper: InventarioDBHelper, navController: NavHostController) {
    val asadores = dbHelper.obtenerAsadoresCreados()
    var asadorAReversar by remember { mutableStateOf<Asador?>(null) }
    var cantidadReversa by remember { mutableStateOf("") }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy")
    val fechaHoy = dateFormat.format(Date())
    val articulos = dbHelper.obtenerTodos()

    Scaffold(
        topBar = {
            Header(
                title = "Lista de Asadores",
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
            LazyColumn {
                items(asadores) { asador ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Código: ${asador.codigo}", style = MaterialTheme.typography.bodyMedium)
                                Text("Nombre: ${asador.nombre}", style = MaterialTheme.typography.bodyMedium)
                                Text("Valor: \$${asador.valor}", style = MaterialTheme.typography.bodyMedium)
                                Text("Stock: ${asador.existencia}", style = MaterialTheme.typography.bodyMedium)
                            }
                            Button(onClick = {
                                asadorAReversar = asador
                                cantidadReversa = ""
                                mostrarDialogo = true
                            }) {
                                Text("Reversar")
                            }
                        }
                    }
                }
            }
            if (mensaje.isNotBlank()) {
                Text(mensaje, color = Color.Red)
            }
            Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
        // Diálogo para reversar ahumadores
        if (mostrarDialogo && asadorAReversar != null) {
            AlertDialog(
                onDismissRequest = { mostrarDialogo = false },
                title = { Text("Reversar Ahumadores") },
                text = {
                    Column {
                        Text("¿Cuántos ahumadores de tipo '${asadorAReversar!!.nombre}' deseas desconvertir?")
                        OutlinedTextField(
                            value = cantidadReversa,
                            onValueChange = { cantidadReversa = it },
                            label = { Text("Cantidad a reversar") },
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val cantidad = cantidadReversa.toIntOrNull() ?: 0
                        if (cantidad <= 0 || cantidad > asadorAReversar!!.existencia) {
                            mensaje = "Cantidad inválida."
                            mostrarDialogo = false
                            return@TextButton
                        }
                        // 1. Restar ahumadores
                        val nuevaExistencia = asadorAReversar!!.existencia - cantidad
                        dbHelper.actualizarStockAhumador(asadorAReversar!!.nombre.split(" ").last(), nuevaExistencia)
                        // 2. Sumar artículos según receta
                        val tipo = asadorAReversar!!.nombre.split(" ").last() // "Pequeño", "Mediano", "Grande"
                        val componentes = articulos.filter { it.tipoAsador == tipo && it.unidadesNecesarias > 0 }
                        componentes.forEach { articulo ->
                            val cantidadRecuperada = cantidad * articulo.unidadesNecesarias
                            val nuevaExistenciaArticulo = maxOf(articulo.existencia + cantidadRecuperada, 0)
                            dbHelper.actualizarExistencia(articulo.codigo, nuevaExistenciaArticulo)
                            // Registrar movimiento de entrada de artículos
                            val movArt = Movimiento(
                                tipo = "Entrada",
                                fecha = fechaHoy,
                                codigo = articulo.codigo,
                                cantidad = cantidadRecuperada,
                                valorUnitario = articulo.valor,
                                promedio = articulo.valor,
                                esAjuste = false
                            )
                            dbHelper.registrarMovimiento(movArt)
                        }
                        // 3. Registrar movimiento de salida de ahumadores
                        val movAhumador = Movimiento(
                            tipo = "Salida",
                            fecha = fechaHoy,
                            codigo = asadorAReversar!!.codigo,
                            cantidad = cantidad,
                            valorUnitario = asadorAReversar!!.valor,
                            promedio = asadorAReversar!!.valor,
                            esAjuste = false
                        )
                        dbHelper.registrarMovimiento(movAhumador)
                        mensaje = "Reversión realizada correctamente."
                        mostrarDialogo = false
                    }) {
                        Text("Reversar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogo = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
