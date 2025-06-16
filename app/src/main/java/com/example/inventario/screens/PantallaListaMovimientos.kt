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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaListaMovimientos(dbHelper: InventarioDBHelper, navController: NavHostController) {
    val tabs = listOf("Artículos", "Ahumadores", "Pedidos")
    var selectedTab by remember { mutableStateOf(0) }

    // Movimientos de artículos
    val movimientosArticulos = remember { dbHelper.obtenerMovimientosPorCodigo("") }
    // Movimientos de ahumadores (simulación: filtrar movimientos con código que empiece por "Ahumador_")
    val movimientosAhumadores = movimientosArticulos.filter { it.codigo.startsWith("Ahumador_") }
    // Movimientos de pedidos (simulación: filtrar movimientos con código que empiece por "Pedido_" o similar)
    val movimientosPedidos = movimientosArticulos.filter { it.codigo.startsWith("Pedido_") }

    Scaffold(
        topBar = {
            Header(title = "Lista de Movimientos", navController = navController)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            when (selectedTab) {
                0 -> ListaMovimientos(movimientosArticulos, "artículo")
                1 -> ListaMovimientos(movimientosAhumadores, "ahumador")
                2 -> ListaMovimientos(movimientosPedidos, "pedido")
            }
        }
    }
}

@Composable
fun ListaMovimientos(lista: List<Movimiento>, tipo: String) {
    if (lista.isEmpty()) {
        Text("No hay movimientos de $tipo registrados.", color = MaterialTheme.colorScheme.onBackground)
    } else {
        LazyColumn {
            items(lista) { mov ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Text("${mov.fecha} - ${mov.tipo}", style = MaterialTheme.typography.bodyMedium)
                            if (mov.esAjuste) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("✔️ Ajuste", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Text("Código: ${mov.codigo}", style = MaterialTheme.typography.bodySmall)
                        Text("Cantidad: ${mov.cantidad} | Valor: $${mov.valorUnitario} | Prom: $${mov.promedio}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
} 