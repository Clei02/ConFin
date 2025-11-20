package com.upc.confin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private static final int RC_GOOGLE_SIGN_IN = 9001;

    private EditText etName, etUsername, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister, btnGoogleRegister;
    private TextView tvLogin;

    private DatabaseHelper dbHelper;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeViews();
        setupListeners();

        dbHelper = DatabaseHelper.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Configurar Google Sign-In
        configureGoogleSignIn();
    }

    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Limpiar la sesión de Google para permitir elegir cuenta
        if (mGoogleSignInClient != null) {
            mGoogleSignInClient.signOut();
        }
    }

    private void initializeViews() {
        etName = findViewById(R.id.etName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
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
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (!validateInputs(name, username, email, password, confirmPassword)) {
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("Registrando...");

        registerUser(name, username, email, password);
    }

    private boolean validateInputs(String name, String username, String email,
                                   String password, String confirmPassword) {
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

        if (email.isEmpty()) {
            showError("El email no puede estar vacío");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Ingresa un email válido");
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

    private void registerUser(String name, String username, String email, String password) {
        // Primero registrar en Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Ahora guardar en Realtime Database
                            String userId = firebaseUser.getUid();
                            saveUserToDatabase(userId, name, username, email, password);
                        }
                    } else {
                        btnRegister.setEnabled(true);
                        btnRegister.setText("Registrarme");
                        String errorMsg = task.getException() != null ?
                                task.getException().getMessage() : "Error al registrar";
                        showError(errorMsg);
                    }
                });
    }

    private void saveUserToDatabase(String userId, String name, String username,
                                    String email, String password) {
        dbHelper.registerUser(name, username, email, password,
                new DatabaseHelper.OnOperationCompleteListener() {
                    @Override
                    public void onSuccess() {
                        showSuccess("✅ Cuenta creada exitosamente");

                        // Guardar sesión
                        saveUserSession(userId, name, email);

                        // Ir al Dashboard
                        navigateToDashboard();
                    }

                    @Override
                    public void onError(String error) {
                        btnRegister.setEnabled(true);
                        btnRegister.setText("Registrarme");
                        showError("❌ Error: " + error);
                    }
                });
    }

    private void handleGoogleRegister() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                showError("Error al iniciar sesión con Google");
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            String name = firebaseUser.getDisplayName();
                            String email = firebaseUser.getEmail();
                            String photoUrl = firebaseUser.getPhotoUrl() != null ?
                                    firebaseUser.getPhotoUrl().toString() : null;

                            // Verificar si el usuario ya existe
                            dbHelper.getUserById(userId, new DatabaseHelper.OnUserLoadedListener() {
                                @Override
                                public void onUserLoaded(User user) {
                                    // Usuario ya existe, solo hacer login
                                    saveUserSession(userId, name, email);
                                    navigateToDashboard();
                                }

                                @Override
                                public void onUserNotFound() {
                                    // Usuario nuevo, registrar en la base de datos
                                    dbHelper.registerUserWithGoogle(userId, name, email, photoUrl,
                                            new DatabaseHelper.OnOperationCompleteListener() {
                                                @Override
                                                public void onSuccess() {
                                                    showSuccess("✅ Registro exitoso con Google");
                                                    saveUserSession(userId, name, email);
                                                    navigateToDashboard();
                                                }

                                                @Override
                                                public void onError(String error) {
                                                    showError("Error al registrar: " + error);
                                                }
                                            });
                                }

                                @Override
                                public void onError(String error) {
                                    showError("Error al verificar usuario: " + error);
                                }
                            });
                        }
                    } else {
                        showError("Error en autenticación con Google");
                    }
                });
    }

    private void saveUserSession(String userId, String userName, String userEmail) {
        SharedPreferences prefs = getSharedPreferences("ConFinPrefs", MODE_PRIVATE);
        prefs.edit()
                .putString("userId", userId)
                .putString("userName", userName)
                .putString("userEmail", userEmail)
                .apply();

        dbHelper.setCurrentUserId(userId);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}