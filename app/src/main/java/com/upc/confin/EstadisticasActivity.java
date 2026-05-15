package com.upc.confin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EstadisticasActivity extends AppCompatActivity {

    private PieChart pieChart;
    private BarChart barChart;
    private TextView tvMonthYear;

    private DatabaseHelper dbHelper;
    private Map<String, Category> categoryMap;

    private final int[] CATEGORY_COLORS = {
            Color.parseColor("#FF101F"), // Red
            Color.parseColor("#379634"), // Forest Green
            Color.parseColor("#2E86AB"), // Ocean Blue
            Color.parseColor("#FF9800"), // Orange
            Color.parseColor("#9C27B0"), // Purple
            Color.parseColor("#E91E63"), // Pink
            Color.parseColor("#00BCD4"), // Cyan
            Color.parseColor("#795548"), // Brown
            Color.parseColor("#607D8B"), // Blue Grey
            Color.parseColor("#FF5722")  // Deep Orange
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas);

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

        initViews();
        setupBottomNavigation();
        displayCurrentMonth();
        loadData();
    }

    private void initViews() {
        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);
        tvMonthYear = findViewById(R.id.tv_month_year);

        setupPieChart();
        setupBarChart();
    }

    private void setupPieChart() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.setDrawEntryLabels(false);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextColor(Color.parseColor("#343633"));
    }

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setHighlightFullBarEnabled(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setPinchZoom(false);
        barChart.setDoubleTapToZoomEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.parseColor("#343633"));

        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setTextColor(Color.parseColor("#343633"));
        barChart.getAxisLeft().setDrawGridLines(true);
        barChart.getAxisLeft().setGridColor(Color.parseColor("#EEEEEE"));

        Legend legend = barChart.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextColor(Color.parseColor("#343633"));
    }

    private void displayCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
        String currentMonthYear = monthFormat.format(calendar.getTime());
        currentMonthYear = currentMonthYear.substring(0, 1).toUpperCase() + currentMonthYear.substring(1);
        tvMonthYear.setText(currentMonthYear);
    }

    private void loadData() {
        // Primero cargar categorías
        dbHelper.loadCategories(new DatabaseHelper.OnCategoriesLoadedListener() {
            @Override
            public void onCategoriesLoaded(List<Category> categories) {
                categoryMap.clear();
                for (Category category : categories) {
                    categoryMap.put(category.getId(), category);
                }

                // Solo cargar gráficos si hay categorías
                if (!categoryMap.isEmpty()) {
                    loadPieChartData();
                    loadBarChartData();
                } else {
                    // Si no hay categorías, mostrar mensaje
                    Toast.makeText(EstadisticasActivity.this,
                            R.string.info_no_categories,
                            Toast.LENGTH_SHORT).show();
                    pieChart.setNoDataText(getString(R.string.info_create_categories_first));
                    barChart.setNoDataText(getString(R.string.info_create_categories_first));
                    pieChart.invalidate();
                    barChart.invalidate();
                }
            }

@Override
            public void onError(String error) {
                Toast.makeText(EstadisticasActivity.this,
                        getString(R.string.error_loading_categories, error),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPieChartData() {
        dbHelper.getExpensesByCategory(new DatabaseHelper.OnExpensesByCategoryLoadedListener() {
            @Override
            public void onExpensesLoaded(Map<String, Double> expensesByCategory) {
                try {
                    if (expensesByCategory.isEmpty()) {
                        pieChart.setNoDataText(getString(R.string.info_no_expenses));
                        pieChart.invalidate();
                        return;
                    }

                    // Validar que categoryMap tenga datos
                    if (categoryMap.isEmpty()) {
                        pieChart.setNoDataText(getString(R.string.info_loading_categories));
                        pieChart.invalidate();
                        return;
                    }

                    List<PieEntry> entries = new ArrayList<>();
                    List<Integer> colors = new ArrayList<>();
                    int colorIndex = 0;

                    for (Map.Entry<String, Double> entry : expensesByCategory.entrySet()) {
                        Category category = categoryMap.get(entry.getKey());
                        String label = category != null ? category.getNombre() : getString(R.string.chart_no_category);
                        entries.add(new PieEntry(entry.getValue().floatValue(), label));
                        colors.add(CATEGORY_COLORS[colorIndex % CATEGORY_COLORS.length]);
                        colorIndex++;
                    }

                    PieDataSet dataSet = new PieDataSet(entries, "");
                    dataSet.setColors(colors);
                    dataSet.setValueTextSize(12f);
                    dataSet.setValueTextColor(Color.WHITE);
                    dataSet.setSliceSpace(2f);

                    PieData data = new PieData(dataSet);
                    pieChart.setData(data);
                    pieChart.invalidate();
                } catch (Exception e) {
                    Toast.makeText(EstadisticasActivity.this,
                            getString(R.string.chart_error_loading, e.getMessage()),
                            Toast.LENGTH_SHORT).show();
                    pieChart.setNoDataText(getString(R.string.info_error_loading));
                    pieChart.invalidate();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(EstadisticasActivity.this,
                        getString(R.string.error_generic, error),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBarChartData() {
        dbHelper.getMonthlySummary(6, new DatabaseHelper.OnMonthlySummaryLoadedListener() {
            @Override
            public void onSummaryLoaded(List<DatabaseHelper.MonthlySummary> summaries) {
                try {
                    // Validar que hay datos reales
                    boolean hasData = false;
                    for (DatabaseHelper.MonthlySummary s : summaries) {
                        if (s.getTotalIngresos() > 0 || s.getTotalGastos() > 0) {
                            hasData = true;
                            break;
                        }
                    }

                    if (!hasData || summaries.isEmpty()) {
                        barChart.setNoDataText(getString(R.string.info_no_data));
                        barChart.invalidate();
                        return;
                    }

                    List<BarEntry> incomeEntries = new ArrayList<>();
                    List<BarEntry> expenseEntries = new ArrayList<>();
                    List<String> monthLabels = new ArrayList<>();

                    String[] monthNames = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};

                    for (int i = 0; i < summaries.size(); i++) {
                        DatabaseHelper.MonthlySummary summary = summaries.get(i);
                        incomeEntries.add(new BarEntry(i, (float) summary.getTotalIngresos()));
                        expenseEntries.add(new BarEntry(i, (float) summary.getTotalGastos()));
                        monthLabels.add(monthNames[summary.getMonth()]);
                    }

                    BarDataSet incomeDataSet = new BarDataSet(incomeEntries, getString(R.string.chart_income));
                    incomeDataSet.setColor(Color.parseColor("#379634")); // Forest Green
                    incomeDataSet.setValueTextColor(Color.parseColor("#343633"));
                    incomeDataSet.setValueTextSize(10f);

                    BarDataSet expenseDataSet = new BarDataSet(expenseEntries, getString(R.string.chart_expense));
                    expenseDataSet.setColor(Color.parseColor("#FF101F")); // Red
                    expenseDataSet.setValueTextColor(Color.parseColor("#343633"));
                    expenseDataSet.setValueTextSize(10f);

                    BarData barData = new BarData(incomeDataSet, expenseDataSet);
                    barData.setBarWidth(0.35f);

                    // Configurar ejes
                    barChart.getXAxis().setAxisMinimum(0f);
                    barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(monthLabels));
                    
                    // Solo usar groupBars si hay más de 1 mes de datos
                    if (summaries.size() > 1) {
                        float groupSpace = 0.2f;
                        float barSpace = 0.02f;
                        barChart.groupBars(0f, groupSpace, barSpace);
                        barChart.getXAxis().setAxisMaximum(summaries.size());
                    } else {
                        // Para 1 solo mes, no usar groupBars
                        barChart.getXAxis().setAxisMaximum(1f);
                    }

                    barChart.setData(barData);
                    barChart.invalidate();
                } catch (Exception e) {
                    Toast.makeText(EstadisticasActivity.this,
                            getString(R.string.chart_error_loading, e.getMessage()),
                            Toast.LENGTH_SHORT).show();
                    barChart.setNoDataText(getString(R.string.info_error_loading));
                    barChart.invalidate();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(EstadisticasActivity.this,
                        getString(R.string.error_generic, error),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_view);
        bottomNav.setSelectedItemId(R.id.nav_estadisticas);

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
                startActivity(new Intent(this, HistorialActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_categorias) {
                startActivity(new Intent(this, CategoriasActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_estadisticas) {
                return true;
            }
            return false;
        });
    }
}