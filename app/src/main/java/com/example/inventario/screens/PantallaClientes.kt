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
fun PantallaClientes(dbHelper: InventarioDBHelper, navController: NavHostController) {
    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var clientes by remember { mutableStateOf(dbHelper.obtenerClientes()) }

    Scaffold(
        topBar = {
            Header(title = "Registro de Clientes", navController = navController)
        },
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
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    if (nombre.isBlank() || telefono.isBlank() || correo.isBlank()) {
                        mensaje = "Completa todos los campos."
                    } else {
                        val insertado = dbHelper.insertarCliente(nombre, telefono, correo)
                        if (insertado) {
                            mensaje = "Cliente guardado exitosamente."
                            nombre = ""; telefono = ""; correo = ""
                            clientes = dbHelper.obtenerClientes()
                        } else {
                            mensaje = "Error al guardar cliente."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Cliente")
            }
            if (mensaje.isNotBlank()) {
                Text(mensaje, color = if (mensaje.contains("exitosamente")) Color(0xFF4CAF50) else Color.Red)
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }
    }
} 