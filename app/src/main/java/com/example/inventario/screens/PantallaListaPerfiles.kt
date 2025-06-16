package com.example.inventario.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.inventario.InventarioDBHelper

@Composable
fun PantallaListaPerfiles(dbHelper: InventarioDBHelper, navController: NavHostController) {
    val perfiles = dbHelper.obtenerPerfiles() // Obtener todos los perfiles desde la base de datos
    val clientes = dbHelper.obtenerClientes() // Obtener todos los clientes
    var filtro by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color.White // Fondo blanco para la pantalla
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Lista de Perfiles de Clientes", color = Color.Black, style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = filtro,
                onValueChange = { filtro = it },
                label = { Text("Buscar por nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            val clientesFiltrados = clientes.filter { it.nombre.contains(filtro, ignoreCase = true) }

            LazyColumn {
                items(clientesFiltrados) { cliente ->
                    val cantidadPedidos = perfiles.count { it.clienteId == cliente.id }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp), // Más separación entre tarjetas
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text("Nombre: ${cliente.nombre}", style = MaterialTheme.typography.bodyMedium)
                            Text("Teléfono: ${cliente.telefono}", style = MaterialTheme.typography.bodyMedium)
                            Text("Correo: ${cliente.correo}", style = MaterialTheme.typography.bodyMedium)
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                            Text("Cantidad de pedidos realizados: $cantidadPedidos", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
    }
}
