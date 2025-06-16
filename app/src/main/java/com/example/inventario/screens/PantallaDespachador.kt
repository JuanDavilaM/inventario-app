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
    val pedidos = remember { dbHelper.obtenerPerfiles() }
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
                                Text("Cliente: ${cliente?.nombre ?: "Desconocido"}", style = MaterialTheme.typography.titleMedium, color = Color(0xFF3F51B5))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Pedido: ${pedido.nombre}")
                                Text("Ahumadores Pequeños: ${pedido.cantidadPequenos}")
                                Text("Ahumadores Medianos: ${pedido.cantidadMedianos}")
                                Text("Ahumadores Grandes: ${pedido.cantidadGrandes}")
                                Text("Valor Total: \$${pedido.valorTotal}", style = MaterialTheme.typography.titleMedium, color = Color.Red)
                                Text("Fecha: ${pedido.fecha}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        // Verificar stock
                                        val stockPequenos = dbHelper.obtenerStockAhumador("Pequeño")
                                        val stockMedianos = dbHelper.obtenerStockAhumador("Mediano")
                                        val stockGrandes = dbHelper.obtenerStockAhumador("Grande")
                                        if (stockPequenos >= pedido.cantidadPequenos && stockMedianos >= pedido.cantidadMedianos && stockGrandes >= pedido.cantidadGrandes) {
                                            // Restar del stock
                                            dbHelper.actualizarStockAhumador("Pequeño", stockPequenos - pedido.cantidadPequenos)
                                            dbHelper.actualizarStockAhumador("Mediano", stockMedianos - pedido.cantidadMedianos)
                                            dbHelper.actualizarStockAhumador("Grande", stockGrandes - pedido.cantidadGrandes)
                                            mensajeSnackbar = "Pedido despachado exitosamente."
                                        } else {
                                            mensajeSnackbar = "No hay suficiente stock para despachar este pedido."
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.End)
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