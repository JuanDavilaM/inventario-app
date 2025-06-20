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
    SQLiteOpenHelper(context, "InventarioDB", null, 13) { // Subir versión a 13

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
                promedio REAL,
                esAjuste INTEGER DEFAULT 0
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
                fecha TEXT,
                despachadoPequenos INTEGER DEFAULT 0,
                despachadoMedianos INTEGER DEFAULT 0,
                despachadoGrandes INTEGER DEFAULT 0,
                fechaComprometida TEXT,
                estado TEXT DEFAULT 'Activo'
            )"""
        )

        db.execSQL(
            """CREATE TABLE pagos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                clienteId INTEGER,
                pedidoId INTEGER,
                monto REAL,
                fecha TEXT,
                observaciones TEXT
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
        if (oldVersion < 11) {
            try {
                db.execSQL("ALTER TABLE movimientos ADD COLUMN esAjuste INTEGER DEFAULT 0")
            } catch (e: Exception) {}
        }
        if (oldVersion < 12) {
            try { db.execSQL("ALTER TABLE perfiles ADD COLUMN despachadoPequenos INTEGER DEFAULT 0") } catch (e: Exception) {}
            try { db.execSQL("ALTER TABLE perfiles ADD COLUMN despachadoMedianos INTEGER DEFAULT 0") } catch (e: Exception) {}
            try { db.execSQL("ALTER TABLE perfiles ADD COLUMN despachadoGrandes INTEGER DEFAULT 0") } catch (e: Exception) {}
            try { db.execSQL("ALTER TABLE perfiles ADD COLUMN fechaComprometida TEXT") } catch (e: Exception) {}
            try { db.execSQL("ALTER TABLE perfiles ADD COLUMN estado TEXT DEFAULT 'Activo'") } catch (e: Exception) {}
        }
        if (oldVersion < 13) {
            try { db.execSQL("CREATE TABLE IF NOT EXISTS pagos (id INTEGER PRIMARY KEY AUTOINCREMENT, clienteId INTEGER, pedidoId INTEGER, monto REAL, fecha TEXT, observaciones TEXT)") } catch (e: Exception) {}
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
                        fecha TEXT,
                        despachadoPequenos INTEGER DEFAULT 0,
                        despachadoMedianos INTEGER DEFAULT 0,
                        despachadoGrandes INTEGER DEFAULT 0,
                        fechaComprometida TEXT,
                        estado TEXT DEFAULT 'Activo'
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
            put("esAjuste", if (mov.esAjuste) 1 else 0)
        }
        val result = db.insert("movimientos", null, values)

        val signo = if (mov.tipo == "Entrada") 1 else -1
        if (!mov.esAjuste) {
            db.execSQL(
                "UPDATE articulos SET existencia = existencia + (${signo} * ${mov.cantidad}) WHERE codigo = ?",
                arrayOf(mov.codigo)
            )
        }
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
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo")),
                    fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha")),
                    codigo = cursor.getString(cursor.getColumnIndexOrThrow("codigo")),
                    cantidad = cursor.getInt(cursor.getColumnIndexOrThrow("cantidad")),
                    valorUnitario = cursor.getDouble(cursor.getColumnIndexOrThrow("valorUnitario")),
                    promedio = cursor.getDouble(cursor.getColumnIndexOrThrow("promedio")),
                    esAjuste = (cursor.getColumnIndex("esAjuste") != -1 && cursor.getInt(cursor.getColumnIndexOrThrow("esAjuste")) == 1)
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
                    fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha")),
                    despachadoPequenos = cursor.getInt(cursor.getColumnIndexOrThrow("despachadoPequenos")),
                    despachadoMedianos = cursor.getInt(cursor.getColumnIndexOrThrow("despachadoMedianos")),
                    despachadoGrandes = cursor.getInt(cursor.getColumnIndexOrThrow("despachadoGrandes")),
                    fechaComprometida = cursor.getString(cursor.getColumnIndexOrThrow("fechaComprometida")),
                    estado = cursor.getString(cursor.getColumnIndexOrThrow("estado"))
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
                    fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha")),
                    despachadoPequenos = cursor.getInt(cursor.getColumnIndexOrThrow("despachadoPequenos")),
                    despachadoMedianos = cursor.getInt(cursor.getColumnIndexOrThrow("despachadoMedianos")),
                    despachadoGrandes = cursor.getInt(cursor.getColumnIndexOrThrow("despachadoGrandes")),
                    fechaComprometida = cursor.getString(cursor.getColumnIndexOrThrow("fechaComprometida")),
                    estado = cursor.getString(cursor.getColumnIndexOrThrow("estado"))
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
        fecha: String,
        fechaComprometida: String?,
        despachadoPequenos: Int = 0,
        despachadoMedianos: Int = 0,
        despachadoGrandes: Int = 0,
        estado: String = "Activo"
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
            put("fechaComprometida", fechaComprometida)
            put("despachadoPequenos", despachadoPequenos)
            put("despachadoMedianos", despachadoMedianos)
            put("despachadoGrandes", despachadoGrandes)
            put("estado", estado)
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

    // Obtener el stock de un tipo de ahumador
    fun obtenerStockAhumador(tipoAsador: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT existencia FROM asadores WHERE tipoAsador = ?", arrayOf(tipoAsador))
        var stock = 0
        if (cursor.moveToFirst()) {
            stock = cursor.getInt(cursor.getColumnIndexOrThrow("existencia"))
        }
        cursor.close()
        return stock
    }

    // Actualizar el stock de un tipo de ahumador
    fun actualizarStockAhumador(tipoAsador: String, nuevaExistencia: Int): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("existencia", nuevaExistencia)
        }
        val filasAfectadas = db.update("asadores", values, "tipoAsador = ?", arrayOf(tipoAsador))
        return filasAfectadas > 0
    }

    // Despachar parcialmente un pedido
    // Despachar parcialmente un pedido
    fun despacharPedido(
        pedidoId: Int,
        nuevosDespachadoPequenos: Int,
        nuevosDespachadoMedianos: Int,
        nuevosDespachadoGrandes: Int
    ): Boolean {
        val db = writableDatabase
        // Obtener el pedido actual
        val cursor = db.rawQuery(
            "SELECT cantidadPequenos, cantidadMedianos, cantidadGrandes, despachadoPequenos, despachadoMedianos, despachadoGrandes FROM perfiles WHERE id = ?",
            arrayOf(pedidoId.toString())
        )
        if (!cursor.moveToFirst()) {
            cursor.close()
            return false
        }
        val totalPeq = cursor.getInt(cursor.getColumnIndexOrThrow("cantidadPequenos"))
        val totalMed = cursor.getInt(cursor.getColumnIndexOrThrow("cantidadMedianos"))
        val totalGra = cursor.getInt(cursor.getColumnIndexOrThrow("cantidadGrandes"))
        val anteriorDespPeq = cursor.getInt(cursor.getColumnIndexOrThrow("despachadoPequenos"))
        val anteriorDespMed = cursor.getInt(cursor.getColumnIndexOrThrow("despachadoMedianos"))
        val anteriorDespGra = cursor.getInt(cursor.getColumnIndexOrThrow("despachadoGrandes"))
        cursor.close()

        // Calcular cuántos se están despachando en este movimiento
        val aDespacharPeq = nuevosDespachadoPequenos - anteriorDespPeq
        val aDespacharMed = nuevosDespachadoMedianos - anteriorDespMed
        val aDespacharGra = nuevosDespachadoGrandes - anteriorDespGra

        // Verificar stock
        val stockPeq = obtenerStockAhumador("Pequeño")
        val stockMed = obtenerStockAhumador("Mediano")
        val stockGra = obtenerStockAhumador("Grande")
        if (aDespacharPeq > stockPeq || aDespacharMed > stockMed || aDespacharGra > stockGra) {
            return false // No hay stock suficiente
        }

        // Actualizar stock
        if (aDespacharPeq > 0) actualizarStockAhumador("Pequeño", stockPeq - aDespacharPeq)
        if (aDespacharMed > 0) actualizarStockAhumador("Mediano", stockMed - aDespacharMed)
        if (aDespacharGra > 0) actualizarStockAhumador("Grande", stockGra - aDespacharGra)

        // Verificar si el pedido está completado
        val completado = (nuevosDespachadoPequenos >= totalPeq) && (nuevosDespachadoMedianos >= totalMed) && (nuevosDespachadoGrandes >= totalGra)
        val values = ContentValues().apply {
            put("despachadoPequenos", nuevosDespachadoPequenos)
            put("despachadoMedianos", nuevosDespachadoMedianos)
            put("despachadoGrandes", nuevosDespachadoGrandes)
            if (completado) put("estado", "Completado")
        }
        db.update("perfiles", values, "id = ?", arrayOf(pedidoId.toString()))
        return true
    }

    fun actualizarEstadoPedido(pedidoId: Int, nuevoEstado: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("estado", nuevoEstado)
        }
        db.update("perfiles", values, "id = ?", arrayOf(pedidoId.toString()))
    }

    // Insertar un pago
    fun insertarPago(clienteId: Int, pedidoId: Int, monto: Double, fecha: String, observaciones: String?): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("clienteId", clienteId)
            put("pedidoId", pedidoId)
            put("monto", monto)
            put("fecha", fecha)
            put("observaciones", observaciones)
        }
        val resultado = db.insert("pagos", null, values)
        return resultado != -1L
    }

    // Obtener pagos por cliente
    fun obtenerPagosPorCliente(clienteId: Int): List<Map<String, Any?>> {
        val lista = mutableListOf<Map<String, Any?>>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM pagos WHERE clienteId = ?", arrayOf(clienteId.toString()))
        while (cursor.moveToNext()) {
            lista.add(
                mapOf(
                    "id" to cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    "clienteId" to cursor.getInt(cursor.getColumnIndexOrThrow("clienteId")),
                    "pedidoId" to cursor.getInt(cursor.getColumnIndexOrThrow("pedidoId")),
                    "monto" to cursor.getDouble(cursor.getColumnIndexOrThrow("monto")),
                    "fecha" to cursor.getString(cursor.getColumnIndexOrThrow("fecha")),
                    "observaciones" to cursor.getString(cursor.getColumnIndexOrThrow("observaciones"))
                )
            )
        }
        cursor.close()
        return lista
    }

    // Obtener pagos por pedido
    fun obtenerPagosPorPedido(pedidoId: Int): List<Map<String, Any?>> {
        val lista = mutableListOf<Map<String, Any?>>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM pagos WHERE pedidoId = ?", arrayOf(pedidoId.toString()))
        while (cursor.moveToNext()) {
            lista.add(
                mapOf(
                    "id" to cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    "clienteId" to cursor.getInt(cursor.getColumnIndexOrThrow("clienteId")),
                    "pedidoId" to cursor.getInt(cursor.getColumnIndexOrThrow("pedidoId")),
                    "monto" to cursor.getDouble(cursor.getColumnIndexOrThrow("monto")),
                    "fecha" to cursor.getString(cursor.getColumnIndexOrThrow("fecha")),
                    "observaciones" to cursor.getString(cursor.getColumnIndexOrThrow("observaciones"))
                )
            )
        }
        cursor.close()
        return lista
    }
}
