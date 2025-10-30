package com.upc.confin;

public class Category {
    private String id;
    private String nombre;
    private String tipo; // "INGRESO" o "GASTO"
    private String icono; // Nombre del drawable (ej: "ic_restaurant")
    private String color; // Hex color (ej: "#FF5722")

    // Constructor vacío para Firebase
    public Category() {
    }

    // Constructor completo
    public Category(String id, String nombre, String tipo, String icono, String color) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.icono = icono;
        this.color = color;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getIcono() { return icono; }
    public void setIcono(String icono) { this.icono = icono; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    // Método auxiliar para obtener el ID del recurso drawable
    public int getIconoResId(android.content.Context context) {
        return context.getResources().getIdentifier(
                icono,
                "drawable",
                context.getPackageName()
        );
    }
}