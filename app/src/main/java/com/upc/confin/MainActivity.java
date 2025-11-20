package com.upc.confin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int RC_GOOGLE_SIGN_IN = 9001;

    private TextInputEditText etEmail;
    private TextInputEditText etContraseña;
    private MaterialButton btnIniciarSesion;
    private MaterialButton btnGoogleSignIn;
    private TextView tvRegistro;

    private DatabaseHelper dbHelper;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupWindowInsets();
        setupListeners();

        dbHelper = DatabaseHelper.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Configurar Google Sign-In
        configureGoogleSignIn();

        // Verificar si ya hay sesión iniciada
        checkExistingSession();

        // Inicializar datos por defecto
        initializeDefaultData();
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

    private void checkExistingSession() {
        SharedPreferences prefs = getSharedPreferences("ConFinPrefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", null);

        if (userId != null) {
            dbHelper.setCurrentUserId(userId);
            navigateToDashboard();
        }
    }

    private void initializeDefaultData() {
        dbHelper.initializeDefaultTypes(new DatabaseHelper.OnOperationCompleteListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Tipos inicializados");

                dbHelper.loadCategories(new DatabaseHelper.OnCategoriesLoadedListener() {
                    @Override
                    public void onCategoriesLoaded(java.util.List<Category> categories) {
                        if (categories.isEmpty()) {
                            dbHelper.createDefaultCategories(new DatabaseHelper.OnOperationCompleteListener() {
                                @Override
                                public void onSuccess() {
                                    Log.d(TAG, "Categorías por defecto creadas");
                                }

                                @Override
                                public void onError(String error) {
                                    Log.e(TAG, "Error creando categorías: " + error);
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error verificando categorías: " + error);
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error inicializando tipos: " + error);
            }
        });
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etUsuario);
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
        String email = etEmail.getText().toString().trim();
        String contraseña = etContraseña.getText().toString();

        Log.d(TAG, "Intentando login con email: " + email);

        if (!validateLoginInputs(email, contraseña)) {
            return;
        }

        btnIniciarSesion.setEnabled(false);
        btnIniciarSesion.setText("Iniciando...");

        // Login con Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, contraseña)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();

                            // Obtener datos del usuario de la BD
                            dbHelper.getUserById(userId, new DatabaseHelper.OnUserLoadedListener() {
                                @Override
                                public void onUserLoaded(User user) {
                                    saveUserSession(userId, user.getNombre(), user.getEmail());
                                    Toast.makeText(MainActivity.this,
                                            "¡Bienvenido, " + user.getNombre() + "!",
                                            Toast.LENGTH_SHORT).show();
                                    navigateToDashboard();
                                }

                                @Override
                                public void onUserNotFound() {
                                    btnIniciarSesion.setEnabled(true);
                                    btnIniciarSesion.setText("Iniciar Sesión");
                                    showError("Usuario no encontrado en la base de datos");
                                }

                                @Override
                                public void onError(String error) {
                                    btnIniciarSesion.setEnabled(true);
                                    btnIniciarSesion.setText("Iniciar Sesión");
                                    showError("Error: " + error);
                                }
                            });
                        }
                    } else {
                        btnIniciarSesion.setEnabled(true);
                        btnIniciarSesion.setText("Iniciar Sesión");
                        String errorMsg = task.getException() != null ?
                                task.getException().getMessage() : "Error al iniciar sesión";
                        showError(errorMsg);
                    }
                });
    }

    private boolean validateLoginInputs(String email, String contraseña) {
        if (email.isEmpty()) {
            showError("El email no puede estar vacío");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Ingresa un email válido");
            return false;
        }

        if (contraseña.isEmpty()) {
            showError("La contraseña no puede estar vacía");
            return false;
        }

        return true;
    }

    private void handleGoogleSignIn() {
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
                                    // Usuario ya existe, hacer login
                                    saveUserSession(userId, user.getNombre(), user.getEmail());
                                    Toast.makeText(MainActivity.this,
                                            "¡Bienvenido de nuevo, " + user.getNombre() + "!",
                                            Toast.LENGTH_SHORT).show();
                                    navigateToDashboard();
                                }

                                @Override
                                public void onUserNotFound() {
                                    // Usuario nuevo, registrar
                                    dbHelper.registerUserWithGoogle(userId, name, email, photoUrl,
                                            new DatabaseHelper.OnOperationCompleteListener() {
                                                @Override
                                                public void onSuccess() {
                                                    saveUserSession(userId, name, email);
                                                    Toast.makeText(MainActivity.this,
                                                            "¡Bienvenido, " + name + "!",
                                                            Toast.LENGTH_SHORT).show();
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

    private void navigateToRegister() {
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}