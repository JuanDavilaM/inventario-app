package com.example.inventario.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun PantallaDespachador(dbHelper: InventarioDBHelper, navController: NavHostController) {
    val context = LocalContext.current
    var pedidos by remember { mutableStateOf(dbHelper.obtenerPerfiles()) }
    val clientes = remember { dbHelper.obtenerClientes() }
    var mensajeSnackbar by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            Header(title = "Despachador", navController = navController)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFE6F0FF)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (pedidos.isEmpty()) {
                Text("No hay pedidos pendientes.", color = Color.Gray)
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(pedidos) { pedido ->
                        val cliente = clientes.find { it.id == pedido.clienteId }
                        var despacharPequenos by remember { mutableStateOf("") }
                        var despacharMedianos by remember { mutableStateOf("") }
                        var despacharGrandes by remember { mutableStateOf("") }
                        val pendientePequenos = pedido.cantidadPequenos - pedido.despachadoPequenos
                        val pendienteMedianos = pedido.cantidadMedianos - pedido.despachadoMedianos
                        val pendienteGrandes = pedido.cantidadGrandes - pedido.despachadoGrandes
                        val completado = pendientePequenos == 0 && pendienteMedianos == 0 && pendienteGrandes == 0
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            ) {
                                Text("Cliente: ${cliente?.let { String.format("%03d", it.id) + " - " + it.nombre } ?: "Desconocido"}", style = MaterialTheme.typography.titleMedium, color = Color(0xFF3F51B5))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Pedido: ${pedido.nombre}")
                                Text("Ahumadores Pequeños: ${pedido.cantidadPequenos} | Despachados: ${pedido.despachadoPequenos} | Pendiente: $pendientePequenos")
                                OutlinedTextField(
                                    value = despacharPequenos,
                                    onValueChange = { despacharPequenos = it },
                                    label = { Text("Despachar Pequeños") },
                                    enabled = pedido.estado == "Activo" && pendientePequenos > 0 && !completado,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text("Ahumadores Medianos: ${pedido.cantidadMedianos} | Despachados: ${pedido.despachadoMedianos} | Pendiente: $pendienteMedianos")
                                OutlinedTextField(
                                    value = despacharMedianos,
                                    onValueChange = { despacharMedianos = it },
                                    label = { Text("Despachar Medianos") },
                                    enabled = pedido.estado == "Activo" && pendienteMedianos > 0 && !completado,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text("Ahumadores Grandes: ${pedido.cantidadGrandes} | Despachados: ${pedido.despachadoGrandes} | Pendiente: $pendienteGrandes")
                                OutlinedTextField(
                                    value = despacharGrandes,
                                    onValueChange = { despacharGrandes = it },
                                    label = { Text("Despachar Grandes") },
                                    enabled = pedido.estado == "Activo" && pendienteGrandes > 0 && !completado,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text("Valor Total: \$${pedido.valorTotal}", style = MaterialTheme.typography.titleMedium, color = Color.Red)
                                Text("Fecha comprometida: ${pedido.fechaComprometida ?: "-"}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                                Text("Estado: ${pedido.estado}", color = if (pedido.estado == "Cancelado") Color.Red else if (completado) Color.Gray else Color(0xFF388E3C), style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        val dPeq = despacharPequenos.toIntOrNull() ?: 0
                                        val dMed = despacharMedianos.toIntOrNull() ?: 0
                                        val dGra = despacharGrandes.toIntOrNull() ?: 0
                                        if (dPeq < 0 || dMed < 0 || dGra < 0) {
                                            mensajeSnackbar = "No puedes despachar cantidades negativas."
                                            return@Button
                                        }
                                        if (dPeq > pendientePequenos || dMed > pendienteMedianos || dGra > pendienteGrandes) {
                                            mensajeSnackbar = "No puedes despachar más de lo pendiente."
                                            return@Button
                                        }
                                        // Actualizar los despachados en la base de datos
                                        dbHelper.despacharPedido(
                                            pedido.id,
                                            pedido.despachadoPequenos + dPeq,
                                            pedido.despachadoMedianos + dMed,
                                            pedido.despachadoGrandes + dGra
                                        )
                                        // Restar del stock
                                        dbHelper.actualizarStockAhumador("Pequeño", dbHelper.obtenerStockAhumador("Pequeño") - dPeq)
                                        dbHelper.actualizarStockAhumador("Mediano", dbHelper.obtenerStockAhumador("Mediano") - dMed)
                                        dbHelper.actualizarStockAhumador("Grande", dbHelper.obtenerStockAhumador("Grande") - dGra)
                                        mensajeSnackbar = "Despacho realizado."
                                        // Refrescar la lista
                                        pedidos = dbHelper.obtenerPerfiles()
                                    },
                                    enabled = pedido.estado == "Activo" && !completado && (pendientePequenos > 0 || pendienteMedianos > 0 || pendienteGrandes > 0),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Despachar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(mensajeSnackbar) {
        if (mensajeSnackbar.isNotBlank()) {
            snackbarHostState.showSnackbar(mensajeSnackbar)
            mensajeSnackbar = ""
        }
    }
} 