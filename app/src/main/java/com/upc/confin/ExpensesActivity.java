package com.upc.confin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpensesActivity extends AppCompatActivity {

    private RecyclerView rvExpenses;
    private BottomNavigationView bottomNavigation;
    private ExpenseAdapter adapter;
    private List<ExpenseDisplay> expensesList;
    private DatabaseHelper dbHelper;
    private Map<String, Category> categoryMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses);

        // Inicializar vistas
        rvExpenses = findViewById(R.id.rv_expenses);
        bottomNavigation = findViewById(R.id.bottom_navigation_view);

        // Inicializar DatabaseHelper
        dbHelper = DatabaseHelper.getInstance();
        categoryMap = new HashMap<>();
        expensesList = new ArrayList<>();

        // Configurar RecyclerView
        setupRecyclerView();

        // Configurar navegación
        setupNavigation();

        // Cargar datos
        loadCategories();
    }

    private void setupRecyclerView() {
        rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExpenseAdapter(expensesList);
        rvExpenses.setAdapter(adapter);
    }

    private void setupNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_expenses);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_summary) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_expenses) {
                return true;
            } else if (itemId == R.id.nav_categories) {
                startActivity(new Intent(this, CategoriasActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadCategories() {
        dbHelper.loadCategories(new DatabaseHelper.OnCategoriesLoadedListener() {
            @Override
            public void onCategoriesLoaded(List<Category> categories) {
                // Guardar categorías en mapa
                for (Category category : categories) {
                    categoryMap.put(category.getId(), category);
                }
                // Cargar gastos
                loadExpenses();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ExpensesActivity.this,
                        "Error cargando categorías: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadExpenses() {
        // Cargar solo transacciones de tipo GASTO
        dbHelper.loadTransactionsByType("GASTO", new DatabaseHelper.OnTransactionsLoadedListener() {
            @Override
            public void onTransactionsLoaded(List<Transaction> transactions) {
                expensesList.clear();

                for (Transaction transaction : transactions) {
                    Category category = categoryMap.get(transaction.getCategoriaId());

                    if (category != null) {
                        int iconResId = category.getIconoResId(ExpensesActivity.this);
                        String dateStr = formatDate(transaction.getFecha());

                        expensesList.add(new ExpenseDisplay(
                                category.getNombre(),
                                dateStr,
                                String.format(Locale.getDefault(), "-$%.2f", transaction.getMonto()),
                                iconResId
                        ));
                    }
                }

                adapter.notifyDataSetChanged();

                // Mostrar mensaje si no hay gastos
                if (expensesList.isEmpty()) {
                    Toast.makeText(ExpensesActivity.this,
                            "No hay gastos registrados",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ExpensesActivity.this,
                        "Error cargando gastos: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("es", "CO"));
        return sdf.format(new Date(timestamp));
    }

    // Clase interna para mostrar gastos
    public static class ExpenseDisplay {
        private final String categoryName;
        private final String date;
        private final String amount;
        private final int iconResId;

        public ExpenseDisplay(String categoryName, String date, String amount, int iconResId) {
            this.categoryName = categoryName;
            this.date = date;
            this.amount = amount;
            this.iconResId = iconResId;
        }

        public String getCategoryName() { return categoryName; }
        public String getDate() { return date; }
        public String getAmount() { return amount; }
        public int getIconResId() { return iconResId; }
    }
}