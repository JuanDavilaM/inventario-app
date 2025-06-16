package com.example.inventario.screens

data class Movimiento(
    val id: Int = 0,
    val tipo: String, // "Entrada", "Salida" o "Ajuste"
    val fecha: String,
    val codigo: String,
    val cantidad: Int,
    val valorUnitario: Double,
    val promedio: Double,
    val esAjuste: Boolean = false // Nuevo campo para indicar si es un ajuste de inventario
)
