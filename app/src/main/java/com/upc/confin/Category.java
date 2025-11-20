package com.upc.confin;

public class Category {
    private String id;
    private String nombre;
    private String tipoId; // Ahora es tipoId en lugar de tipo
    private String icono;
    private String color;

    // Constructor vacío para Firebase
    public Category() {
    }

    public Category(String id, String nombre, String tipoId, String icono, String color) {
        this.id = id;
        this.nombre = nombre;
        this.tipoId = tipoId;
        this.icono = icono;
        this.color = color;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipoId() { return tipoId; }
    public void setTipoId(String tipoId) { this.tipoId = tipoId; }

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