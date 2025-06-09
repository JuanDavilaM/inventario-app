package com.example.inventario

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.inventario.screens.Articulo
import com.example.inventario.screens.Movimiento

class InventarioDBHelper(context: Context) :
    SQLiteOpenHelper(context, "InventarioDB", null, 4) { // Versión 4

    override fun onCreate(db: SQLiteDatabase) {
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
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (!columnaExiste(db, "articulos", "tipoAsador")) {
            db.execSQL("ALTER TABLE articulos ADD COLUMN tipoAsador TEXT")
        }
        if (!columnaExiste(db, "articulos", "unidadesNecesarias")) {
            db.execSQL("ALTER TABLE articulos ADD COLUMN unidadesNecesarias INTEGER DEFAULT 0")
        }
    }

    private fun columnaExiste(db: SQLiteDatabase, tabla: String, columna: String): Boolean {
        val cursor = db.rawQuery("PRAGMA table_info($tabla)", null)
        var existe = false
        while (cursor.moveToNext()) {
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            if (nombre == columna) {
                existe = true
                break
            }
        }
        cursor.close()
        return existe
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

    fun obtenerArticuloPorCodigo(codigo: String): Articulo? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM articulos WHERE codigo = ?", arrayOf(codigo))
        var articulo: Articulo? = null
        if (cursor.moveToFirst()) {
            articulo = Articulo(
                codigo = cursor.getString(cursor.getColumnIndexOrThrow("codigo")),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                valor = cursor.getDouble(cursor.getColumnIndexOrThrow("valor")),
                existencia = cursor.getInt(cursor.getColumnIndexOrThrow("existencia")),
                tipoAsador = cursor.getString(cursor.getColumnIndexOrThrow("tipoAsador")) ?: "",
                unidadesNecesarias = cursor.getInt(cursor.getColumnIndexOrThrow("unidadesNecesarias"))
            )
        }
        cursor.close()
        return articulo
    }

    fun eliminarArticuloPorCodigo(codigo: String): Boolean {
        val db = writableDatabase
        val filasAfectadas = db.delete("articulos", "codigo = ?", arrayOf(codigo))
        return filasAfectadas > 0
    }

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
}
