package com.example.inventario.screens

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.inventario.InventarioDBHelper
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaExportar(dbHelper: InventarioDBHelper, navController: NavHostController) {
    val context = LocalContext.current
    var mensajeSnackbar by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    var exportando by remember { mutableStateOf(false) }
    var restaurando by remember { mutableStateOf(false) }

    // Launcher para seleccionar archivo Excel
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                restaurando = true
                try {
                    restaurarDesdeExcel(context, dbHelper, uri)
                    mensajeSnackbar = "Restauración exitosa"
                } catch (e: Exception) {
                    mensajeSnackbar = "Error al restaurar: ${e.message}"
                } finally {
                    restaurando = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Header(title = "Exportar Datos", navController = navController)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFE6F0FF)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Exportar datos a Excel",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF3F51B5)
            )

            Text(
                "Esta función creará un archivo Excel con todos los datos de la aplicación, incluyendo:\n" +
                "• Inventario\n" +
                "• Clientes\n" +
                "• Pedidos\n" +
                "• Pagos\n" +
                "• Movimientos",
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = {
                    exportando = true
                    try {
                        exportarDatos(context, dbHelper)
                        mensajeSnackbar = "Datos exportados exitosamente"
                    } catch (e: Exception) {
                        mensajeSnackbar = "Error al exportar datos: ${e.message}"
                    } finally {
                        exportando = false
                    }
                },
                enabled = !exportando && !restaurando,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (exportando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Exportar Datos")
                }
            }

            // Botón para restaurar
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    }
                    launcher.launch(intent)
                },
                enabled = !restaurando && !exportando,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (restaurando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Restaurar desde Excel")
                }
            }
        }

        LaunchedEffect(mensajeSnackbar) {
            if (mensajeSnackbar.isNotBlank()) {
                snackbarHostState.showSnackbar(mensajeSnackbar)
                mensajeSnackbar = ""
            }
        }
    }
}

private fun exportarDatos(context: Context, dbHelper: InventarioDBHelper) {
    val workbook = XSSFWorkbook()
    val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    val timestamp = dateFormat.format(Date())
    val fileName = "Inventario_Backup_$timestamp.xlsx"

    // Hoja de Inventario
    val sheetInventario = workbook.createSheet("Inventario")
    val articulos = dbHelper.obtenerTodos()
    var rowNum = 0
    var headerRow = sheetInventario.createRow(rowNum++)
    headerRow.createCell(0).setCellValue("Código")
    headerRow.createCell(1).setCellValue("Nombre")
    headerRow.createCell(2).setCellValue("Existencia")
    headerRow.createCell(3).setCellValue("Valor Unitario")
    headerRow.createCell(4).setCellValue("Tipo Asador")
    headerRow.createCell(5).setCellValue("Unidades Necesarias")

    articulos.forEach { articulo ->
        val row = sheetInventario.createRow(rowNum++)
        row.createCell(0).setCellValue(articulo.codigo)
        row.createCell(1).setCellValue(articulo.nombre)
        row.createCell(2).setCellValue(articulo.existencia.toDouble())
        row.createCell(3).setCellValue(articulo.valor.toDouble())
        row.createCell(4).setCellValue(articulo.tipoAsador)
        row.createCell(5).setCellValue(articulo.unidadesNecesarias.toDouble())
    }

    // Hoja de Clientes
    val sheetClientes = workbook.createSheet("Clientes")
    val clientes = dbHelper.obtenerClientes()
    rowNum = 0
    headerRow = sheetClientes.createRow(rowNum++)
    headerRow.createCell(0).setCellValue("ID")
    headerRow.createCell(1).setCellValue("Nombre")
    headerRow.createCell(2).setCellValue("Teléfono")
    headerRow.createCell(3).setCellValue("Correo")

    clientes.forEach { cliente ->
        val row = sheetClientes.createRow(rowNum++)
        row.createCell(0).setCellValue(cliente.id.toDouble())
        row.createCell(1).setCellValue(cliente.nombre)
        row.createCell(2).setCellValue(cliente.telefono)
        row.createCell(3).setCellValue(cliente.correo)
    }

    // Hoja de Pedidos
    val sheetPedidos = workbook.createSheet("Pedidos")
    val pedidos = dbHelper.obtenerPerfiles()
    rowNum = 0
    headerRow = sheetPedidos.createRow(rowNum++)
    headerRow.createCell(0).setCellValue("ID")
    headerRow.createCell(1).setCellValue("Cliente ID")
    headerRow.createCell(2).setCellValue("Nombre")
    headerRow.createCell(3).setCellValue("Estado")
    headerRow.createCell(4).setCellValue("Valor Total")
    headerRow.createCell(5).setCellValue("Fecha Comprometida")

    pedidos.forEach { pedido ->
        val row = sheetPedidos.createRow(rowNum++)
        row.createCell(0).setCellValue(pedido.id.toDouble())
        row.createCell(1).setCellValue(pedido.clienteId.toDouble())
        row.createCell(2).setCellValue(pedido.nombre)
        row.createCell(3).setCellValue(pedido.estado)
        row.createCell(4).setCellValue(pedido.valorTotal.toDouble())
        row.createCell(5).setCellValue(pedido.fechaComprometida)
    }

    // Guardar el archivo en la carpeta Download
    val downloadsDir = context.getExternalFilesDir(null)?.parentFile?.parentFile?.resolve("Download")
    val file = File(downloadsDir, fileName)
    FileOutputStream(file).use { outputStream ->
        workbook.write(outputStream)
    }

    workbook.close()
    
    // Mostrar la ruta del archivo
    val filePath = file.absolutePath
    throw Exception("Archivo guardado en: $filePath")
}

