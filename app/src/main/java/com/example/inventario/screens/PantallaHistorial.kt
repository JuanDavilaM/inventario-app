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
fun PantallaHistorial(dbHelper: InventarioDBHelper, navController: NavHostController) {
    var filtro by remember { mutableStateOf("") }
    var lista by remember { mutableStateOf(dbHelper.obtenerMovimientosPorCodigo("")) }

    Scaffold(
        topBar = {
            Header(
                title = "Historial de Movimientos",
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
            OutlinedTextField(
                value = filtro,
                onValueChange = {
                    filtro = it
                    lista = dbHelper.obtenerMovimientosPorCodigo(filtro)
                },
                label = { Text("Buscar por Código") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }

            Divider(thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

            LazyColumn {
                items(lista) { mov ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${mov.fecha} - ${mov.tipo}", style = MaterialTheme.typography.bodyMedium)
                                if (mov.esAjuste) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("✔️ Ajuste", color = Color(0xFF388E3C), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Text("Código: ${mov.codigo}", style = MaterialTheme.typography.bodySmall)
                            Text("Cantidad: ${mov.cantidad} | Valor: \$${mov.valorUnitario} | Prom: \$${mov.promedio}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
