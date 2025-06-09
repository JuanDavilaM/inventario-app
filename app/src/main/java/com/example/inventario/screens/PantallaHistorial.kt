package com.example.inventario.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.inventario.InventarioDBHelper

@Composable
fun PantallaHistorial(dbHelper: InventarioDBHelper, navController: NavHostController) {
    var filtro by remember { mutableStateOf("") }
    var lista by remember { mutableStateOf(dbHelper.obtenerMovimientosPorCodigo("")) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Historial de Movimientos", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = filtro,
            onValueChange = {
                filtro = it
                lista = dbHelper.obtenerMovimientosPorCodigo(filtro)
            },
            label = { Text("Buscar por Código") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

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
                        Text("${mov.fecha} - ${mov.tipo}", style = MaterialTheme.typography.bodyMedium)
                        Text("Código: ${mov.codigo}", style = MaterialTheme.typography.bodySmall)
                        Text("Cantidad: ${mov.cantidad} | Valor: \$${mov.valorUnitario} | Prom: \$${mov.promedio}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
