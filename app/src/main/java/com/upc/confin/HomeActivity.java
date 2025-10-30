package com.upc.confin; // Reemplaza con tu package name

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu; // Importar PopupMenu
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView; // Importar BottomNavigationView
import com.google.firebase.auth.FirebaseAuth; // Importar Firebase Auth

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    // --- Variables de la UI ---
    private RecyclerView recyclerViewTransactions;
    private BottomNavigationView bottomNavigation;
    private ImageView ivProfileIcon; // Variable para el icono de perfil

    // --- Variables de Datos y Firebase ---
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionList;
    private FirebaseAuth mAuth; // Instancia de Firebase Authentication

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // --- Inicializar Firebase Auth ---
        mAuth = FirebaseAuth.getInstance();

        // --- Inicializar Vistas ---
        recyclerViewTransactions = findViewById(R.id.recycler_view_transactions);
        bottomNavigation = findViewById(R.id.bottom_navigation_view);
        ivProfileIcon = findViewById(R.id.iv_profile_icon); // Conectar el icono de perfil

        // --- Configurar Componentes ---
        setupRecyclerView();
        loadTransactionData();
        setupNavigation(); // Método para la navegación inferior
        setupProfileMenu(); // Nuevo método para el menú de perfil
    }

    /**
     * Configura el listener del icono de perfil para mostrar el menú.
     */
    private void setupProfileMenu() {
        ivProfileIcon.setOnClickListener(view -> {
            // 1. Crear el PopupMenu y anclarlo al icono
            PopupMenu popup = new PopupMenu(this, ivProfileIcon);

            // 2. Inflar (cargar) el menú que creamos (profile_menu.xml)
            popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());

            // 3. Definir la acción al hacer clic en una opción
            popup.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.menu_sign_out) {
                    // Si el usuario selecciona "Cerrar Sesión", llamamos al método
                    signOut();
                    return true;
                }
                return false;
            });

            // 4. Mostrar el menú
            popup.show();
        });
    }

    /**
     * Cierra la sesión del usuario en Firebase y lo regresa al Login.
     */
    private void signOut() {
        // Cierra la sesión en Firebase Authentication
        mAuth.signOut();

        // Prepara el Intent para volver a la pantalla de Login (MainActivity)
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);

        // Estas "flags" son MUY IMPORTANTES. Limpian el historial de navegación
        // para que el usuario no pueda "volver" a HomeActivity con el botón de atrás.
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish(); // Cierra esta actividad (HomeActivity)
    }

    /**
     * Configura la barra de navegación inferior (BottomNavigationView).
     */
    private void setupNavigation() {
        // Marca "Resumen" como el ítem seleccionado.
        bottomNavigation.setSelectedItemId(R.id.nav_resumen);

        // Listener para los clics en el menú.
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_resumen) {
                // Ya estamos aquí, no hacer nada.
                return true;
            } else if (itemId == R.id.nav_gastos) {
                startActivity(new Intent(this, ExpensesActivity.class));
                return true;
            } else if (itemId == R.id.nav_categorias) {
                startActivity(new Intent(this, CategoriasActivity.class));
                return true;
            }
            return false;
        });
    }

    /**
     * Configura el RecyclerView (tu código original).
     */
    private void setupRecyclerView() {
        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(this, transactionList);
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTransactions.setAdapter(transactionAdapter);
    }

    /**
     * Carga los datos de ejemplo (tu código original).
     */
    private void loadTransactionData() {
        transactionList.add(new Transaction(R.drawable.ic_shopping_cart, "Supermercado Central", "Alimentación • 15 Ene", 85.50));
        transactionList.add(new Transaction(R.drawable.ic_local_gas_station, "Gasolinera Shell", "Transporte • 14 Ene", 45.00));
        transactionList.add(new Transaction(R.drawable.ic_live_tv, "Netflix", "Entretenimiento • 13 Ene", 12.99));
        transactionList.add(new Transaction(R.drawable.ic_restaurant, "Restaurante Italiano", "Restaurantes • 12 Ene", 67.80));
        transactionAdapter.notifyDataSetChanged();
    }
}