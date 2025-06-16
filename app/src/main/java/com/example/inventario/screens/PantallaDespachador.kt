package com.example.inventario.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.inventario.InventarioDBHelper
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDespachador(dbHelper: InventarioDBHelper, navController: NavHostController) {
    val context = LocalContext.current
    var filtro by remember { mutableStateOf("") }
    var pedidos by remember { mutableStateOf(dbHelper.obtenerPerfiles()) }
    val clientes = remember { dbHelper.obtenerClientes() }
    var mensajeSnackbar by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    val pedidosFiltrados = pedidos.filter { it.nombre.contains(filtro, ignoreCase = true) }
    val pendientes = pedidosFiltrados.filter { it.estado == "Activo" }
    val completados = pedidosFiltrados.filter { it.estado == "Completado" }
    val anulados = pedidosFiltrados.filter { it.estado == "Cancelado" || it.estado == "Anulado" }
    var mostrarDialogoAnulacion by remember { mutableStateOf(false) }
    var razonAnulacion by remember { mutableStateOf("") }
    var pedidoAAnular by remember { mutableStateOf<Perfil?>(null) }

    Scaffold(
        topBar = {
            Header(title = "Despachador", navController = navController)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFE6F0FF)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                OutlinedTextField(
                    value = filtro,
                    onValueChange = {
                        filtro = it
                        pedidos = dbHelper.obtenerPerfiles()
                    },
                    label = { Text("Filtrar por número de pedido") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Text("Pedidos Pendientes", style = MaterialTheme.typography.titleMedium, color = Color(0xFF3F51B5))
            }
            if (pendientes.isEmpty()) {
                item { Text("No hay pedidos pendientes.", color = Color.Gray) }
            } else {
                items(pendientes) { pedido ->
                    val cliente = clientes.find { it.id == pedido.clienteId }
                    var despacharPequenos by remember { mutableStateOf("") }
                    var despacharMedianos by remember { mutableStateOf("") }
                    var despacharGrandes by remember { mutableStateOf("") }
                    val pendientePequenos = pedido.cantidadPequenos - pedido.despachadoPequenos
                    val pendienteMedianos = pedido.cantidadMedianos - pedido.despachadoMedianos
                    val pendienteGrandes = pedido.cantidadGrandes - pedido.despachadoGrandes
                    val completado = pendientePequenos == 0 && pendienteMedianos == 0 && pendienteGrandes == 0
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
                            Text("Ahumadores Pequeños: ${pedido.cantidadPequenos} | Despachados: ${pedido.despachadoPequenos} | Pendiente: $pendientePequenos")
                            OutlinedTextField(
                                value = despacharPequenos,
                                onValueChange = { despacharPequenos = it },
                                label = { Text("Despachar Pequeños") },
                                enabled = pedido.estado == "Activo" && pendientePequenos > 0 && !completado,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text("Ahumadores Medianos: ${pedido.cantidadMedianos} | Despachados: ${pedido.despachadoMedianos} | Pendiente: $pendienteMedianos")
                            OutlinedTextField(
                                value = despacharMedianos,
                                onValueChange = { despacharMedianos = it },
                                label = { Text("Despachar Medianos") },
                                enabled = pedido.estado == "Activo" && pendienteMedianos > 0 && !completado,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text("Ahumadores Grandes: ${pedido.cantidadGrandes} | Despachados: ${pedido.despachadoGrandes} | Pendiente: $pendienteGrandes")
                            OutlinedTextField(
                                value = despacharGrandes,
                                onValueChange = { despacharGrandes = it },
                                label = { Text("Despachar Grandes") },
                                enabled = pedido.estado == "Activo" && pendienteGrandes > 0 && !completado,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text("Valor Total: \$${pedido.valorTotal}", style = MaterialTheme.typography.titleMedium, color = Color.Red)
                            Text("Fecha comprometida: ${pedido.fechaComprometida ?: "-"}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                            Text("Estado: ${pedido.estado}", color = if (pedido.estado == "Cancelado") Color.Red else if (completado) Color.Gray else Color(0xFF388E3C), style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val dPeq = despacharPequenos.toIntOrNull() ?: 0
                                    val dMed = despacharMedianos.toIntOrNull() ?: 0
                                    val dGra = despacharGrandes.toIntOrNull() ?: 0
                                    if (dPeq < 0 || dMed < 0 || dGra < 0) {
                                        mensajeSnackbar = "No puedes despachar cantidades negativas."
                                        return@Button
                                    }
                                    if (dPeq > pendientePequenos || dMed > pendienteMedianos || dGra > pendienteGrandes) {
                                        mensajeSnackbar = "No puedes despachar más de lo pendiente."
                                        return@Button
                                    }
                                    // Verificar stock antes de despachar
                                    val stockPeq = dbHelper.obtenerStockAhumador("Pequeño")
                                    val stockMed = dbHelper.obtenerStockAhumador("Mediano")
                                    val stockGra = dbHelper.obtenerStockAhumador("Grande")
                                    if (dPeq > stockPeq || dMed > stockMed || dGra > stockGra) {
                                        mensajeSnackbar = "No hay suficiente stock para despachar."
                                        return@Button
                                    }
                                    val exito = dbHelper.despacharPedido(
                                        pedido.id,
                                        pedido.despachadoPequenos + dPeq,
                                        pedido.despachadoMedianos + dMed,
                                        pedido.despachadoGrandes + dGra
                                    )
                                    if (exito) {
                                        // REGISTRAR MOVIMIENTOS DE SALIDA DE AHUMADORES
                                        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
                                        val fechaHoy = dateFormat.format(Date())
                                        if (dPeq > 0) {
                                            val movPeq = Movimiento(
                                                tipo = "Salida",
                                                fecha = fechaHoy,
                                                codigo = "Ahumador_1",
                                                cantidad = dPeq,
                                                valorUnitario = dbHelper.obtenerValorAhumador("Pequeño"),
                                                promedio = dbHelper.obtenerValorAhumador("Pequeño"),
                                                esAjuste = false
                                            )
                                            dbHelper.registrarMovimiento(movPeq)
                                        }
                                        if (dMed > 0) {
                                            val movMed = Movimiento(
                                                tipo = "Salida",
                                                fecha = fechaHoy,
                                                codigo = "Ahumador_2",
                                                cantidad = dMed,
                                                valorUnitario = dbHelper.obtenerValorAhumador("Mediano"),
                                                promedio = dbHelper.obtenerValorAhumador("Mediano"),
                                                esAjuste = false
                                            )
                                            dbHelper.registrarMovimiento(movMed)
                                        }
                                        if (dGra > 0) {
                                            val movGra = Movimiento(
                                                tipo = "Salida",
                                                fecha = fechaHoy,
                                                codigo = "Ahumador_3",
                                                cantidad = dGra,
                                                valorUnitario = dbHelper.obtenerValorAhumador("Grande"),
                                                promedio = dbHelper.obtenerValorAhumador("Grande"),
                                                esAjuste = false
                                            )
                                            dbHelper.registrarMovimiento(movGra)
                                        }
                                        mensajeSnackbar = "Despacho realizado."
                                        pedidos = dbHelper.obtenerPerfiles()
                                    } else {
                                        mensajeSnackbar = "No se pudo despachar."
                                    }
                                },
                                enabled = pedido.estado == "Activo" && !completado && (pendientePequenos > 0 || pendienteMedianos > 0 || pendienteGrandes > 0),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Despachar")
                            }
                            // Botón cancelar pedido
                            Button(
                                onClick = {
                                    pedidoAAnular = pedido
                                    mostrarDialogoAnulacion = true
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
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { Text("Pedidos Completados", style = MaterialTheme.typography.titleMedium, color = Color(0xFF388E3C)) }
            if (completados.isEmpty()) {
                item { Text("No hay pedidos completados.", color = Color.Gray) }
            } else {
                items(completados) { pedido ->
                    val cliente = clientes.find { it.id == pedido.clienteId }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Text("Cliente: ${cliente?.let { String.format("%03d", it.id) + " - " + it.nombre } ?: "Desconocido"}", style = MaterialTheme.typography.titleMedium, color = Color(0xFF3F51B5))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Pedido: ${pedido.nombre}")
                            Text("PEDIDO COMPLETADO", color = Color(0xFF388E3C), style = MaterialTheme.typography.titleMedium)
                            Text("Ahumadores Pequeños: ${pedido.cantidadPequenos} | Despachados: ${pedido.despachadoPequenos}")
                            Text("Ahumadores Medianos: ${pedido.cantidadMedianos} | Despachados: ${pedido.despachadoMedianos}")
                            Text("Ahumadores Grandes: ${pedido.cantidadGrandes} | Despachados: ${pedido.despachadoGrandes}")
                            Text("Valor Total: \$${pedido.valorTotal}", style = MaterialTheme.typography.titleMedium, color = Color.Red)
                            Text("Fecha comprometida: ${pedido.fechaComprometida ?: "-"}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { Text("Pedidos Anulados", style = MaterialTheme.typography.titleMedium, color = Color.Red) }
            if (anulados.isEmpty()) {
                item { Text("No hay pedidos anulados.", color = Color.Gray) }
            } else {
                items(anulados) { pedido ->
                    val cliente = clientes.find { it.id == pedido.clienteId }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Text("Cliente: ${cliente?.let { String.format("%03d", it.id) + " - " + it.nombre } ?: "Desconocido"}", style = MaterialTheme.typography.titleMedium, color = Color(0xFF3F51B5))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Pedido: ${pedido.nombre}")
                            Text("PEDIDO ANULADO", color = Color.Red, style = MaterialTheme.typography.titleMedium)
                            Text("Ahumadores Pequeños: ${pedido.cantidadPequenos} | Despachados: ${pedido.despachadoPequenos}")
                            Text("Ahumadores Medianos: ${pedido.cantidadMedianos} | Despachados: ${pedido.despachadoMedianos}")
                            Text("Ahumadores Grandes: ${pedido.cantidadGrandes} | Despachados: ${pedido.despachadoGrandes}")
                            Text("Valor Total: \$${pedido.valorTotal}", style = MaterialTheme.typography.titleMedium, color = Color.Red)
                            Text("Fecha comprometida: ${pedido.fechaComprometida ?: "-"}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoAnulacion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoAnulacion = false; razonAnulacion = "" },
            title = { Text("Cancelar Pedido") },
            text = {
                Column {
                    Text("Por favor, escribe la razón de anulación:")
                    OutlinedTextField(
                        value = razonAnulacion,
                        onValueChange = { razonAnulacion = it },
                        label = { Text("Razón de anulación") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pedidoAAnular?.let {
                            dbHelper.actualizarEstadoPedido(it.id, "Cancelado")
                            pedidos = dbHelper.obtenerPerfiles()
                            mensajeSnackbar = "Pedido anulado."
                        }
                        mostrarDialogoAnulacion = false
                        razonAnulacion = ""
                    }
                ) {
                    Text("Anular")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoAnulacion = false; razonAnulacion = "" }) {
                    Text("Cancelar")
                }
            }
        )
    }

    LaunchedEffect(mensajeSnackbar) {
        if (mensajeSnackbar.isNotBlank()) {
            snackbarHostState.showSnackbar(mensajeSnackbar)
            mensajeSnackbar = ""
        }
    }
} 