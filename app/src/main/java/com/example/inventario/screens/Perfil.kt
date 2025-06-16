package com.example.inventario.screens

data class Perfil(
    val id: Int,
    val clienteId: Int,
    val nombre: String,
    val cantidadPequenos: Int,
    val cantidadMedianos: Int,
    val cantidadGrandes: Int,
    val valorTotal: Double,
    val fecha: String,
    val despachadoPequenos: Int = 0,
    val despachadoMedianos: Int = 0,
    val despachadoGrandes: Int = 0,
    val fechaComprometida: String? = null,
    val estado: String = "Activo"
)
