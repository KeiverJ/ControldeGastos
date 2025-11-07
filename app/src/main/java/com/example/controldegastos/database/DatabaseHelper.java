package com.example.controldegastos.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Nombre y versión de la base de datos
    private static final String DATABASE_NAME = "control_gastos.db";
    private static final int DATABASE_VERSION = 2; // Incrementamos para actualizar

    // Tabla Categorías
    private static final String TABLE_CATEGORIAS = "categorias";
    private static final String COL_CAT_ID = "id";
    private static final String COL_CAT_NOMBRE = "nombre";
    private static final String COL_CAT_TIPO = "tipo";
    private static final String COL_CAT_COLOR = "color";
    private static final String COL_CAT_ICONO = "icono";

    // Tabla Transacciones
    private static final String TABLE_TRANSACCIONES = "transacciones";
    private static final String COL_TRANS_ID = "id";
    private static final String COL_TRANS_MONTO = "monto";
    private static final String COL_TRANS_DESCRIPCION = "descripcion";
    private static final String COL_TRANS_FECHA = "fecha";
    private static final String COL_TRANS_TIPO = "tipo";
    private static final String COL_TRANS_ID_CATEGORIA = "id_categoria";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear tabla Categorías
        String createCategorias = "CREATE TABLE " + TABLE_CATEGORIAS + " (" +
                COL_CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CAT_NOMBRE + " TEXT NOT NULL, " +
                COL_CAT_TIPO + " TEXT NOT NULL, " +
                COL_CAT_COLOR + " TEXT, " +
                COL_CAT_ICONO + " TEXT)";
        db.execSQL(createCategorias);

        // Crear tabla Transacciones
        String createTransacciones = "CREATE TABLE " + TABLE_TRANSACCIONES + " (" +
                COL_TRANS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TRANS_MONTO + " REAL NOT NULL, " +
                COL_TRANS_DESCRIPCION + " TEXT, " +
                COL_TRANS_FECHA + " INTEGER NOT NULL, " +
                COL_TRANS_TIPO + " TEXT NOT NULL, " +
                COL_TRANS_ID_CATEGORIA + " INTEGER, " +
                "FOREIGN KEY(" + COL_TRANS_ID_CATEGORIA + ") REFERENCES " +
                TABLE_CATEGORIAS + "(" + COL_CAT_ID + "))";
        db.execSQL(createTransacciones);

        // Insertar categorías predeterminadas
        insertarCategoriasIniciales(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Agregar nuevas categorías sin borrar las existentes
            insertarCategoriasAdicionalesV2(db);
        }
    }

    // Insertar categorías iniciales AMPLIADAS
    private void insertarCategoriasIniciales(SQLiteDatabase db) {
        // ============ CATEGORÍAS DE GASTOS ============
        // Básicas
        insertarCategoriaInicial(db, "Alimentación", "gasto", "#FF6B6B", "🍔");
        insertarCategoriaInicial(db, "Transporte", "gasto", "#4ECDC4", "🚗");
        insertarCategoriaInicial(db, "Entretenimiento", "gasto", "#95E1D3", "🎮");
        insertarCategoriaInicial(db, "Salud", "gasto", "#F38181", "💊");
        insertarCategoriaInicial(db, "Educación", "gasto", "#AA96DA", "📚");
        insertarCategoriaInicial(db, "Servicios", "gasto", "#FCBAD3", "💡");

        // Adicionales
        insertarCategoriaInicial(db, "Vivienda", "gasto", "#FFB347", "🏠");
        insertarCategoriaInicial(db, "Ropa y Calzado", "gasto", "#DDA0DD", "👕");
        insertarCategoriaInicial(db, "Mascotas", "gasto", "#98D8C8", "🐾");
        insertarCategoriaInicial(db, "Regalos", "gasto", "#F7B7A3", "🎁");
        insertarCategoriaInicial(db, "Tecnología", "gasto", "#7EC8E3", "💻");
        insertarCategoriaInicial(db, "Gimnasio", "gasto", "#C1E1C1", "💪");
        insertarCategoriaInicial(db, "Seguros", "gasto", "#B4A7D6", "🛡️");
        insertarCategoriaInicial(db, "Restaurantes", "gasto", "#FFD700", "🍽️");
        insertarCategoriaInicial(db, "Cafetería", "gasto", "#D2691E", "☕");
        insertarCategoriaInicial(db, "Compras", "gasto", "#FF69B4", "🛍️");
        insertarCategoriaInicial(db, "Belleza", "gasto", "#FFC0CB", "💄");
        insertarCategoriaInicial(db, "Viajes", "gasto", "#87CEEB", "✈️");
        insertarCategoriaInicial(db, "Impuestos", "gasto", "#DC143C", "📋");
        insertarCategoriaInicial(db, "Otros Gastos", "gasto", "#A8D8EA", "📦");

        // ============ CATEGORÍAS DE INGRESOS ============
        // Básicas
        insertarCategoriaInicial(db, "Salario", "ingreso", "#38B000", "💰");
        insertarCategoriaInicial(db, "Freelance", "ingreso", "#5FD068", "💼");
        insertarCategoriaInicial(db, "Inversiones", "ingreso", "#8BC34A", "📈");

        // Adicionales
        insertarCategoriaInicial(db, "Bonos", "ingreso", "#7CB342", "🎯");
        insertarCategoriaInicial(db, "Ventas", "ingreso", "#9CCC65", "🏪");
        insertarCategoriaInicial(db, "Alquiler", "ingreso", "#AED581", "🏘️");
        insertarCategoriaInicial(db, "Premios", "ingreso", "#C5E1A5", "🏆");
        insertarCategoriaInicial(db, "Regalos Recibidos", "ingreso", "#DCEDC8", "🎁");
        insertarCategoriaInicial(db, "Reembolsos", "ingreso", "#689F38", "💳");
        insertarCategoriaInicial(db, "Otros Ingresos", "ingreso", "#9CCC65", "💵");
    }

    // Método para agregar categorías adicionales en actualizaciones
    private void insertarCategoriasAdicionalesV2(SQLiteDatabase db) {
        // Verificar si ya existen antes de insertar
        insertarCategoriaInicial(db, "Vivienda", "gasto", "#FFB347", "🏠");
        insertarCategoriaInicial(db, "Ropa y Calzado", "gasto", "#DDA0DD", "👕");
        insertarCategoriaInicial(db, "Mascotas", "gasto", "#98D8C8", "🐾");
        insertarCategoriaInicial(db, "Regalos", "gasto", "#F7B7A3", "🎁");
        insertarCategoriaInicial(db, "Tecnología", "gasto", "#7EC8E3", "💻");
        insertarCategoriaInicial(db, "Gimnasio", "gasto", "#C1E1C1", "💪");
        insertarCategoriaInicial(db, "Seguros", "gasto", "#B4A7D6", "🛡️");
        insertarCategoriaInicial(db, "Restaurantes", "gasto", "#FFD700", "🍽️");
        insertarCategoriaInicial(db, "Cafetería", "gasto", "#D2691E", "☕");
        insertarCategoriaInicial(db, "Compras", "gasto", "#FF69B4", "🛍️");
        insertarCategoriaInicial(db, "Belleza", "gasto", "#FFC0CB", "💄");
        insertarCategoriaInicial(db, "Viajes", "gasto", "#87CEEB", "✈️");
        insertarCategoriaInicial(db, "Impuestos", "gasto", "#DC143C", "📋");

        insertarCategoriaInicial(db, "Bonos", "ingreso", "#7CB342", "🎯");
        insertarCategoriaInicial(db, "Ventas", "ingreso", "#9CCC65", "🏪");
        insertarCategoriaInicial(db, "Alquiler", "ingreso", "#AED581", "🏘️");
        insertarCategoriaInicial(db, "Premios", "ingreso", "#C5E1A5", "🏆");
        insertarCategoriaInicial(db, "Regalos Recibidos", "ingreso", "#DCEDC8", "🎁");
        insertarCategoriaInicial(db, "Reembolsos", "ingreso", "#689F38", "💳");
    }

    private void insertarCategoriaInicial(SQLiteDatabase db, String nombre, String tipo, String color, String icono) {
        ContentValues values = new ContentValues();
        values.put(COL_CAT_NOMBRE, nombre);
        values.put(COL_CAT_TIPO, tipo);
        values.put(COL_CAT_COLOR, color);
        values.put(COL_CAT_ICONO, icono);
        db.insert(TABLE_CATEGORIAS, null, values);
    }

    // ==================== MÉTODOS PARA CATEGORÍAS ====================

    public long insertarCategoria(String nombre, String tipo, String color, String icono) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CAT_NOMBRE, nombre);
        values.put(COL_CAT_TIPO, tipo);
        values.put(COL_CAT_COLOR, color);
        values.put(COL_CAT_ICONO, icono);
        return db.insert(TABLE_CATEGORIAS, null, values);
    }

    public int actualizarCategoria(int id, String nombre, String color, String icono) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CAT_NOMBRE, nombre);
        values.put(COL_CAT_COLOR, color);
        values.put(COL_CAT_ICONO, icono);
        return db.update(TABLE_CATEGORIAS, values, COL_CAT_ID + "=?", new String[]{String.valueOf(id)});
    }

    public void eliminarCategoria(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CATEGORIAS, COL_CAT_ID + "=?", new String[]{String.valueOf(id)});
    }

    public List<String[]> obtenerCategoriasPorTipo(String tipo) {
        List<String[]> categorias = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CATEGORIAS,
                new String[]{COL_CAT_ID, COL_CAT_NOMBRE, COL_CAT_COLOR, COL_CAT_ICONO},
                COL_CAT_TIPO + "=?",
                new String[]{tipo},
                null, null, COL_CAT_NOMBRE + " ASC");

        if (cursor.moveToFirst()) {
            do {
                String[] categoria = new String[4];
                categoria[0] = cursor.getString(0); // id
                categoria[1] = cursor.getString(1); // nombre
                categoria[2] = cursor.getString(2); // color
                categoria[3] = cursor.getString(3); // icono
                categorias.add(categoria);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categorias;
    }

    public Cursor obtenerTodasCategorias() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_CATEGORIAS +
                " ORDER BY " + COL_CAT_TIPO + " ASC, " + COL_CAT_NOMBRE + " ASC", null);
    }

    public String[] obtenerCategoriaPorId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CATEGORIAS,
                new String[]{COL_CAT_ID, COL_CAT_NOMBRE, COL_CAT_COLOR, COL_CAT_ICONO},
                COL_CAT_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String[] categoria = new String[4];
            categoria[0] = cursor.getString(0);
            categoria[1] = cursor.getString(1);
            categoria[2] = cursor.getString(2);
            categoria[3] = cursor.getString(3);
            cursor.close();
            return categoria;
        }
        return null;
    }

    // ==================== MÉTODOS PARA TRANSACCIONES ====================

    public long insertarTransaccion(double monto, String descripcion, long fecha, String tipo, int idCategoria) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TRANS_MONTO, monto);
        values.put(COL_TRANS_DESCRIPCION, descripcion);
        values.put(COL_TRANS_FECHA, fecha);
        values.put(COL_TRANS_TIPO, tipo);
        values.put(COL_TRANS_ID_CATEGORIA, idCategoria);
        return db.insert(TABLE_TRANSACCIONES, null, values);
    }

    public int actualizarTransaccion(int id, double monto, String descripcion, long fecha, String tipo, int idCategoria) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TRANS_MONTO, monto);
        values.put(COL_TRANS_DESCRIPCION, descripcion);
        values.put(COL_TRANS_FECHA, fecha);
        values.put(COL_TRANS_TIPO, tipo);
        values.put(COL_TRANS_ID_CATEGORIA, idCategoria);
        return db.update(TABLE_TRANSACCIONES, values, COL_TRANS_ID + "=?", new String[]{String.valueOf(id)});
    }

    public void eliminarTransaccion(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TRANSACCIONES, COL_TRANS_ID + "=?", new String[]{String.valueOf(id)});
    }

    public Cursor obtenerTodasTransacciones() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_TRANSACCIONES +
                " ORDER BY " + COL_TRANS_FECHA + " DESC", null);
    }

    public Cursor obtenerTransaccionesPorCategoria(int idCategoria) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_TRANSACCIONES +
                " WHERE " + COL_TRANS_ID_CATEGORIA + "=? ORDER BY " +
                COL_TRANS_FECHA + " DESC", new String[]{String.valueOf(idCategoria)});
    }

    public Cursor obtenerTransaccionesPorFecha(long fechaInicio, long fechaFin) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_TRANSACCIONES +
                        " WHERE " + COL_TRANS_FECHA + " BETWEEN ? AND ? ORDER BY " +
                        COL_TRANS_FECHA + " DESC",
                new String[]{String.valueOf(fechaInicio), String.valueOf(fechaFin)});
    }

    public Cursor buscarTransacciones(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_TRANSACCIONES +
                        " WHERE " + COL_TRANS_DESCRIPCION + " LIKE ? ORDER BY " +
                        COL_TRANS_FECHA + " DESC",
                new String[]{"%" + query + "%"});
    }

    public double calcularBalance() {
        SQLiteDatabase db = this.getReadableDatabase();
        double balance = 0;

        Cursor cursor = db.rawQuery("SELECT " + COL_TRANS_TIPO + ", SUM(" + COL_TRANS_MONTO +
                ") as total FROM " + TABLE_TRANSACCIONES + " GROUP BY " + COL_TRANS_TIPO, null);

        if (cursor.moveToFirst()) {
            do {
                String tipo = cursor.getString(0);
                double total = cursor.getDouble(1);
                if (tipo.equals("ingreso")) {
                    balance += total;
                } else {
                    balance -= total;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return balance;
    }

    public double calcularTotalPorTipo(String tipo) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COL_TRANS_MONTO + ") FROM " +
                TABLE_TRANSACCIONES + " WHERE " + COL_TRANS_TIPO + "=?", new String[]{tipo});

        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }
}