package com.upc.confin;

public class Transaction {
    private String id;
    private String tipo; // "INGRESO" o "GASTO"
    private String descripcion;
    private double monto;
    private String categoriaId;
    private long fecha; // timestamp en milisegundos
    private long timestamp; // Para ordenar

    // Constructor vacío requerido por Firebase
    public Transaction() {
    }

    // Constructor completo
    public Transaction(String id, String tipo, String descripcion, double monto,
                       String categoriaId, long fecha) {
        this.id = id;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.monto = monto;
        this.categoriaId = categoriaId;
        this.fecha = fecha;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public String getCategoriaId() { return categoriaId; }
    public void setCategoriaId(String categoriaId) { this.categoriaId = categoriaId; }

    public long getFecha() { return fecha; }
    public void setFecha(long fecha) { this.fecha = fecha; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}