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
                                Text("Cliente: ${cliente?.nombre ?: "Desconocido"}", style = MaterialTheme.typography.titleMedium, color = Color(0xFF3F51B5))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Pedido: ${pedido.nombre}")
                                Text("Ahumadores Pequeños: ${pedido.cantidadPequenos}")
                                Text("Ahumadores Medianos: ${pedido.cantidadMedianos}")
                                Text("Ahumadores Grandes: ${pedido.cantidadGrandes}")
                                Text("Valor Total: \$${pedido.valorTotal}", style = MaterialTheme.typography.titleMedium, color = Color.Red)
                                Text("Fecha: ${pedido.fecha}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
} 