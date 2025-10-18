package com.upc.confin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etUsername, etPassword, etConfirmPassword;
    private Button btnRegister, btnGoogleRegister;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        etName = findViewById(R.id.etName);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoogleRegister = findViewById(R.id.btnGoogleRegister);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> handleRegister());
        btnGoogleRegister.setOnClickListener(v -> handleGoogleRegister());
        tvLogin.setOnClickListener(v -> navigateToLogin());
    }

    private void handleRegister() {
        String name = etName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (!validateInputs(name, username, password, confirmPassword)) {
            return;
        }

        registerUser(name, username, password);
    }

    private boolean validateInputs(String name, String username, String password, String confirmPassword) {
        if (name.isEmpty()) {
            showError("El nombre no puede estar vacío");
            return false;
        }

        if (name.length() < 3) {
            showError("El nombre debe tener al menos 3 caracteres");
            return false;
        }

        if (username.isEmpty()) {
            showError("El usuario no puede estar vacío");
            return false;
        }

        if (username.length() < 4) {
            showError("El usuario debe tener al menos 4 caracteres");
            return false;
        }

        if (password.isEmpty()) {
            showError("La contraseña no puede estar vacía");
            return false;
        }

        if (password.length() < 6) {
            showError("La contraseña debe tener al menos 6 caracteres");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showError("Las contraseñas no coinciden");
            return false;
        }

        return true;
    }

    private void registerUser(String name, String username, String password) {
        // TODO: Implementar lógica de registro con base de datos
        // Por ahora, mostramos un mensaje de éxito
        showSuccess("Cuenta creada exitosamente");

        // Navegar a Dashboard después de 1.5 segundos
        new android.os.Handler().postDelayed(() -> {
            Intent intent = new Intent(String.valueOf(RegisterActivity.this));
            startActivity(intent);
            finish();
        }, 1500);
    }

    private void handleGoogleRegister() {
        // TODO: Implementar autenticación con Google
        showInfo("Google Sign-In coming soon");
    }

    private void navigateToLogin() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showInfo(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}