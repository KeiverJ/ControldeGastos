package com.example.controldegastos.models;

public class Transaccion {
    private int id;
    private double monto;
    private String descripcion;
    private String fecha;
    private int idCategoria;
    private String tipo; // "ingreso" o "gasto"

    // Para mostrar en la lista
    private String nombreCategoria;
    private String colorCategoria;

    public Transaccion() {
    }

    // Constructor completo
    public Transaccion(int id, double monto, String descripcion, String fecha,
                       int idCategoria, String tipo) {
        this.id = id;
        this.monto = monto;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.idCategoria = idCategoria;
        this.tipo = tipo;
    }

    // Constructor sin ID (para insertar nuevos)
    public Transaccion(double monto, String descripcion, String fecha,
                       int idCategoria, String tipo) {
        this.monto = monto;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.idCategoria = idCategoria;
        this.tipo = tipo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public String getColorCategoria() {
        return colorCategoria;
    }

    public void setColorCategoria(String colorCategoria) {
        this.colorCategoria = colorCategoria;
    }
}