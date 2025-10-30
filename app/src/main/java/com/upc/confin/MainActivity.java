package com.upc.confin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth; // Importar Firebase Auth
import com.google.firebase.auth.FirebaseUser; // Importar Firebase User

public class MainActivity extends AppCompatActivity {

    // --- Componentes de la UI (IDs de tu XML) ---
    private TextInputEditText etUsuario;
    private TextInputEditText etContraseña;
    private MaterialButton btnIniciarSesion;
    private MaterialButton btnGoogleSignIn;
    private TextView tvRegistro;

    // --- Firebase ---
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main); // Tu XML de login

        // --- Inicializar Firebase Auth ---
        mAuth = FirebaseAuth.getInstance();

        initializeViews();
        setupWindowInsets(); // Tu código original
        setupListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // --- VERIFICACIÓN DE SESIÓN ---
        // Comprueba si el usuario ya ha iniciado sesión al abrir la app.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Si ya hay una sesión activa, lo mandamos directo a la pantalla principal.
            navigateToHome();
        }
    }

    private void initializeViews() {
        // Conectamos las variables con los IDs de tu activity_main.xml
        etUsuario = findViewById(R.id.etUsuario);
        etContraseña = findViewById(R.id.etContraseña);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        tvRegistro = findViewById(R.id.tvRegistro);
    }

    private void setupWindowInsets() {
        // Este es tu código original, está perfecto.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupListeners() {
        // Listener para el botón de iniciar sesión
        btnIniciarSesion.setOnClickListener(v -> validateAndLogin());

        // Listener para el botón de Google (lógica pendiente)
        btnGoogleSignIn.setOnClickListener(v -> handleGoogleSignIn());

        // Listener para el texto "Regístrate"
        tvRegistro.setOnClickListener(v -> navigateToRegister());
    }

    /**
     * Valida los campos de login antes de consultar a Firebase.
     */
    private void validateAndLogin() {
        String email = etUsuario.getText().toString().trim(); // etUsuario es el email
        String password = etContraseña.getText().toString().trim();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etUsuario.setError("Ingresa un correo válido.");
            etUsuario.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etContraseña.setError("La contraseña es requerida.");
            etContraseña.requestFocus();
            return;
        }

        // Si es válido, llamamos a Firebase para iniciar sesión
        loginUserWithFirebase(email, password);
    }

    /**
     * Inicia sesión en Firebase Authentication.
     */
    private void loginUserWithFirebase(String email, String password) {
        // progressBar.setVisibility(View.VISIBLE); // Mostrar ProgressBar

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    // progressBar.setVisibility(View.GONE); // Ocultar ProgressBar
                    if (task.isSuccessful()) {
                        // Inicio de sesión exitoso
                        Toast.makeText(MainActivity.this, "Inicio de sesión exitoso.", Toast.LENGTH_SHORT).show();
                        navigateToHome();
                    } else {
                        // Si falla (contraseña incorrecta, usuario no existe), Firebase nos da el error.
                        Toast.makeText(MainActivity.this, "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void handleGoogleSignIn() {
        // TODO: Implementar autenticación con Google
        Toast.makeText(this, "Google Sign-In (próximamente)", Toast.LENGTH_SHORT).show();
    }

    private void navigateToRegister() {
        // Abre la RegisterActivity que creamos
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    /**
     * Navega a la pantalla principal y limpia el historial de navegación.
     */
    private void navigateToHome() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class); // Asumo que tu principal es HomeActivity
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Cierra la actividad de login
    }
}