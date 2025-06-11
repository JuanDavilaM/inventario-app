package com.example.inventario.screens

// Clase que representa un Asador
data class Asador(
    val codigo: String,          // Código único para el asador
    val nombre: String,          // Nombre del asador
    val valor: Double,           // Valor del asador
    val existencia: Int         // Cantidad de asadores disponibles en inventario
)
