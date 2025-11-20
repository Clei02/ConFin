package com.upc.confin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    private TextView tvMonthTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses);

        // Verificar sesión
        SharedPreferences prefs = getSharedPreferences("ConFinPrefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", null);

        if (userId == null) {
            Intent intent = new Intent(ExpensesActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Establecer usuario actual
        dbHelper = DatabaseHelper.getInstance();
        dbHelper.setCurrentUserId(userId);
        categoryMap = new HashMap<>();
        expensesList = new ArrayList<>();

        // Inicializar vistas
        rvExpenses = findViewById(R.id.rv_expenses);
        bottomNavigation = findViewById(R.id.bottom_navigation_view);
        tvMonthTitle = findViewById(R.id.tv_month_title);

        // Mostrar mes actual en el título
        displayCurrentMonth();

        // Configurar RecyclerView
        setupRecyclerView();

        // Configurar navegación
        setupNavigation();

        // Cargar datos
        loadCategories();
    }

    private void displayCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
        String currentMonthYear = monthFormat.format(calendar.getTime());
        currentMonthYear = currentMonthYear.substring(0, 1).toUpperCase() + currentMonthYear.substring(1);

        tvMonthTitle.setText("Gastos de " + currentMonthYear);
    }

    private void setupRecyclerView() {
        rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExpenseAdapter(expensesList);
        rvExpenses.setAdapter(adapter);
    }

    private void setupNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_gastos);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_resumen) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_gastos) {
                return true;
            } else if (itemId == R.id.nav_historial) {
                startActivity(new Intent(this, HistorialActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_categorias) {
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
                for (Category category : categories) {
                    categoryMap.put(category.getId(), category);
                }
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
        // Cargar solo gastos del tipo GASTO
        dbHelper.loadTransactionsByTypeId(DatabaseHelper.TIPO_GASTO_ID,
                new DatabaseHelper.OnTransactionsLoadedListener() {
                    @Override
                    public void onTransactionsLoaded(List<Transaction> transactions) {
                        expensesList.clear();

                        // Obtener mes y año actual
                        Calendar calendar = Calendar.getInstance();
                        int currentMonth = calendar.get(Calendar.MONTH);
                        int currentYear = calendar.get(Calendar.YEAR);

                        for (Transaction transaction : transactions) {
                            // Filtrar solo transacciones del mes actual
                            Calendar transactionDate = Calendar.getInstance();
                            transactionDate.setTimeInMillis(transaction.getFecha());

                            int transactionMonth = transactionDate.get(Calendar.MONTH);
                            int transactionYear = transactionDate.get(Calendar.YEAR);

                            // Solo mostrar gastos del mes actual
                            if (transactionMonth == currentMonth && transactionYear == currentYear) {
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
                        }

                        adapter.notifyDataSetChanged();

                        if (expensesList.isEmpty()) {
                            Toast.makeText(ExpensesActivity.this,
                                    "No hay gastos este mes",
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