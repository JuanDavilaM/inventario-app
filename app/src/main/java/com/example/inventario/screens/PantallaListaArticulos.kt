package com.example.inventario.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.inventario.InventarioDBHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaListaArticulos(dbHelper: InventarioDBHelper, navController: NavHostController) {
    Scaffold(
        topBar = {
            Header(
                title = "Lista de Artículos",
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
            var lista by remember { mutableStateOf(dbHelper.obtenerTodos()) }
            val tipos = listOf("Pequeño", "Mediano", "Grande", "Todos", "No aplica")

            Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }

            Divider(thickness = 1.dp)

            tipos.forEach { tipo ->
                val articulosPorTipo = lista.filter { it.tipoAsador == tipo }

                if (articulosPorTipo.isNotEmpty()) {
                    Text("Tipo: $tipo", style = MaterialTheme.typography.titleMedium)

                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(articulosPorTipo) { articulo ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Código: ${articulo.codigo}", style = MaterialTheme.typography.bodyMedium)
                                        Text("Nombre: ${articulo.nombre}", style = MaterialTheme.typography.bodySmall)
                                        Text("Asador: ${articulo.tipoAsador}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Valor: \$${articulo.valor}", style = MaterialTheme.typography.bodySmall)
                                        Text("Stock: ${articulo.existencia}", style = MaterialTheme.typography.bodySmall)
                                        Text("Unidades necesarias: ${articulo.unidadesNecesarias}", style = MaterialTheme.typography.bodySmall)
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