private fun restaurarDesdeExcel(context: Context, dbHelper: InventarioDBHelper, uri: Uri) {
    val inputStream = context.contentResolver.openInputStream(uri)
    val workbook = XSSFWorkbook(inputStream)

    // --- BORRAR DATOS EXISTENTES ---
    val db = dbHelper.writableDatabase
    db.delete("articulos", null, null)
    db.delete("clientes", null, null)
    db.delete("perfiles", null, null)

    // --- RESTAURAR INVENTARIO ---
    val sheetInventario = workbook.getSheet("Inventario")
    for (rowIndex in 1..sheetInventario.lastRowNum) { // Saltar encabezado
        val row = sheetInventario.getRow(rowIndex)
        if (row != null) {
            val codigo = row.getCell(0)?.stringCellValue ?: ""
            val nombre = row.getCell(1)?.stringCellValue ?: ""
            val existencia = row.getCell(2)?.numericCellValue?.toInt() ?: 0
            val valor = row.getCell(3)?.numericCellValue ?: 0.0
            val tipoAsador = row.getCell(4)?.stringCellValue ?: ""
            val unidadesNecesarias = row.getCell(5)?.numericCellValue?.toInt() ?: 0
            val articulo = Articulo(codigo, nombre, valor, existencia, tipoAsador, unidadesNecesarias)
            dbHelper.insertarArticulo(articulo)
        }
    }

    // --- RESTAURAR CLIENTES ---
    val sheetClientes = workbook.getSheet("Clientes")
    for (rowIndex in 1..sheetClientes.lastRowNum) {
        val row = sheetClientes.getRow(rowIndex)
        if (row != null) {
            val id = row.getCell(0)?.numericCellValue?.toInt() ?: 0
            val nombre = row.getCell(1)?.stringCellValue ?: ""
            val telefono = row.getCell(2)?.stringCellValue ?: ""
            val correo = row.getCell(3)?.stringCellValue ?: ""
            dbHelper.insertarCliente(nombre, telefono, correo)
        }
    }

    // --- RESTAURAR PEDIDOS (Perfiles) ---
    val sheetPedidos = workbook.getSheet("Pedidos")
    for (rowIndex in 1..sheetPedidos.lastRowNum) {
        val row = sheetPedidos.getRow(rowIndex)
        if (row != null) {
            val id = row.getCell(0)?.numericCellValue?.toInt() ?: 0
            val clienteId = row.getCell(1)?.numericCellValue?.toInt() ?: 0
            val nombre = row.getCell(2)?.stringCellValue ?: ""
            val estado = row.getCell(3)?.stringCellValue ?: ""
            val valorTotal = row.getCell(4)?.numericCellValue ?: 0.0
            val fechaComprometida = row.getCell(5)?.stringCellValue ?: ""
            dbHelper.insertarPerfil(
                clienteId = clienteId,
                nombre = nombre,
                cantidadPequenos = 0,
                cantidadMedianos = 0,
                cantidadGrandes = 0,
                valorTotal = valorTotal,
                fecha = "",
                fechaComprometida = fechaComprometida,
                despachadoPequenos = 0,
                despachadoMedianos = 0,
                despachadoGrandes = 0,
                estado = estado
            )
        }
    }

    workbook.close()
    inputStream?.close()
} 