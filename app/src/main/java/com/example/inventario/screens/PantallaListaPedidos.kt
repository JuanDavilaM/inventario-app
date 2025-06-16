package com.example.inventario.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.inventario.InventarioDBHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaListaPedidos(dbHelper: InventarioDBHelper, navController: NavHostController) {
    val pedidos = remember { dbHelper.obtenerPerfiles() }
    val clientes = remember { dbHelper.obtenerClientes() }

    Scaffold(
        topBar = {
            Header(title = "Lista de Pedidos", navController = navController)
        },
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
                Text("No hay pedidos registrados.", color = Color.Gray)
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
                                Text("Cliente: ${cliente?.let { String.format("%03d", it.id) + " - " + it.nombre } ?: "Desconocido"}", style = MaterialTheme.typography.titleMedium, color = Color(0xFF3F51B5))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Pedido: ${pedido.nombre}")
                                Text("Ahumadores Pequeños: ${pedido.cantidadPequenos} | Despachados: ${pedido.despachadoPequenos} | Pendiente: ${pedido.cantidadPequenos - pedido.despachadoPequenos}")
                                Text("Ahumadores Medianos: ${pedido.cantidadMedianos} | Despachados: ${pedido.despachadoMedianos} | Pendiente: ${pedido.cantidadMedianos - pedido.despachadoMedianos}")
                                Text("Ahumadores Grandes: ${pedido.cantidadGrandes} | Despachados: ${pedido.despachadoGrandes} | Pendiente: ${pedido.cantidadGrandes - pedido.despachadoGrandes}")
                                Text("Valor Total: \$${pedido.valorTotal}", style = MaterialTheme.typography.titleMedium, color = Color.Red)
                                Text("Fecha: ${pedido.fecha}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                                Text("Fecha comprometida: ${pedido.fechaComprometida ?: "-"}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                                Text("Estado: ${pedido.estado}", color = if (pedido.estado == "Cancelado") Color.Red else Color(0xFF388E3C), style = MaterialTheme.typography.bodySmall)
                                if (pedido.estado == "Activo") {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            // Cancelar pedido (deberás implementar el método en DBHelper)
                                            dbHelper.actualizarEstadoPedido(pedido.id, "Cancelado")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Cancelar Pedido", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 