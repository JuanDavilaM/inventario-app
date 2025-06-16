package com.example.inventario.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.inventario.InventarioDBHelper
import com.example.inventario.screens.*


@Composable
fun AppNavHost(
    navController: NavHostController,
    dbHelper: InventarioDBHelper
) {
    NavHost(
        navController = navController,
        startDestination = "pantalla1" // Cambiar a la pantalla inicial de tu preferencia
    ) {
        composable("pantalla1") {
            Pantalla1(navController)
        }
        composable("inventario") {
            PantallaInventario(dbHelper, navController)
        }
        composable("movimientos") {
            PantallaMovimientos(dbHelper, navController)
        }
        composable("historial") {
            PantallaHistorial(dbHelper, navController)
        }
        composable("lista") {
            PantallaListaArticulos(dbHelper, navController)
        }
        composable("calcular") {
            PantallaCalcularAsadores(dbHelper, navController)
        }
        // Agregar la pantalla de Lista de Asadores
        composable("pantalla_lista_asadores") {
            PantallaListaAsadores(dbHelper, navController)
        }

        composable("perfiles") {
            PantallaPerfiles(dbHelper, navController)
        }

        composable("lista_pedidos") {
            PantallaListaPedidos(dbHelper, navController)
        }

        composable("despachador") {
            PantallaDespachador(dbHelper, navController)
        }

        composable("clientes") {
            PantallaClientes(dbHelper, navController)
        }

        composable("cartera") {
            PantallaCartera(dbHelper, navController)
        }

        composable("calculo_faltante") {
            PantallaCalculoFaltante(dbHelper, navController)
        }

        composable("exportar") {
            PantallaExportar(dbHelper, navController)
        }

        composable("lista_movimientos") {
            PantallaListaMovimientos(dbHelper, navController)
        }

        composable("lista_perfiles") {
    PantallaListaPerfiles(dbHelper, navController)
    }

    }
}
