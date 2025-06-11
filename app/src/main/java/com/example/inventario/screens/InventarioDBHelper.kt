package com.example.inventario

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.inventario.screens.Articulo
import com.example.inventario.screens.Asador
import com.example.inventario.screens.Movimiento
import com.example.inventario.screens.Perfil
import com.example.inventario.screens.Cliente

class InventarioDBHelper(context: Context) :
    SQLiteOpenHelper(context, "InventarioDB", null, 10) { // Actualiza la versión de la base de datos a 10

    override fun onCreate(db: SQLiteDatabase) {
        // Crear la tabla de artículos (para artículos regulares)
        db.execSQL(
            """CREATE TABLE articulos (
                codigo TEXT PRIMARY KEY,
                nombre TEXT,
                valor REAL,
                existencia INTEGER,
                tipoAsador TEXT,
                unidadesNecesarias INTEGER
            )"""
        )

        // Crear la tabla de movimientos
        db.execSQL(
            """CREATE TABLE movimientos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tipo TEXT,
                fecha TEXT,
                codigo TEXT,
                cantidad INTEGER,
                valorUnitario REAL,
                promedio REAL
            )"""
        )

        // Crear la tabla de asadores (solo para ahumadores creados)
        db.execSQL(
            """CREATE TABLE asadores (
                codigo TEXT PRIMARY KEY,
                nombre TEXT,
                valor REAL,
                existencia INTEGER,
                tipoAsador TEXT
            )"""
        )
        db.execSQL(
            """CREATE TABLE clientes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT,
                telefono TEXT,
                correo TEXT
            )"""
        )

        // Crear la tabla de perfiles (para los clientes y su historial de compras)
        db.execSQL(
            """CREATE TABLE perfiles (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                clienteId INTEGER,
                nombre TEXT,
                cantidadPequenos INTEGER,
                cantidadMedianos INTEGER,
                cantidadGrandes INTEGER,
                valorTotal REAL,
                fecha TEXT
            )"""
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 8) {
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS clientes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT,
                    telefono TEXT,
                    correo TEXT
                )"""
            )
        }
        if (oldVersion < 9) {
            try {
                db.execSQL("ALTER TABLE perfiles ADD COLUMN clienteId INTEGER DEFAULT 0")
            } catch (e: Exception) {}
        }
        if (oldVersion < 10) {
            try {
                db.execSQL("ALTER TABLE perfiles ADD COLUMN fecha TEXT DEFAULT ''")
            } catch (e: Exception) {}
        }
        // Actualiza la base de datos si es necesario
        if (oldVersion < 7) {  // Si la versión es menor que 7, actualiza creando la tabla 'perfiles'
            try {
                // Primero eliminamos la tabla si existe
                db.execSQL("DROP TABLE IF EXISTS perfiles")

                // Creamos la tabla perfiles
                db.execSQL(
                    """CREATE TABLE perfiles (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        clienteId INTEGER,
                        nombre TEXT,
                        cantidadPequenos INTEGER,
                        cantidadMedianos INTEGER,
                        cantidadGrandes INTEGER,
                        valorTotal REAL,
                        fecha TEXT
                    )"""
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Obtener todos los artículos (para artículos regulares)
    fun obtenerTodos(): List<Articulo> {
        val lista = mutableListOf<Articulo>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM articulos", null)

        while (cursor.moveToNext()) {
            lista.add(
                Articulo(
                    codigo = cursor.getString(cursor.getColumnIndexOrThrow("codigo")),
                    nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                    valor = cursor.getDouble(cursor.getColumnIndexOrThrow("valor")),
                    existencia = cursor.getInt(cursor.getColumnIndexOrThrow("existencia")),
                    tipoAsador = cursor.getString(cursor.getColumnIndexOrThrow("tipoAsador")) ?: "",
                    unidadesNecesarias = cursor.getInt(cursor.getColumnIndexOrThrow("unidadesNecesarias"))
                )
            )
        }
        cursor.close()
        return lista
    }

    // Agregar los ahumadores creados al inventario (en la tabla de asadores)
    fun agregarAhumadoresCreados(cantidadAsadores: Int, tipoAsador: String, tipoCodigo: Int, valorAsador: Double): Boolean {
        val db = writableDatabase
        val codigoAsador = "Ahumador_$tipoCodigo"

        // Primero verificamos si ya existe un asador de este tipo
        val cursor = db.rawQuery("SELECT * FROM asadores WHERE codigo = ?", arrayOf(codigoAsador))

        if (cursor.moveToFirst()) {
            // Si existe, actualizamos la cantidad sumando y actualizamos el valor
            val existenciaActual = cursor.getInt(cursor.getColumnIndexOrThrow("existencia"))
            val values = ContentValues().apply {
                put("existencia", existenciaActual + cantidadAsadores)
                put("valor", valorAsador) // Actualizamos al nuevo valor
            }
            cursor.close()
            val filasAfectadas = db.update("asadores", values, "codigo = ?", arrayOf(codigoAsador))
            return filasAfectadas > 0
        } else {
            // Si no existe, creamos un nuevo registro
            cursor.close()
            val values = ContentValues().apply {
                put("codigo", codigoAsador)
                put("nombre", "Ahumador tipo $tipoAsador")
                put("valor", valorAsador)
                put("existencia", cantidadAsadores)
                put("tipoAsador", tipoAsador)
            }
            val resultado = db.insert("asadores", null, values)
            return resultado != -1L
        }
    }

    // Actualiza la existencia de un artículo (en la tabla de artículos)
    fun actualizarExistencia(codigo: String, nuevaExistencia: Int): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("existencia", nuevaExistencia)
        }
        val filasAfectadas = db.update("articulos", values, "codigo = ?", arrayOf(codigo))
        return filasAfectadas > 0
    }

    // Eliminar artículo por código (en la tabla de artículos)
    fun eliminarArticuloPorCodigo(codigo: String): Boolean {
        val db = writableDatabase
        val filasAfectadas = db.delete("articulos", "codigo = ?", arrayOf(codigo))
        return filasAfectadas > 0
    }

    // Registrar un movimiento
    fun registrarMovimiento(mov: Movimiento): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("tipo", mov.tipo)
            put("fecha", mov.fecha)
            put("codigo", mov.codigo)
            put("cantidad", mov.cantidad)
            put("valorUnitario", mov.valorUnitario)
            put("promedio", mov.promedio)
        }
        val result = db.insert("movimientos", null, values)

        val signo = if (mov.tipo == "Entrada") 1 else -1
        db.execSQL(
            "UPDATE articulos SET existencia = existencia + (${signo} * ${mov.cantidad}) WHERE codigo = ?",
            arrayOf(mov.codigo)
        )

        return result != -1L
    }

    // Obtener movimientos por código
    fun obtenerMovimientosPorCodigo(filtro: String): List<Movimiento> {
        val lista = mutableListOf<Movimiento>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM movimientos WHERE codigo LIKE ?", arrayOf("%$filtro%"))
        while (cursor.moveToNext()) {
            lista.add(
                Movimiento(
                    id = cursor.getInt(0),
                    tipo = cursor.getString(1),
                    fecha = cursor.getString(2),
                    codigo = cursor.getString(3),
                    cantidad = cursor.getInt(4),
                    valorUnitario = cursor.getDouble(5),
                    promedio = cursor.getDouble(6)
                )
            )
        }
        cursor.close()
        return lista
    }

    // Obtener solo los asadores creados desde la tabla de "asadores"
    fun obtenerAsadoresCreados(): List<Asador> {
        val lista = mutableListOf<Asador>()
        val db = readableDatabase

        // Consulta para obtener solo los asadores creados (de la tabla "asadores")
        val cursor = db.rawQuery("SELECT * FROM asadores WHERE codigo LIKE 'Ahumador_%'", null)

        while (cursor.moveToNext()) {
            lista.add(
                Asador(
                    codigo = cursor.getString(cursor.getColumnIndexOrThrow("codigo")),
                    nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                    valor = cursor.getDouble(cursor.getColumnIndexOrThrow("valor")),
                    existencia = cursor.getInt(cursor.getColumnIndexOrThrow("existencia"))
                )
            )
        }
        cursor.close()
        return lista
    }

    fun insertarArticulo(articulo: Articulo): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("codigo", articulo.codigo)
            put("nombre", articulo.nombre)
            put("valor", articulo.valor)
            put("existencia", articulo.existencia)
            put("tipoAsador", articulo.tipoAsador)
            put("unidadesNecesarias", articulo.unidadesNecesarias)
        }
        val resultado = db.insert("articulos", null, values)
        return resultado != -1L
    }

    // Obtener el valor unitario de un ahumador por tipo
    fun obtenerValorAhumador(tipoAsador: String): Double {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT valor FROM asadores WHERE tipoAsador = ?", arrayOf(tipoAsador))
        var valor = 0.0
        if (cursor.moveToFirst()) {
            valor = cursor.getDouble(cursor.getColumnIndexOrThrow("valor"))
        }
        cursor.close()
        return valor
    }

    // Funciones de perfiles
    fun obtenerPerfiles(): List<Perfil> {
        val lista = mutableListOf<Perfil>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM perfiles", null)
        while (cursor.moveToNext()) {
            lista.add(
                Perfil(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    clienteId = cursor.getInt(cursor.getColumnIndexOrThrow("clienteId")),
                    nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                    cantidadPequenos = cursor.getInt(cursor.getColumnIndexOrThrow("cantidadPequenos")),
                    cantidadMedianos = cursor.getInt(cursor.getColumnIndexOrThrow("cantidadMedianos")),
                    cantidadGrandes = cursor.getInt(cursor.getColumnIndexOrThrow("cantidadGrandes")),
                    valorTotal = cursor.getDouble(cursor.getColumnIndexOrThrow("valorTotal")),
                    fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha"))
                )
            )
        }
        cursor.close()
        return lista
    }

    // Obtener perfiles por cliente
    fun obtenerPerfilesPorCliente(clienteId: Int): List<Perfil> {
        val lista = mutableListOf<Perfil>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM perfiles WHERE clienteId = ?", arrayOf(clienteId.toString()))
        while (cursor.moveToNext()) {
            lista.add(
                Perfil(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    clienteId = cursor.getInt(cursor.getColumnIndexOrThrow("clienteId")),
                    nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                    cantidadPequenos = cursor.getInt(cursor.getColumnIndexOrThrow("cantidadPequenos")),
                    cantidadMedianos = cursor.getInt(cursor.getColumnIndexOrThrow("cantidadMedianos")),
                    cantidadGrandes = cursor.getInt(cursor.getColumnIndexOrThrow("cantidadGrandes")),
                    valorTotal = cursor.getDouble(cursor.getColumnIndexOrThrow("valorTotal")),
                    fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha"))
                )
            )
        }
        cursor.close()
        return lista
    }

    // Insertar un perfil con el total de los ahumadores y su valor
    fun insertarPerfil(
        clienteId: Int,
        nombre: String,
        cantidadPequenos: Int,
        cantidadMedianos: Int,
        cantidadGrandes: Int,
        valorTotal: Double,
        fecha: String
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("clienteId", clienteId)
            put("nombre", nombre)
            put("cantidadPequenos", cantidadPequenos)
            put("cantidadMedianos", cantidadMedianos)
            put("cantidadGrandes", cantidadGrandes)
            put("valorTotal", valorTotal)
            put("fecha", fecha)
        }
        val resultado = db.insert("perfiles", null, values)
        return resultado != -1L
    }

    // Insertar un cliente
    fun insertarCliente(nombre: String, telefono: String, correo: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nombre", nombre)
            put("telefono", telefono)
            put("correo", correo)
        }
        val resultado = db.insert("clientes", null, values)
        return resultado != -1L
    }

    // Obtener todos los clientes
    fun obtenerClientes(): List<Cliente> {
        val lista = mutableListOf<Cliente>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM clientes", null)
        while (cursor.moveToNext()) {
            lista.add(
                Cliente(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                    telefono = cursor.getString(cursor.getColumnIndexOrThrow("telefono")),
                    correo = cursor.getString(cursor.getColumnIndexOrThrow("correo"))
                )
            )
        }
        cursor.close()
        return lista
    }
}
