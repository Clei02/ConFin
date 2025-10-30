package com.upc.confin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerViewTransactions;
    private TransactionAdapter transactionAdapter;
    private List<TransactionDisplay> transactionDisplayList;

    private TextView textSaldoAmount;
    private TextView textIngresosAmount;
    private TextView textGastosAmount;

    private DatabaseHelper dbHelper;
    private Map<String, Category> categoryMap; // Para almacenar categorías

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Inicializar vistas
        initViews();

        // Inicializar DatabaseHelper
        dbHelper = DatabaseHelper.getInstance();
        categoryMap = new HashMap<>();

        // Configurar RecyclerView
        setupRecyclerView();

        // Cargar categorías primero, luego transacciones
        loadCategories();

        // Configurar FAB (Floating Action Button)
        com.google.android.material.floatingactionbutton.FloatingActionButton fab =
                findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AgregarTransaccionActivity.class);
            startActivity(intent);
        });

        // Configurar Bottom Navigation
        setupBottomNavigation();
    }

    private void initViews() {
        recyclerViewTransactions = findViewById(R.id.recycler_view_transactions);
        textSaldoAmount = findViewById(R.id.text_saldo_amount);
        textIngresosAmount = findViewById(R.id.text_ingresos_amount);
        textGastosAmount = findViewById(R.id.text_gastos_amount);
    }

    private void setupRecyclerView() {
        transactionDisplayList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(this, transactionDisplayList);
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTransactions.setAdapter(transactionAdapter);
    }

    private void loadCategories() {
        dbHelper.loadCategories(new DatabaseHelper.OnCategoriesLoadedListener() {
            @Override
            public void onCategoriesLoaded(List<Category> categories) {
                // Guardar categorías en un mapa para acceso rápido
                for (Category category : categories) {
                    categoryMap.put(category.getId(), category);
                }

                // Ahora cargar transacciones
                loadTransactions();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(HomeActivity.this,
                        "Error cargando categorías: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTransactions() {
        dbHelper.loadTransactions(new DatabaseHelper.OnTransactionsLoadedListener() {
            @Override
            public void onTransactionsLoaded(List<Transaction> transactions) {
                // Limpiar lista actual
                transactionDisplayList.clear();

                double totalIngresos = 0;
                double totalGastos = 0;

                // Convertir transacciones a formato de display
                for (Transaction transaction : transactions) {
                    Category category = categoryMap.get(transaction.getCategoriaId());

                    if (category != null) {
                        // Obtener icono de la categoría
                        int iconResId = category.getIconoResId(HomeActivity.this);

                        // Formatear fecha
                        String dateStr = formatDate(transaction.getFecha());

                        // Crear detalle (categoría + fecha)
                        String detail = category.getNombre() + " • " + dateStr;

                        // Agregar a lista de display (solo las 5 más recientes)
                        if (transactionDisplayList.size() < 5) {
                            transactionDisplayList.add(new TransactionDisplay(
                                    iconResId,
                                    transaction.getDescripcion(),
                                    detail,
                                    transaction.getMonto(),
                                    transaction.getTipo()
                            ));
                        }

                        // Calcular totales
                        if (transaction.getTipo().equals("INGRESO")) {
                            totalIngresos += transaction.getMonto();
                        } else {
                            totalGastos += transaction.getMonto();
                        }
                    }
                }

                // Actualizar UI
                updateBalances(totalIngresos, totalGastos);
                transactionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(HomeActivity.this,
                        "Error cargando transacciones: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBalances(double ingresos, double gastos) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

        double saldo = ingresos - gastos;

        textSaldoAmount.setText(currencyFormat.format(saldo));
        textIngresosAmount.setText("+" + currencyFormat.format(ingresos));
        textGastosAmount.setText("-" + currencyFormat.format(gastos));
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", new Locale("es", "CO"));
        return sdf.format(new Date(timestamp));
    }

    private void setupBottomNavigation() {
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                findViewById(R.id.bottom_navigation_view);

        bottomNav.setSelectedItemId(R.id.nav_summary);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_summary) {
                // Ya estamos aquí
                return true;
            } else if (itemId == R.id.nav_expenses) {
                startActivity(new Intent(this, ExpensesActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_categories) {
                startActivity(new Intent(this, CategoriasActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    // Clase interna para mostrar transacciones en el RecyclerView
    public static class TransactionDisplay {
        private final int icon;
        private final String name;
        private final String detail;
        private final double amount;
        private final String tipo;

        public TransactionDisplay(int icon, String name, String detail, double amount, String tipo) {
            this.icon = icon;
            this.name = name;
            this.detail = detail;
            this.amount = amount;
            this.tipo = tipo;
        }

        public int getIcon() { return icon; }
        public String getName() { return name; }
        public String getDetail() { return detail; }
        public double getAmount() { return amount; }
        public String getTipo() { return tipo; }
    }
}