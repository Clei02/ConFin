package com.upc.confin; // Reemplaza con tu paquete

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class ExpensesActivity extends AppCompatActivity {

    // --- Variables de la UI y de Datos ---
    private RecyclerView rvExpenses;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. Establece el layout de la actividad.
        setContentView(R.layout.activity_expenses);

        // 2. Conecta las variables con las vistas del XML.
        rvExpenses = findViewById(R.id.rv_expenses);
        bottomNavigation = findViewById(R.id.bottom_navigation_view);

        // 3. Llama a los métodos para configurar la lista y la navegación.
        setupRecyclerView();
        setupNavigation();
    }

    /**
     * Prepara y muestra la lista de gastos en el RecyclerView.
     */
    private void setupRecyclerView() {
        // Define que la lista será vertical.
        rvExpenses.setLayoutManager(new LinearLayoutManager(this));

        // Crea los datos de ejemplo.
        List<Expense> expenses = createFakeExpenses();

        // Crea el adaptador y se lo asigna al RecyclerView.
        ExpenseAdapter adapter = new ExpenseAdapter(expenses);
        rvExpenses.setAdapter(adapter);
    }

    /**
     * Configura la barra de navegación inferior.
     */
    private void setupNavigation() {
        // Marca "Gastos" como el ítem seleccionado.
        bottomNavigation.setSelectedItemId(R.id.nav_expenses);

        // Listener para los clics en el menú.
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_summary) {
                // Ir a la pantalla de Resumen (HomeActivity).
                startActivity(new Intent(this, HomeActivity.class));
                finish(); // Cierra esta pantalla.
                return true;
            } else if (itemId == R.id.nav_expenses) {
                // Ya estamos aquí, no hacer nada.
                return true;
            } else if (itemId == R.id.nav_categories) {
                // Ir a la pantalla de Categorías.
                startActivity(new Intent(this, CategoriasActivity.class));
                finish(); // Cierra esta pantalla.
                return true;
            }
            return false;
        });
    }

    /**
     * Genera una lista de gastos de ejemplo para mostrar.
     * En una app real, estos datos vendrían de una base de datos.
     */
    private List<Expense> createFakeExpenses() {
        List<Expense> expenseList = new ArrayList<>();
        // Asegúrate de tener los iconos en res/drawable.
        expenseList.add(new Expense("Comida", "15 Enero 2025", "-$25.50", R.drawable.ic_restaurant));
        expenseList.add(new Expense("Transporte", "14 Enero 2025", "-$12.00", R.drawable.ic_transporte));
        expenseList.add(new Expense("Compras", "13 Enero 2025", "-$89.99", R.drawable.ic_compras));
        expenseList.add(new Expense("Hogar", "12 Enero 2025", "-$156.75", R.drawable.ic_hogar));
        expenseList.add(new Expense("Entretenimiento", "11 Enero 2025", "-$42.30", R.drawable.ic_entretenimiento));
        expenseList.add(new Expense("Salud", "10 Enero 2025", "-$75.00", R.drawable.ic_entretenimiento));
        return expenseList;
    }
}