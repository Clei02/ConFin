package com.upc.confin; // Reemplaza con tu paquete

/**
 * Modelo de datos que representa un único Gasto.
 */
public class Expense {
    private final String categoryName;
    private final String date;
    private final String amount;
    private final int iconResId; // ID del recurso drawable del icono

    public Expense(String categoryName, String date, String amount, int iconResId) {
        this.categoryName = categoryName;
        this.date = date;
        this.amount = amount;
        this.iconResId = iconResId;
    }

    // Getters para acceder a los datos
    public String getCategoryName() { return categoryName; }
    public String getDate() { return date; }
    public String getAmount() { return amount; }
    public int getIconResId() { return iconResId; }
}