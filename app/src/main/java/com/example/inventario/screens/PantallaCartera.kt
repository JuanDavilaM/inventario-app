package com.example.inventario.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.inventario.InventarioDBHelper


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCartera(dbHelper: InventarioDBHelper, navController: NavHostController) {
    val clientes = remember { dbHelper.obtenerClientes() }
    val perfiles = remember { dbHelper.obtenerPerfiles() }
    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }
    var mostrarPagos by remember { mutableStateOf(false) }
    var pagosCliente by remember { mutableStateOf(listOf<Map<String, Any?>>()) }
    var mostrarDialogoPago by remember { mutableStateOf(false) }
    var montoPago by remember { mutableStateOf("") }
    var fechaPago by remember { mutableStateOf("") }
    var observacionesPago by remember { mutableStateOf("") }
    var mensajeSnackbar by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val hoy = remember { java.text.SimpleDateFormat("dd/MM/yyyy").format(java.util.Date()) }
    var pedidoSeleccionadoId by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            Header(title = "Cartera de Clientes", navController = navController)
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
            Text("Clientes y Deudas", style = MaterialTheme.typography.titleMedium, color = Color(0xFF3F51B5))
            if (clientes.isEmpty()) {
                Text("No hay clientes registrados.", color = Color.Gray)
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(clientes) { cliente ->
                        val pedidosCliente = perfiles.filter { it.clienteId == cliente.id && it.estado != "Completado" && it.estado != "Cancelado" }
                        val totalPedidos = pedidosCliente.sumOf { it.valorTotal }
                        val totalPagos = dbHelper.obtenerPagosPorCliente(cliente.id).sumOf { (it["monto"] as? Double) ?: 0.0 }
                        val saldoPendiente = totalPedidos - totalPagos
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = if (saldoPendiente > 0) Color(0xFFFFF3E0) else Color(0xFFE8F5E9))
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            ) {
                                Text("Cliente: ${String.format("%03d", cliente.id)} - ${cliente.nombre}", style = MaterialTheme.typography.titleMedium)
                                Text("Total pedidos activos: $${"%.2f".format(totalPedidos)}")
                                Text("Pagos realizados: $${"%.2f".format(totalPagos)}")
                                Text("Saldo pendiente: $${"%.2f".format(saldoPendiente)}", color = if (saldoPendiente > 0) Color.Red else Color(0xFF388E3C))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    Button(onClick = {
                                        clienteSeleccionado = cliente
                                        pagosCliente = dbHelper.obtenerPagosPorCliente(cliente.id)
                                        mostrarPagos = true
                                    }) {
                                        Text("Ver Pagos")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(onClick = {
                                        clienteSeleccionado = cliente
                                        mostrarDialogoPago = true
                                        montoPago = ""
                                        fechaPago = hoy
                                        observacionesPago = ""
                                    }) {
                                        Text("Registrar Pago")
                                    }
                                }
                                // Pedidos atrasados
                                val pedidosAtrasados = perfiles.filter {
                                    it.clienteId == cliente.id &&
                                    it.estado != "Completado" && it.estado != "Cancelado" &&
                                    !it.fechaComprometida.isNullOrBlank() &&
                                    try {
                                        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy")
                                        val fechaComp = sdf.parse(it.fechaComprometida)
                                        val hoyDate = sdf.parse(hoy)
                                        fechaComp != null && hoyDate != null && fechaComp.before(hoyDate)
                                    } catch (e: Exception) { false }
                                }
                                if (pedidosAtrasados.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Pedidos Atrasados:", color = Color.Red, style = MaterialTheme.typography.bodyMedium)
                                    pedidosAtrasados.forEach { pedido ->
                                        Text("- ${pedido.nombre} (Entrega: ${pedido.fechaComprometida})", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (mostrarPagos && clienteSeleccionado != null) {
            AlertDialog(
                onDismissRequest = { mostrarPagos = false },
                title = { Text("Pagos de ${clienteSeleccionado?.nombre}") },
                text = {
                    if (pagosCliente.isEmpty()) {
                        Text("No hay pagos registrados para este cliente.")
                    } else {
                        Column {
                            pagosCliente.forEach { pago ->
                                val pedidoId = pago["pedidoId"] as? Int ?: 0
                                val nombrePedido = if (pedidoId != 0) {
                                    perfiles.find { it.id == pedidoId }?.nombre ?: "Pedido #$pedidoId"
                                } else {
                                    "Sin pedido"
                                }
                                Text("$nombrePedido | Monto: $${"%.2f".format(pago["monto"] as? Double ?: 0.0)} | Fecha: ${pago["fecha"] ?: "-"}")
                                if (!pago["observaciones"].toString().isNullOrBlank()) {
                                    Text("Obs: ${pago["observaciones"]}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Divider()
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { mostrarPagos = false }) {
                        Text("Cerrar")
                    }
                }
            )
        }
        if (mostrarDialogoPago && clienteSeleccionado != null) {
            val pedidosActivos = perfiles.filter { it.clienteId == clienteSeleccionado!!.id && it.estado != "Completado" && it.estado != "Cancelado" }
            AlertDialog(
                onDismissRequest = { mostrarDialogoPago = false },
                title = { Text("Registrar Pago para ${clienteSeleccionado?.nombre}") },
                text = {
                    Column {
                        // Menú desplegable para seleccionar pedido
                        var expanded by remember { mutableStateOf(false) }
                        val nombrePedidoSeleccionado = if (pedidoSeleccionadoId != 0) {
                            pedidosActivos.find { it.id == pedidoSeleccionadoId }?.nombre ?: "Sin pedido"
                        } else {
                            "Sin pedido"
                        }
                        Box(Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = nombrePedidoSeleccionado,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Pedido") },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    IconButton(onClick = { expanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Seleccionar pedido")
                                    }
                                }
                            )
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Sin pedido") },
                                    onClick = {
                                        pedidoSeleccionadoId = 0
                                        expanded = false
                                    }
                                )
                                pedidosActivos.forEach { pedido ->
                                    DropdownMenuItem(
                                        text = { Text(pedido.nombre) },
                                        onClick = {
                                            pedidoSeleccionadoId = pedido.id
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = montoPago,
                            onValueChange = { montoPago = it },
                            label = { Text("Monto") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = fechaPago,
                            onValueChange = { fechaPago = it },
                            label = { Text("Fecha (dd/MM/yyyy)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = observacionesPago,
                            onValueChange = { observacionesPago = it },
                            label = { Text("Observaciones") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val monto = montoPago.toDoubleOrNull() ?: 0.0
                        if (monto <= 0.0) {
                            mensajeSnackbar = "El monto debe ser mayor a 0."
                            return@TextButton
                        }
                        val exito = dbHelper.insertarPago(
                            clienteId = clienteSeleccionado!!.id,
                            pedidoId = pedidoSeleccionadoId,
                            monto = monto,
                            fecha = fechaPago,
                            observaciones = observacionesPago
                        )
                        if (exito) {
                            mensajeSnackbar = "Pago registrado."
                        } else {
                            mensajeSnackbar = "Error al registrar pago."
                        }
                        mostrarDialogoPago = false
                    }) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoPago = false }) {
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
} 