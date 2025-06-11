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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaListaAsadores(dbHelper: InventarioDBHelper, navController: NavHostController) {
    // Obtener la lista de ahumadores creados desde la base de datos
    val asadores = dbHelper.obtenerAsadoresCreados()  // Asegúrate de obtener solo los ahumadores

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
            // Mostrar la lista de asadores
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
                        }
                    }
                }
            }

            // Botón para volver a la pantalla anterior
            Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
    }
}
