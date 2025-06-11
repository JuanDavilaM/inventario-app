package com.example.inventario.screens

data class Perfil(
    val id: Int,
    val clienteId: Int,
    val nombre: String,
    val cantidadPequenos: Int,
    val cantidadMedianos: Int,
    val cantidadGrandes: Int,
    val valorTotal: Double,
    val fecha: String
)
