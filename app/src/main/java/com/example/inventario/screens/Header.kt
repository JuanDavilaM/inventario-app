package com.example.inventario.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Header(
    title: String,
    navController: NavHostController,
    showBackButton: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = Color.White)
                Spacer(modifier = Modifier.width(12.dp))
                
            }
        },
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
                    // Secciones principales
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
                        text = { Text("Pedidos") },
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
                    DropdownMenuItem(
                        text = { Text("Despachador") },
                        onClick = {
                            expanded = false
                            navController.navigate("despachador")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Cartera") },
                        onClick = {
                            expanded = false
                            navController.navigate("cartera")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Exportar Datos") },
                        onClick = {
                            expanded = false
                            navController.navigate("exportar")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Convertir Ahumadores") },
                        onClick = {
                            expanded = false
                            navController.navigate("calcular")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Cálculo Faltante") },
                        onClick = {
                            expanded = false
                            navController.navigate("calculo_faltante")
                        }
                    )
                    Divider()
                    // Submenú de Listas
                    var expandedListas by remember { mutableStateOf(false) }
                    DropdownMenuItem(
                        text = { Text("Listas ▶") },
                        onClick = {
                            expandedListas = true
                        }
                    )
                    DropdownMenu(
                        expanded = expandedListas,
                        onDismissRequest = { expandedListas = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Lista de Artículos") },
                            onClick = {
                                expandedListas = false
                                expanded = false
                                navController.navigate("lista")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Lista de Ahumadores") },
                            onClick = {
                                expandedListas = false
                                expanded = false
                                navController.navigate("pantalla_lista_asadores")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Lista de Movimientos") },
                            onClick = {
                                expandedListas = false
                                expanded = false
                                navController.navigate("lista_movimientos")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Lista de Pedidos") },
                            onClick = {
                                expandedListas = false
                                expanded = false
                                navController.navigate("lista_pedidos")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Lista de Perfiles") },
                            onClick = {
                                expandedListas = false
                                expanded = false
                                navController.navigate("lista_perfiles")
                            }
                        )
                    }
                }
            }
        }
    )
}
