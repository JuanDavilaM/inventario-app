package com.example.inventario.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.inventario.InventarioDBHelper
import com.example.inventario.screens.Perfil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfiles(dbHelper: InventarioDBHelper, navController: NavHostController) {
    val context = LocalContext.current

    var clientes by remember { mutableStateOf(dbHelper.obtenerClientes()) }
    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }
    var expandedClientes by remember { mutableStateOf(false) }

    var contadorPedidos by remember { mutableStateOf(dbHelper.obtenerPerfiles().size + 1) }
    var cantidadPequenos by remember { mutableStateOf("") }
    var cantidadMedianos by remember { mutableStateOf("") }
    var cantidadGrandes by remember { mutableStateOf("") }
    var valorTotal by remember { mutableStateOf("") }
    var mensajeSnackbar by remember { mutableStateOf("") }
    var valorPequeno by remember { mutableStateOf(0.0) }
    var valorMediano by remember { mutableStateOf(0.0) }
    var valorGrande by remember { mutableStateOf(0.0) }
    var fecha by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var fechaComprometida by remember { mutableStateOf("") }
    var showDatePickerComprometida by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // DatePickerDialog
    val calendar = java.util.Calendar.getInstance()
    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            fecha = "%02d/%02d/%04d".format(dayOfMonth, month + 1, year)
        },
        calendar.get(java.util.Calendar.YEAR),
        calendar.get(java.util.Calendar.MONTH),
        calendar.get(java.util.Calendar.DAY_OF_MONTH)
    )

    val datePickerDialogComprometida = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            fechaComprometida = "%02d/%02d/%04d".format(dayOfMonth, month + 1, year)
        },
        calendar.get(java.util.Calendar.YEAR),
        calendar.get(java.util.Calendar.MONTH),
        calendar.get(java.util.Calendar.DAY_OF_MONTH)
    )

    // Scaffold para la pantalla con barra superior
    Scaffold(
        topBar = {
            Header(
                title = "Perfiles de Clientes",
                navController = navController
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFE6F0FF)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Menú desplegable para seleccionar cliente
            ExposedDropdownMenuBox(expanded = expandedClientes, onExpandedChange = { expandedClientes = !expandedClientes }) {
                OutlinedTextField(
                    value = clienteSeleccionado?.let { String.format("%03d", it.id) + " - " + it.nombre } ?: "Selecciona un cliente",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Cliente") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedClientes) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
            )
                ExposedDropdownMenu(expanded = expandedClientes, onDismissRequest = { expandedClientes = false }) {
                    clientes.forEach { cliente ->
                        DropdownMenuItem(text = { Text(String.format("%03d", cliente.id) + " - " + cliente.nombre) }, onClick = {
                            clienteSeleccionado = cliente
                            expandedClientes = false
                        })
                    }
                }
            }

            Text("Número de Pedido: Pedido #$contadorPedidos", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(value = cantidadPequenos, onValueChange = { cantidadPequenos = it }, label = { Text("Cantidad de Ahumadores Pequeños") }, keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = cantidadMedianos, onValueChange = { cantidadMedianos = it }, label = { Text("Cantidad de Ahumadores Medianos") }, keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = cantidadGrandes, onValueChange = { cantidadGrandes = it }, label = { Text("Cantidad de Ahumadores Grandes") }, keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

            // Selección de fecha
            OutlinedTextField(
                value = fecha,
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha del Pedido") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            if (showDatePicker) {
                LaunchedEffect(Unit) {
                    datePickerDialog.show()
                    showDatePicker = false
                }
            }

            // Selección de fecha comprometida
            OutlinedTextField(
                value = fechaComprometida,
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha comprometida de entrega") },
                trailingIcon = {
                    IconButton(onClick = { showDatePickerComprometida = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha comprometida")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            if (showDatePickerComprometida) {
                LaunchedEffect(Unit) {
                    datePickerDialogComprometida.show()
                    showDatePickerComprometida = false
                }
            }

            // Obtener el valor unitario de cada tipo de ahumador
            if (cantidadPequenos.isNotEmpty()) valorPequeno = dbHelper.obtenerValorAhumador("Pequeño")
            if (cantidadMedianos.isNotEmpty()) valorMediano = dbHelper.obtenerValorAhumador("Mediano")
            if (cantidadGrandes.isNotEmpty()) valorGrande = dbHelper.obtenerValorAhumador("Grande")

            val cantidadPequenosInt = cantidadPequenos.toIntOrNull() ?: 0
            val cantidadMedianosInt = cantidadMedianos.toIntOrNull() ?: 0
            val cantidadGrandesInt = cantidadGrandes.toIntOrNull() ?: 0

            val totalPequenos = valorPequeno * cantidadPequenosInt
            val totalMedianos = valorMediano * cantidadMedianosInt
            val totalGrandes = valorGrande * cantidadGrandesInt

            val total = totalPequenos + totalMedianos + totalGrandes
            valorTotal = String.format("%.2f", total)

            Text(
                "Valor Total: \$${valorTotal}",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Red,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Button(
                onClick = {
                    val fechaFinal = if (fecha.isNotBlank()) fecha else {
                        val cal = java.util.Calendar.getInstance()
                        "%02d/%02d/%04d".format(
                            cal.get(java.util.Calendar.DAY_OF_MONTH),
                            cal.get(java.util.Calendar.MONTH) + 1,
                            cal.get(java.util.Calendar.YEAR)
                        )
                    }
                    if (clienteSeleccionado == null) {
                        mensajeSnackbar = "Selecciona un cliente."
                    } else if (cantidadPequenos.isBlank() || cantidadMedianos.isBlank() || cantidadGrandes.isBlank()) {
                        mensajeSnackbar = "Por favor complete todos los campos."
                    } else {
                        val nombrePedido = "Pedido #$contadorPedidos"
                        val insertado = dbHelper.insertarPerfil(
                            clienteId = clienteSeleccionado!!.id,
                            nombre = nombrePedido,
                            cantidadPequenos = cantidadPequenosInt,
                            cantidadMedianos = cantidadMedianosInt,
                            cantidadGrandes = cantidadGrandesInt,
                            valorTotal = total,
                            fecha = fechaFinal,
                            fechaComprometida = fechaComprometida
                        )
                        if (insertado) {
                            contadorPedidos = dbHelper.obtenerPerfiles().size + 1
                            cantidadPequenos = ""
                            cantidadMedianos = ""
                            cantidadGrandes = ""
                            valorTotal = "0.00"
                            fecha = ""
                            fechaComprometida = ""
                            mensajeSnackbar = "Perfil guardado exitosamente."
                        } else {
                            mensajeSnackbar = "Error al guardar perfil."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Pedido")
            }

            Button(
                onClick = { navController.navigate("lista_pedidos") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Ver Lista de Pedidos")
            }

            LaunchedEffect(mensajeSnackbar) {
                if (mensajeSnackbar.isNotBlank()) {
                    snackbarHostState.showSnackbar(mensajeSnackbar)
                    mensajeSnackbar = ""
                }
            }
        }
    }
}
