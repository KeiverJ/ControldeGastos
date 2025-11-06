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
    private static final int DATABASE_VERSION = 1;

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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACCIONES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIAS);
        onCreate(db);
    }

    // Insertar categorías iniciales
    private void insertarCategoriasIniciales(SQLiteDatabase db) {
        // Categorías de Gastos
        insertarCategoriaInicial(db, "Alimentación", "gasto", "#FF6B6B", "🍔");
        insertarCategoriaInicial(db, "Transporte", "gasto", "#4ECDC4", "🚗");
        insertarCategoriaInicial(db, "Entretenimiento", "gasto", "#95E1D3", "🎮");
        insertarCategoriaInicial(db, "Salud", "gasto", "#F38181", "💊");
        insertarCategoriaInicial(db, "Educación", "gasto", "#AA96DA", "📚");
        insertarCategoriaInicial(db, "Servicios", "gasto", "#FCBAD3", "💡");
        insertarCategoriaInicial(db, "Otros Gastos", "gasto", "#A8D8EA", "📦");

        // Categorías de Ingresos
        insertarCategoriaInicial(db, "Salario", "ingreso", "#38B000", "💰");
        insertarCategoriaInicial(db, "Freelance", "ingreso", "#5FD068", "💼");
        insertarCategoriaInicial(db, "Inversiones", "ingreso", "#8BC34A", "📈");
        insertarCategoriaInicial(db, "Otros Ingresos", "ingreso", "#9CCC65", "💵");
    }

    private void insertarCategoriaInicial(SQLiteDatabase db, String nombre, String tipo, String color, String icono) {
        ContentValues values = new ContentValues();
        values.put(COL_CAT_NOMBRE, nombre);
        values.put(COL_CAT_TIPO, tipo);
        values.put(COL_CAT_COLOR, color);
        values.put(COL_CAT_ICONO, icono);
        db.insert(TABLE_CATEGORIAS, null, values);
    }

    // Metodos para categorias

    public long insertarCategoria(String nombre, String tipo, String color, String icono) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CAT_NOMBRE, nombre);
        values.put(COL_CAT_TIPO, tipo);
        values.put(COL_CAT_COLOR, color);
        values.put(COL_CAT_ICONO, icono);
        return db.insert(TABLE_CATEGORIAS, null, values);
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

    // Metodos para transacciones

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