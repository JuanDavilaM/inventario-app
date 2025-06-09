package com.example.inventario.screens

data class Articulo(
    val codigo: String,
    val nombre: String,
    val valor: Double,
    val existencia: Int,
    val tipoAsador: String, // nuevo campo
    val unidadesNecesarias: Int

)
