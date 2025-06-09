package com.example.inventario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.inventario.navigation.AppNavHost
import com.example.inventario.ui.theme.InventarioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Instancia de base de datos
        val dbHelper = InventarioDBHelper(this)

        setContent {
            InventarioTheme {
                val navController = rememberNavController()
                AppNavHost(
                    navController = navController,
                    dbHelper = dbHelper
                )
            }
        }
    }
}
