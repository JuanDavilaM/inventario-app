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
        estado TEXT DEFAULT 'Activo',
        razonAnulacion TEXT
    )"""
)

if (oldVersion < 13) {
    try { db.execSQL("ALTER TABLE perfiles ADD COLUMN razonAnulacion TEXT") } catch (e: Exception) {}
}

fun actualizarEstadoPedido(pedidoId: Int, nuevoEstado: String, razonAnulacion: String? = null) {
    val db = writableDatabase
    val values = ContentValues().apply {
        put("estado", nuevoEstado)
        if (razonAnulacion != null) put("razonAnulacion", razonAnulacion)
    }
    db.update("perfiles", values, "id = ?", arrayOf(pedidoId.toString()))
} 