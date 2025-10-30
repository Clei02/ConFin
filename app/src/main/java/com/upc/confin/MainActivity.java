package com.upc.confin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etUsuario;
    private TextInputEditText etContraseña;
    private MaterialButton btnIniciarSesion;
    private MaterialButton btnGoogleSignIn;
    private TextView tvRegistro;

    // Firebase
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupWindowInsets();
        setupListeners();

        // Inicializar Firebase y probar conexión
        inicializarFirebase();
        probarConexionFirebase();
    }

    private void inicializarFirebase() {
        database = FirebaseDatabase.getInstance("https://confindb-default-rtdb.firebaseio.com/");
        myRef = database.getReference();
    }

    private void probarConexionFirebase() {
        // Enviar mensaje de prueba
        myRef.child("prueba").setValue("¡Hola desde ConFin! 🔥")
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firebase", "Conexión exitosa");
                    Toast.makeText(MainActivity.this,
                            "✅ Firebase conectado correctamente",
                            Toast.LENGTH_LONG).show();

                    // Inicializar categorías por defecto
                    inicializarCategoriasDefault();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Error de conexión: " + e.getMessage());
                    Toast.makeText(MainActivity.this,
                            "❌ Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void inicializarCategoriasDefault() {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();

        // Verificar si ya existen categorías
        dbHelper.loadCategories(new DatabaseHelper.OnCategoriesLoadedListener() {
            @Override
            public void onCategoriesLoaded(List<Category> categories) {
                if (categories.isEmpty()) {
                    // No hay categorías, crear las por defecto
                    dbHelper.createDefaultCategories(new DatabaseHelper.OnOperationCompleteListener() {
                        @Override
                        public void onSuccess() {
                            Log.d("Firebase", "✅ Categorías por defecto creadas");
                            Toast.makeText(MainActivity.this,
                                    "Categorías inicializadas",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String error) {
                            Log.e("Firebase", "Error creando categorías: " + error);
                        }
                    });
                } else {
                    Log.d("Firebase", "Las categorías ya existen (" + categories.size() + ")");
                }
            }

            @Override
            public void onError(String error) {
                Log.e("Firebase", "Error verificando categorías: " + error);
            }
        });
    }

    private void initializeViews() {
        etUsuario = findViewById(R.id.etUsuario);
        etContraseña = findViewById(R.id.etContraseña);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        tvRegistro = findViewById(R.id.tvRegistro);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupListeners() {
        btnIniciarSesion.setOnClickListener(v -> handleLogin());
        btnGoogleSignIn.setOnClickListener(v -> handleGoogleSignIn());
        tvRegistro.setOnClickListener(v -> navigateToRegister());
    }

    private void handleLogin() {
        String usuario = etUsuario.getText().toString().trim();
        String contraseña = etContraseña.getText().toString();

        if (!validateLoginInputs(usuario, contraseña)) {
            return;
        }

        // Por ahora hacemos login directo (sin validación real)
        // TODO: Implementar validación real de usuarios
        Toast.makeText(this, "¡Bienvenido! 👋", Toast.LENGTH_SHORT).show();

        // Navegar al Dashboard
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        finish(); // Cerrar el login
    }

    private boolean validateLoginInputs(String usuario, String contraseña) {
        if (usuario.isEmpty()) {
            showError("El usuario no puede estar vacío");
            return false;
        }

        if (contraseña.isEmpty()) {
            showError("La contraseña no puede estar vacía");
            return false;
        }

        return true;
    }

    private void handleGoogleSignIn() {
        // TODO: Implementar autenticación con Google
        Toast.makeText(this, "Google Sign-In coming soon", Toast.LENGTH_SHORT).show();
    }

    private void navigateToRegister() {
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}