package com.upc.confin;

public class Transaction {
    private String id;
    private String tipoId; // Ahora es tipoId en lugar de tipo
    private String descripcion;
    private double monto;
    private String categoriaId;
    private long fecha;
    private long timestamp; // Para ordenar

    // Constructor vacío para Firebase
    public Transaction() {
    }

    public Transaction(String id, String tipoId, String descripcion, double monto,
                       String categoriaId, long fecha) {
        this.id = id;
        this.tipoId = tipoId;
        this.descripcion = descripcion;
        this.monto = monto;
        this.categoriaId = categoriaId;
        this.fecha = fecha;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTipoId() { return tipoId; }
    public void setTipoId(String tipoId) { this.tipoId = tipoId; }

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