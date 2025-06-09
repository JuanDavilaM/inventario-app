package com.example.inventario.screens

data class Movimiento(
    val id: Int = 0,
    val tipo: String, // "Entrada" o "Salida"
    val fecha: String,
    val codigo: String,
    val cantidad: Int,
    val valorUnitario: Double,
    val promedio: Double
)
