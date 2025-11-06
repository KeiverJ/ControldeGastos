package com.example.controldegastos.models;

public class Categoria {
    private int id;
    private String nombre;
    private String tipo; // "ingreso" o "gasto"
    private String color;
    private String icono;

    public Categoria() {
    }

    // Constructor completo
    public Categoria(int id, String nombre, String tipo, String color, String icono) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.color = color;
        this.icono = icono;
    }

    // Constructor sin ID (para insertar nuevos)
    public Categoria(String nombre, String tipo, String color, String icono) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.color = color;
        this.icono = icono;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcono() {
        return icono;
    }

    public void setIcono(String icono) {
        this.icono = icono;
    }

    @Override
    public String toString() {
        return nombre;
    }
}