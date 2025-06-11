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

            LazyColumn {
                items(perfiles) { perfil ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Nombre: ${perfil.nombre}", style = MaterialTheme.typography.bodyMedium)
                                Text("Ahumadores Pequeños: ${perfil.cantidadPequenos}", style = MaterialTheme.typography.bodyMedium)
                                Text("Ahumadores Medianos: ${perfil.cantidadMedianos}", style = MaterialTheme.typography.bodyMedium)
                                Text("Ahumadores Grandes: ${perfil.cantidadGrandes}", style = MaterialTheme.typography.bodyMedium)
                                Text("Valor Total: \$${perfil.valorTotal}", style = MaterialTheme.typography.bodyMedium)
                            }
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
