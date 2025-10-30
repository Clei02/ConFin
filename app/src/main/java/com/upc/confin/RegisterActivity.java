package com.upc.confin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ProgressBar; // Importante
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    // --- Componentes de la UI (IDs de tu XML) ---
    private TextInputEditText etName, etUsername, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private TextView tvLogin;
    // private ProgressBar progressBar; // Descomenta si añades un ProgressBar a tu XML

    // --- Firebase ---
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // Tu archivo XML

        // --- Inicializar Firebase ---
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference(); // Apunta a la raíz de la BD

        // --- Enlazar Vistas ---
        initializeViews();

        // --- Configurar Listeners ---
        setupListeners();
    }

    private void initializeViews() {
        // Conectamos las variables con los IDs de tu activity_register.xml
        etName = findViewById(R.id.etName);
        etUsername = findViewById(R.id.etUsername); // Este será el email
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setupListeners() {
        // Listener para el botón de registrar
        btnRegister.setOnClickListener(v -> validateAndRegister());

        // Listener para el texto "Iniciar sesión"
        tvLogin.setOnClickListener(v -> {
            // Cierra esta actividad y vuelve a la anterior (MainActivity)
            finish();
        });
    }

    /**
     * Valida todos los campos de entrada antes de intentar el registro.
     */
    private void validateAndRegister() {
        // Obtener los valores de los campos
        String nombre = etName.getText().toString().trim();
        String email = etUsername.getText().toString().trim(); // Usamos 'etUsername' para el email
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // --- Validaciones ---
        if (TextUtils.isEmpty(nombre)) {
            etName.setError("El nombre es requerido.");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etUsername.setError("El correo es requerido.");
            etUsername.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etUsername.setError("Ingresa un correo válido.");
            etUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("La contraseña es requerida.");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres.");
            etPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Confirma tu contraseña.");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Las contraseñas no coinciden.");
            etConfirmPassword.requestFocus();
            return;
        }

        // Si todas las validaciones son correctas, procedemos a registrar en Firebase
        registerUserInFirebase(nombre, email, password);
    }

    /**
     * Paso 1: Crea el usuario en Firebase Authentication.
     * Paso 2: Si es exitoso, guarda los datos en Realtime Database.
     */
    private void registerUserInFirebase(String nombre, String email, String password) {
        // progressBar.setVisibility(View.VISIBLE); // Mostrar ProgressBar

        // --- PASO 1: CREAR USUARIO EN FIREBASE AUTH ---
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // El usuario fue creado exitosamente en Authentication
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        String userId = firebaseUser.getUid();

                        // --- PASO 2: GUARDAR DATOS EN REALTIME DATABASE ---
                        User user = new User(nombre, email);

                        // Escribimos en la base de datos bajo el nodo "usuarios" y el ID único del usuario
                        mDatabase.child("usuarios").child(userId).setValue(user)
                                .addOnCompleteListener(dbTask -> {
                                    // progressBar.setVisibility(View.GONE); // Ocultar ProgressBar
                                    if (dbTask.isSuccessful()) {
                                        // Éxito en Auth y Realtime DB
                                        Toast.makeText(RegisterActivity.this, "Registro exitoso.", Toast.LENGTH_SHORT).show();
                                        navigateToHome();
                                    } else {
                                        // Falló al escribir en la BD
                                        Toast.makeText(RegisterActivity.this, "Error al guardar datos: " + dbTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });

                    } else {
                        // Falló la creación en Auth (ej. el correo ya existe)
                        // progressBar.setVisibility(View.GONE); // Ocultar ProgressBar
                        Toast.makeText(RegisterActivity.this, "Error en el registro: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Navega a la pantalla principal y limpia el historial de navegación.
     */
    private void navigateToHome() {
        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class); // Asumo que tu principal es HomeActivity
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}