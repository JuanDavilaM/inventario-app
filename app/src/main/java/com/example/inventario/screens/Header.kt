package com.example.inventario.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Header(
    title: String,
    navController: NavHostController,
    showBackButton: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(title, color = Color.White) },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F51B5)),
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Regresar",
                        tint = Color.White
                    )
                }
            } else {
                // Mostrar el ícono de menú cuando no se muestra el botón de retroceso
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menú",
                        tint = Color.White
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Inventario") },
                        onClick = {
                            expanded = false
                            navController.navigate("inventario")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Movimientos") },
                        onClick = {
                            expanded = false
                            navController.navigate("movimientos")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Lista de Artículos") },
                        onClick = {
                            expanded = false
                            navController.navigate("lista")
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Clientes") },
                        onClick = {
                            expanded = false
                            navController.navigate("clientes")
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Lista de Asadores") },
                        onClick = {
                            expanded = false
                            navController.navigate("pantalla_lista_asadores")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Calcular Asadores") },
                        onClick = {
                            expanded = false
                            navController.navigate("calcular")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Perfiles") },
                        onClick = {
                            expanded = false
                            navController.navigate("perfiles")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Clientes") },
                        onClick = {
                            expanded = false
                            navController.navigate("clientes")
                        }
                    )
                }
            }
        }
    )
}
