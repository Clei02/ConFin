package com.upc.confin; // Reemplaza con tu package name

public class Transaction {
    private final int icon;
    private final String name;
    private final String detail;
    private final double amount;

    public Transaction(int icon, String name, String detail, double amount) {
        this.icon = icon;
        this.name = name;
        this.detail = detail;
        this.amount = amount;
    }

    public int getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public String getDetail() {
        return detail;
    }

    public double getAmount() {
        return amount;
    }
}