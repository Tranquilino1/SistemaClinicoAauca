package com.clinica.aauca.model;

public class Medicamento {
    private int id;
    private String nombre;
    private int stock;
    private double precio;
    private int creadorId;

    public Medicamento(int id, String nombre, int stock, double precio, int creadorId) {
        this.id = id;
        this.nombre = nombre;
        this.stock = stock;
        this.precio = precio;
        this.creadorId = creadorId;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public int getStock() { return stock; }
    public double getPrecio() { return precio; }
    public int getCreadorId() { return creadorId; }

    /** Estado visible en la tabla */
    public String getEstadoStock() {
        if (stock == 0) return "❌ Agotado";
        if (stock <= 5) return "⚠️ Bajo";
        return "✅ OK";
    }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setStock(int stock) { this.stock = stock; }
    public void setPrecio(double precio) { this.precio = precio; }
}
