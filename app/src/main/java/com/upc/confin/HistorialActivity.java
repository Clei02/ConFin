package com.upc.confin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistorialActivity extends AppCompatActivity {

    private LinearLayout containerHistorial;
    private DatabaseHelper dbHelper;
    private Map<String, Category> categoryMap;

    // Estructura: año -> mes -> lista de transacciones
    private Map<Integer, Map<Integer, List<Transaction>>> historialData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        // Verificar sesión
        SharedPreferences prefs = getSharedPreferences("ConFinPrefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", null);

        if (userId == null) {
            finish();
            return;
        }

        dbHelper = DatabaseHelper.getInstance();
        dbHelper.setCurrentUserId(userId);
        categoryMap = new HashMap<>();
        historialData = new HashMap<>();

        containerHistorial = findViewById(R.id.containerHistorial);

        loadCategories();
        setupBottomNavigation();
    }

    private void loadCategories() {
        dbHelper.loadCategories(new DatabaseHelper.OnCategoriesLoadedListener() {
            @Override
            public void onCategoriesLoaded(List<Category> categories) {
                for (Category category : categories) {
                    categoryMap.put(category.getId(), category);
                }
                loadAllTransactions();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(HistorialActivity.this,
                        "Error cargando categorías: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAllTransactions() {
        dbHelper.loadTransactions(new DatabaseHelper.OnTransactionsLoadedListener() {
            @Override
            public void onTransactionsLoaded(List<Transaction> transactions) {
                organizeTransactionsByMonth(transactions);
                displayHistorial();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(HistorialActivity.this,
                        "Error cargando transacciones: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void organizeTransactionsByMonth(List<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(transaction.getFecha());

            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);

            if (!historialData.containsKey(year)) {
                historialData.put(year, new HashMap<>());
            }

            if (!historialData.get(year).containsKey(month)) {
                historialData.get(year).put(month, new ArrayList<>());
            }

            historialData.get(year).get(month).add(transaction);
        }
    }

    private void displayHistorial() {
        containerHistorial.removeAllViews();

        // Obtener años ordenados (más reciente primero)
        List<Integer> years = new ArrayList<>(historialData.keySet());
        Collections.sort(years, Collections.reverseOrder());

        for (int year : years) {
            Map<Integer, List<Transaction>> monthsData = historialData.get(year);

            // Obtener meses ordenados (más reciente primero)
            List<Integer> months = new ArrayList<>(monthsData.keySet());
            Collections.sort(months, Collections.reverseOrder());

            for (int month : months) {
                List<Transaction> monthTransactions = monthsData.get(month);
                containerHistorial.addView(createMonthCard(year, month, monthTransactions));
            }
        }

        if (years.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("No hay transacciones registradas");
            emptyView.setTextColor(getResources().getColor(android.R.color.darker_gray));
            emptyView.setTextSize(16);
            emptyView.setPadding(16, 32, 16, 16);
            containerHistorial.addView(emptyView);
        }
    }

    private View createMonthCard(int year, int month, List<Transaction> transactions) {
        View cardView = getLayoutInflater().inflate(R.layout.item_month_header, null);

        TextView tvMonthYear = cardView.findViewById(R.id.tvMonthYear);
        TextView tvMonthSummary = cardView.findViewById(R.id.tvMonthSummary);
        TextView tvIngresos = cardView.findViewById(R.id.tvIngresos);
        TextView tvGastos = cardView.findViewById(R.id.tvGastos);
        LinearLayout monthHeader = cardView.findViewById(R.id.monthHeader);
        LinearLayout monthContent = cardView.findViewById(R.id.monthContent);
        LinearLayout transactionsList = cardView.findViewById(R.id.transactionsList);
        ImageView iconExpand = cardView.findViewById(R.id.iconExpand);

        // Formatear mes y año
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
        String monthYearStr = sdf.format(cal.getTime());
        monthYearStr = monthYearStr.substring(0, 1).toUpperCase() + monthYearStr.substring(1);
        tvMonthYear.setText(monthYearStr);

        // Calcular totales
        double totalIngresos = 0;
        double totalGastos = 0;

        for (Transaction transaction : transactions) {
            if (transaction.getTipoId().equals(DatabaseHelper.TIPO_INGRESO_ID)) {
                totalIngresos += transaction.getMonto();
            } else {
                totalGastos += transaction.getMonto();
            }
        }

        double saldo = totalIngresos - totalGastos;
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

        tvMonthSummary.setText("Saldo: " + currencyFormat.format(saldo));
        tvMonthSummary.setTextColor(saldo >= 0
                ? getResources().getColor(R.color.green_income)
                : getResources().getColor(android.R.color.holo_red_dark));

        tvIngresos.setText("+" + currencyFormat.format(totalIngresos));
        tvGastos.setText("-" + currencyFormat.format(totalGastos));

        // Agregar transacciones
        for (Transaction transaction : transactions) {
            transactionsList.addView(createTransactionItem(transaction));
        }

        // Toggle expandible
        final boolean[] isExpanded = {false};
        monthHeader.setOnClickListener(v -> {
            isExpanded[0] = !isExpanded[0];
            monthContent.setVisibility(isExpanded[0] ? View.VISIBLE : View.GONE);
            iconExpand.setRotation(isExpanded[0] ? 180 : 0);
        });

        return cardView;
    }

    private View createTransactionItem(Transaction transaction) {
        View itemView = getLayoutInflater().inflate(R.layout.item_transaction, null);

        ImageView iconCategory = itemView.findViewById(R.id.icon_category);
        TextView textTransactionName = itemView.findViewById(R.id.text_transaction_name);
        TextView textTransactionDetail = itemView.findViewById(R.id.text_transaction_detail);
        TextView textAmount = itemView.findViewById(R.id.text_amount);

        Category category = categoryMap.get(transaction.getCategoriaId());

        if (category != null) {
            int iconResId = category.getIconoResId(this);
            iconCategory.setImageResource(iconResId);

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", new Locale("es", "CO"));
            String dateStr = sdf.format(transaction.getFecha());
            textTransactionDetail.setText(category.getNombre() + " • " + dateStr);
        }

        textTransactionName.setText(transaction.getDescripcion());

        String sign = transaction.getTipoId().equals(DatabaseHelper.TIPO_INGRESO_ID) ? "+" : "-";
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        textAmount.setText(sign + currencyFormat.format(transaction.getMonto()));

        int color = transaction.getTipoId().equals(DatabaseHelper.TIPO_INGRESO_ID)
                ? getResources().getColor(R.color.green_income)
                : getResources().getColor(R.color.text_primary_dark);
        textAmount.setTextColor(color);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 8);
        itemView.setLayoutParams(params);

        return itemView;
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_view);
        bottomNav.setSelectedItemId(R.id.nav_historial);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_resumen) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_gastos) {
                startActivity(new Intent(this, ExpensesActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_historial) {
                return true;
            } else if (itemId == R.id.nav_categorias) {
                startActivity(new Intent(this, CategoriasActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }
}