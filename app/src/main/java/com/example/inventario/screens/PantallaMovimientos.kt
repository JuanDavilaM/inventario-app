package com.example.inventario.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.inventario.InventarioDBHelper
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMovimientos(dbHelper: InventarioDBHelper, navController: NavHostController) {
    val context = LocalContext.current

    var tipo by remember { mutableStateOf("Entrada") }
    var tipoExpanded by remember { mutableStateOf(false) }
    val tipos = listOf("Entrada", "Salida")

    var fecha by remember { mutableStateOf("") }
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, day: Int ->
            fecha = "$day/${month + 1}/$year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val articulos = remember { dbHelper.obtenerTodos() }
    var articuloSeleccionado by remember { mutableStateOf<Articulo?>(null) }
    var articuloExpanded by remember { mutableStateOf(false) }

    var cantidad by remember { mutableStateOf("") }
    var valorUnitario by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Header(
                title = "Movimientos",
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
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Registrar Movimiento", style = MaterialTheme.typography.headlineSmall)
            }

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(expanded = tipoExpanded, onExpandedChange = { tipoExpanded = !tipoExpanded }) {
                OutlinedTextField(
                    readOnly = true,
                    value = tipo,
                    onValueChange = {},
                    label = { Text("Tipo de Movimiento") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tipoExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = tipoExpanded, onDismissRequest = { tipoExpanded = false }) {
                    tipos.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion) },
                            onClick = {
                                tipo = opcion
                                tipoExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = fecha,
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(expanded = articuloExpanded, onExpandedChange = { articuloExpanded = !articuloExpanded }) {
                OutlinedTextField(
                    readOnly = true,
                    value = articuloSeleccionado?.codigo ?: "",
                    onValueChange = {},
                    label = { Text("Código del Artículo") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = articuloExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = articuloExpanded, onDismissRequest = { articuloExpanded = false }) {
                    articulos.forEach { articulo ->
                        DropdownMenuItem(
                            text = { Text("${articulo.codigo} - ${articulo.nombre} (${articulo.tipoAsador})") },
                            onClick = {
                                articuloSeleccionado = articulo
                                valorUnitario = articulo.valor.toString() // autollenar valor
                                articuloExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = cantidad,
                onValueChange = { cantidad = it },
                label = { Text("Cantidad") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = valorUnitario,
                onValueChange = { valorUnitario = it },
                label = { Text("Valor Unitario") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                val mov = Movimiento(
                    tipo = tipo,
                    fecha = fecha,
                    codigo = articuloSeleccionado?.codigo ?: "",
                    cantidad = cantidad.toIntOrNull() ?: 0,
                    valorUnitario = valorUnitario.toDoubleOrNull() ?: 0.0,
                    promedio = valorUnitario.toDoubleOrNull() ?: 0.0
                )
                if (dbHelper.registrarMovimiento(mov)) {
                    fecha = ""
                    articuloSeleccionado = null
                    cantidad = ""
                    valorUnitario = ""
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Registrar Movimiento")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = { navController.navigate("inventario") }, modifier = Modifier.fillMaxWidth()) {
                Text("Volver al Inventario")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = { navController.navigate("historial") }, modifier = Modifier.fillMaxWidth()) {
                Text("Ver Historial de Movimientos")
            }
        }
    }
}
