package com.upc.confin;

public class Tipo {
    private String id;
    private String nombre; // "GASTO" o "INGRESO"

    // Constructor vacío para Firebase
    public Tipo() {
    }

    public Tipo(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}