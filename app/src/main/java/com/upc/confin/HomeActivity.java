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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PROFILE = 1001;

    private RecyclerView recyclerViewTransactions;
    private TransactionAdapter transactionAdapter;
    private List<TransactionDisplay> transactionDisplayList;

    private TextView textGreeting;
    private TextView textMonthYear;
    private TextView textSaldoAmount;
    private TextView textIngresosAmount;
    private TextView textGastosAmount;
    private ImageView ivProfileIcon;

    private DatabaseHelper dbHelper;
    private Map<String, Category> categoryMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Verificar sesión
        SharedPreferences prefs = getSharedPreferences("ConFinPrefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", null);
        String userName = prefs.getString("userName", "Usuario");

        if (userId == null) {
            // No hay sesión, volver al login
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Establecer usuario actual
        dbHelper = DatabaseHelper.getInstance();
        dbHelper.setCurrentUserId(userId);
        categoryMap = new HashMap<>();

        // Inicializar vistas
        initViews();

        // Mostrar nombre del usuario y fecha actual
        displayUserInfo(userName);

        // Configurar RecyclerView
        setupRecyclerView();

        // Cargar categorías primero, luego transacciones
        loadCategories();

        // Configurar FAB
        com.google.android.material.floatingactionbutton.FloatingActionButton fab =
                findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AgregarTransaccionActivity.class);
            startActivity(intent);
        });

        // Configurar Bottom Navigation
        setupBottomNavigation();

        // Configurar botón de perfil
        setupProfileButton();
    }

    private void initViews() {
        recyclerViewTransactions = findViewById(R.id.recycler_view_transactions);
        textGreeting = findViewById(R.id.text_greeting);
        textMonthYear = findViewById(R.id.text_month_year);
        textSaldoAmount = findViewById(R.id.text_saldo_amount);
        textIngresosAmount = findViewById(R.id.text_ingresos_amount);
        textGastosAmount = findViewById(R.id.text_gastos_amount);
        ivProfileIcon = findViewById(R.id.iv_profile_icon);
    }

    private void displayUserInfo(String userName) {
        // Mostrar saludo con el nombre del usuario
        textGreeting.setText("¡Hola, " + userName + "!");

        // Mostrar mes y año actual
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
        String currentMonthYear = monthFormat.format(calendar.getTime());

        // Capitalizar primera letra
        currentMonthYear = currentMonthYear.substring(0, 1).toUpperCase() + currentMonthYear.substring(1);

        textMonthYear.setText(currentMonthYear);
    }

    private void setupRecyclerView() {
        transactionDisplayList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(this, transactionDisplayList);
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTransactions.setAdapter(transactionAdapter);
    }

    private void setupProfileButton() {
        ivProfileIcon.setOnClickListener(v -> showProfileBottomSheet());
    }

    private void showProfileBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_profile, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Configurar opción de ver perfil
        LinearLayout optionViewProfile = bottomSheetView.findViewById(R.id.option_view_profile);
        optionViewProfile.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivityForResult(intent, REQUEST_CODE_PROFILE); // 👈 CAMBIO AQUÍ
        });

        // Configurar opción de cerrar sesión
        LinearLayout optionSignOut = bottomSheetView.findViewById(R.id.option_sign_out);
        optionSignOut.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            signOut();
        });

        bottomSheetDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PROFILE && resultCode == RESULT_OK) {
            // El usuario actualizó su perfil, recargar datos
            reloadUserData();
        }
    }

    private void reloadUserData() {
        SharedPreferences prefs = getSharedPreferences("ConFinPrefs", MODE_PRIVATE);
        String userName = prefs.getString("userName", "Usuario");

        // Actualizar el saludo
        textGreeting.setText("¡Hola, " + userName + "!");

        // Opcional: Mostrar mensaje
        Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
    }

    private void signOut() {
        // Limpiar sesión
        SharedPreferences prefs = getSharedPreferences("ConFinPrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();

        // Limpiar usuario actual en DatabaseHelper
        dbHelper.setCurrentUserId(null);

        // Mostrar mensaje
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();

        // Ir al login
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadCategories() {
        dbHelper.loadCategories(new DatabaseHelper.OnCategoriesLoadedListener() {
            @Override
            public void onCategoriesLoaded(List<Category> categories) {
                for (Category category : categories) {
                    categoryMap.put(category.getId(), category);
                }
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
                transactionDisplayList.clear();

                double totalIngresos = 0;
                double totalGastos = 0;

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

                    if (transactionMonth == currentMonth && transactionYear == currentYear) {
                        Category category = categoryMap.get(transaction.getCategoriaId());

                        if (category != null) {
                            int iconResId = category.getIconoResId(HomeActivity.this);
                            String dateStr = formatDate(transaction.getFecha());
                            String detail = category.getNombre() + " • " + dateStr;

                            // Determinar el tipo basado en tipoId
                            String tipoNombre = transaction.getTipoId().equals(DatabaseHelper.TIPO_INGRESO_ID)
                                    ? "INGRESO" : "GASTO";

                            if (transactionDisplayList.size() < 5) {
                                transactionDisplayList.add(new TransactionDisplay(
                                        iconResId,
                                        transaction.getDescripcion(),
                                        detail,
                                        transaction.getMonto(),
                                        tipoNombre
                                ));
                            }

                            // Calcular totales solo del mes actual
                            if (transaction.getTipoId().equals(DatabaseHelper.TIPO_INGRESO_ID)) {
                                totalIngresos += transaction.getMonto();
                            } else {
                                totalGastos += transaction.getMonto();
                            }
                        }
                    }
                }

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

        bottomNav.setSelectedItemId(R.id.nav_resumen);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_resumen) {
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
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategories();
    }

    // Clase interna para mostrar transacciones
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