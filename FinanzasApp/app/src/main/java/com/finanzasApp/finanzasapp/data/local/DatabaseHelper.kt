package com.finanzasApp.finanzasapp.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.finanzasApp.finanzasapp.data.model.*
import java.math.BigDecimal
import java.time.LocalDate

/** Base privada del dispositivo. Los montos se almacenan en centavos enteros, nunca REAL. */
class DatabaseHelper(context: Context, name: String = "finanzas.db") :
    SQLiteOpenHelper(context.applicationContext, name, null, 1) {

    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE categorias (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            nombre TEXT NOT NULL,
            tipo TEXT NOT NULL CHECK(tipo IN ('INGRESO','GASTO'))
        )""")
        db.execSQL("""CREATE TABLE movimientos (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            tipo TEXT NOT NULL CHECK(tipo IN ('INGRESO','GASTO')),
            descripcion TEXT NOT NULL CHECK(length(trim(descripcion)) BETWEEN 1 AND 200),
            monto_centavos INTEGER NOT NULL CHECK(monto_centavos > 0),
            fecha TEXT NOT NULL,
            categoria_id INTEGER NOT NULL REFERENCES categorias(id)
        )""")
        db.execSQL("CREATE INDEX movimientos_fecha ON movimientos(fecha DESC, id DESC)")
        listOf(
            "Salario" to "INGRESO", "Otros ingresos" to "INGRESO",
            "Alimentación" to "GASTO", "Transporte" to "GASTO",
            "Vivienda" to "GASTO", "Salud" to "GASTO", "Educación" to "GASTO",
            "Entretenimiento" to "GASTO", "Otros gastos" to "GASTO"
        ).forEach { (nombre, tipo) ->
            db.insertOrThrow("categorias", null, ContentValues().apply {
                put("nombre", nombre)
                put("tipo", tipo)
            })
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Cada versión futura deberá definir una migración que conserve los movimientos.
        error("Falta la migración de SQLite de $oldVersion a $newVersion")
    }

    fun obtenerCategorias(): List<Categoria> = readableDatabase.query(
        "categorias", arrayOf("id", "nombre", "tipo"), null, null, null, null, "id ASC"
    ).use { cursor ->
        buildList {
            while (cursor.moveToNext()) add(Categoria(
                cursor.getLong(0), cursor.getString(1), TipoMovimiento.valueOf(cursor.getString(2))
            ))
        }
    }

    fun obtenerMovimientos(
        tipo: TipoMovimiento? = null, categoriaId: Long? = null,
        fechaInicio: LocalDate? = null, fechaFin: LocalDate? = null,
        id: Long? = null
    ): List<Movimiento> {
        require(fechaInicio == null || fechaFin == null || fechaInicio <= fechaFin) {
            "La fecha inicial no puede ser posterior a la final."
        }
        val condiciones = mutableListOf<String>()
        val args = mutableListOf<String>()
        fun filtro(condicion: String, valor: Any?) {
            if (valor != null) { condiciones.add(condicion); args.add(valor.toString()) }
        }
        filtro("m.tipo = ?", tipo?.name)
        filtro("m.categoria_id = ?", categoriaId)
        filtro("m.fecha >= ?", fechaInicio)
        filtro("m.fecha <= ?", fechaFin)
        filtro("m.id = ?", id)
        val where = if (condiciones.isEmpty()) "" else " WHERE " + condiciones.joinToString(" AND ")
        return readableDatabase.rawQuery(
            """SELECT m.id, m.tipo, m.descripcion, m.monto_centavos, m.fecha,
                c.id, c.nombre FROM movimientos m JOIN categorias c ON c.id = m.categoria_id
                $where ORDER BY m.fecha DESC, m.id DESC""", args.toTypedArray()
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(Movimiento(
                    cursor.getLong(0), TipoMovimiento.valueOf(cursor.getString(1)),
                    cursor.getString(2), BigDecimal.valueOf(cursor.getLong(3), 2),
                    cursor.getString(4), cursor.getLong(5), cursor.getString(6)
                ))
            }
        }
    }

    fun crearMovimiento(request: MovimientoRequest): Movimiento {
        require(request.descripcion.trim().length in 1..200) { "La descripción debe tener entre 1 y 200 caracteres." }
        require(request.monto > BigDecimal.ZERO && request.monto < BigDecimal("1000000000000")) {
            "El monto debe ser positivo y tener como máximo 12 cifras enteras."
        }
        val centavos = try { request.monto.movePointRight(2).longValueExact() }
        catch (_: ArithmeticException) { throw IllegalArgumentException("El monto admite como máximo dos decimales.") }
        val fecha = try { LocalDate.parse(request.fecha) }
        catch (_: java.time.format.DateTimeParseException) { throw IllegalArgumentException("La fecha no es válida.") }
        val categoria = obtenerCategorias().find { it.id == request.categoriaId }
        require(categoria != null) { "La categoría no existe." }
        require(categoria.tipo == request.tipo) { "La categoría no corresponde al tipo de movimiento." }
        val id = writableDatabase.insertOrThrow("movimientos", null, ContentValues().apply {
            put("tipo", request.tipo.name)
            put("descripcion", request.descripcion.trim())
            put("monto_centavos", centavos)
            put("fecha", fecha.toString())
            put("categoria_id", request.categoriaId)
        })
        return Movimiento(id, request.tipo, request.descripcion.trim(), BigDecimal.valueOf(centavos, 2),
            fecha.toString(), categoria.id, categoria.nombre)
    }

    fun eliminarMovimiento(id: Long) {
        require(writableDatabase.delete("movimientos", "id = ?", arrayOf(id.toString())) > 0) {
            "El movimiento no existe (quizá ya fue eliminado)."
        }
    }

    companion object {
        @Volatile private var instance: DatabaseHelper? = null
        fun getInstance(context: Context): DatabaseHelper = instance ?: synchronized(this) {
            instance ?: DatabaseHelper(context).also { instance = it }
        }
    }
}
