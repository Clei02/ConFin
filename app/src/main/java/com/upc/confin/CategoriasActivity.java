package com.upc.confin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class CategoriasActivity extends AppCompatActivity {

    private LinearLayout contentGastos;
    private LinearLayout contentIngresos;
    private ImageView iconExpandGastos;
    private ImageView iconExpandIngresos;
    private boolean isGastosExpanded = true;
    private boolean isIngresosExpanded = true;

    private DatabaseHelper dbHelper;
    private List<Category> categoriasGasto;
    private List<Category> categoriasIngreso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorias);

        // Verificar sesión
        SharedPreferences prefs = getSharedPreferences("ConFinPrefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", null);

        if (userId == null) {
            Intent intent = new Intent(CategoriasActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        dbHelper = DatabaseHelper.getInstance();
        dbHelper.setCurrentUserId(userId);
        categoriasGasto = new ArrayList<>();
        categoriasIngreso = new ArrayList<>();

        // Inicializar vistas
        initViews();

        // Cargar categorías
        loadCategories();

        // Configurar listeners
        setupListeners();

        // Configurar bottom navigation
        setupBottomNavigation();
    }

    private void initViews() {
        LinearLayout headerGastos = findViewById(R.id.headerGastos);
        LinearLayout headerIngresos = findViewById(R.id.headerIngresos);
        contentGastos = findViewById(R.id.contentGastos);
        contentIngresos = findViewById(R.id.contentIngresos);
        iconExpandGastos = findViewById(R.id.iconExpandGastos);
        iconExpandIngresos = findViewById(R.id.iconExpandIngresos);

        headerGastos.setOnClickListener(v -> toggleGastos());
        headerIngresos.setOnClickListener(v -> toggleIngresos());
    }

    private void setupListeners() {
        FloatingActionButton fab = findViewById(R.id.fabAddCategory);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(CategoriasActivity.this, AgregarCategoriaActivity.class);
            startActivity(intent);
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_view);
        bottomNav.setSelectedItemId(R.id.nav_categorias);

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
                return true;
            }
            return false;
        });
    }

    private void loadCategories() {
        dbHelper.loadCategories(new DatabaseHelper.OnCategoriesLoadedListener() {
            @Override
            public void onCategoriesLoaded(List<Category> categories) {
                // Separar por tipoId
                categoriasGasto.clear();
                categoriasIngreso.clear();

                for (Category category : categories) {
                    if (category.getTipoId().equals(DatabaseHelper.TIPO_GASTO_ID)) {
                        categoriasGasto.add(category);
                    } else if (category.getTipoId().equals(DatabaseHelper.TIPO_INGRESO_ID)) {
                        categoriasIngreso.add(category);
                    }
                }

                // Mostrar categorías
                displayCategories();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(CategoriasActivity.this,
                        "Error cargando categorías: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayCategories() {
        // Limpiar contenedores
        contentGastos.removeAllViews();
        contentIngresos.removeAllViews();

        // Agregar categorías de GASTO
        if (categoriasGasto.isEmpty()) {
            contentGastos.addView(createEmptyMessage("No hay categorías de gasto"));
        } else {
            for (Category category : categoriasGasto) {
                contentGastos.addView(createCategoryCard(category));
            }
        }

        // Agregar categorías de INGRESO
        if (categoriasIngreso.isEmpty()) {
            contentIngresos.addView(createEmptyMessage("No hay categorías de ingreso"));
        } else {
            for (Category category : categoriasIngreso) {
                contentIngresos.addView(createCategoryCard(category));
            }
        }
    }

    private View createEmptyMessage(String message) {
        android.widget.TextView textView = new android.widget.TextView(this);
        textView.setText(message);
        textView.setTextColor(getResources().getColor(android.R.color.darker_gray));
        textView.setTextSize(14);
        textView.setPadding(16, 16, 16, 16);
        return textView;
    }

    private CardView createCategoryCard(Category category) {
        View cardView = getLayoutInflater().inflate(R.layout.item_category_card, null);

        ImageView iconCategory = cardView.findViewById(R.id.iconCategory);
        android.widget.TextView tvCategoryName = cardView.findViewById(R.id.tvCategoryName);
        View iconBackground = cardView.findViewById(R.id.iconBackground);

        tvCategoryName.setText(category.getNombre());

        int iconResId = category.getIconoResId(this);
        if (iconResId != 0) {
            iconCategory.setImageResource(iconResId);
        }

        try {
            int color = android.graphics.Color.parseColor(category.getColor());
            iconBackground.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(color)
            );
        } catch (Exception e) {
            iconBackground.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor("#4A4A4A")
                    )
            );
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 8);
        cardView.setLayoutParams(params);

        return (CardView) cardView;
    }

    private void toggleGastos() {
        if (isGastosExpanded) {
            contentGastos.setVisibility(View.GONE);
            iconExpandGastos.setRotation(0);
        } else {
            contentGastos.setVisibility(View.VISIBLE);
            iconExpandGastos.setRotation(180);
        }
        isGastosExpanded = !isGastosExpanded;
    }

    private void toggleIngresos() {
        if (isIngresosExpanded) {
            contentIngresos.setVisibility(View.GONE);
            iconExpandIngresos.setRotation(0);
        } else {
            contentIngresos.setVisibility(View.VISIBLE);
            iconExpandIngresos.setRotation(180);
        }
        isIngresosExpanded = !isIngresosExpanded;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategories();
    }
}