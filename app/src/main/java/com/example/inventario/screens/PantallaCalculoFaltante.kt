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
fun PantallaCalculoFaltante(dbHelper: InventarioDBHelper, navController: NavHostController) {
    val perfiles = remember { dbHelper.obtenerPerfiles() }
    val articulos = remember { dbHelper.obtenerTodos() }

    // Sumar asadores pendientes por tipo
    val pendientesPequenos = perfiles.filter { it.estado == "Activo" }.sumOf { it.cantidadPequenos - it.despachadoPequenos }
    val pendientesMedianos = perfiles.filter { it.estado == "Activo" }.sumOf { it.cantidadMedianos - it.despachadoMedianos }
    val pendientesGrandes = perfiles.filter { it.estado == "Activo" }.sumOf { it.cantidadGrandes - it.despachadoGrandes }

    // Calcular faltantes de artículos para fabricar todos los asadores pendientes
    data class Faltante(val articulo: Articulo, val cantidadFaltante: Int)
    val faltantes = mutableListOf<Faltante>()
    val tipos = listOf("Pequeño", "Mediano", "Grande")
    val pendientesPorTipo = mapOf(
        "Pequeño" to pendientesPequenos,
        "Mediano" to pendientesMedianos,
        "Grande" to pendientesGrandes
    )
    for (tipo in tipos) {
        val cantidadPendiente = pendientesPorTipo[tipo] ?: 0
        if (cantidadPendiente > 0) {
            val articulosTipo = articulos.filter { it.tipoAsador == tipo && it.unidadesNecesarias > 0 }
            for (art in articulosTipo) {
                val necesario = cantidadPendiente * art.unidadesNecesarias
                val faltante = necesario - art.existencia
                if (faltante > 0) {
                    faltantes.add(Faltante(art, faltante))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Header(title = "Cálculo Faltante", navController = navController)
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
            Text("Artículos faltantes para fabricar todos los asadores pendientes:", style = MaterialTheme.typography.titleMedium, color = Color(0xFF3F51B5))
            if (faltantes.isEmpty()) {
                Text("No faltan artículos. Puedes fabricar todos los asadores pendientes con el stock actual.", color = Color(0xFF388E3C))
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(faltantes) { faltante ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Artículo: ${faltante.articulo.nombre}", style = MaterialTheme.typography.titleMedium)
                                Text("Tipo de Asador: ${faltante.articulo.tipoAsador}")
                                Text("Unidades necesarias por asador: ${faltante.articulo.unidadesNecesarias}")
                                Text("Stock actual: ${faltante.articulo.existencia}")
                                Text("Cantidad faltante: ${faltante.cantidadFaltante}", color = Color.Red)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
    }
} 