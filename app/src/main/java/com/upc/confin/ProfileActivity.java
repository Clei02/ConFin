package com.upc.confin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivProfilePhoto;
    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private TextInputEditText etProfileName;
    private TextInputEditText etProfileUsername;
    private TextInputEditText etProfileEmail;
    private MaterialButton btnSaveChanges;
    private FloatingActionButton fabEditPhoto;
    private ImageButton btnBack;

    private TextView tvAccountType;
    private TextView tvAccountTypeDesc;
    private ImageView ivAccountTypeIcon;

    private DatabaseHelper dbHelper;
    private String userId;
    private User currentUser;
    private boolean isGoogleAccount = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        dbHelper = DatabaseHelper.getInstance();

        // Obtener datos de sesión
        SharedPreferences prefs = getSharedPreferences("ConFinPrefs", MODE_PRIVATE);
        userId = prefs.getString("userId", null);

        if (userId == null) {
            finish();
            return;
        }

        loadUserData();
        setupListeners();
    }

    private void initViews() {
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        etProfileName = findViewById(R.id.etProfileName);
        etProfileUsername = findViewById(R.id.etProfileUsername);
        etProfileEmail = findViewById(R.id.etProfileEmail);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        fabEditPhoto = findViewById(R.id.fabEditPhoto);
        btnBack = findViewById(R.id.btnBack);

        tvAccountType = findViewById(R.id.tvAccountType);
        tvAccountTypeDesc = findViewById(R.id.tvAccountTypeDesc);
        ivAccountTypeIcon = findViewById(R.id.ivAccountTypeIcon);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSaveChanges.setOnClickListener(v -> saveChanges());
        fabEditPhoto.setOnClickListener(v -> {
            Toast.makeText(this,
                    "Próximamente: Cambiar foto de perfil",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void loadUserData() {
        dbHelper.getUserById(userId, new DatabaseHelper.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                currentUser = user;
                displayUserData(user);
            }

            @Override
            public void onUserNotFound() {
                Toast.makeText(ProfileActivity.this,
                        "Error: Usuario no encontrado",
                        Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ProfileActivity.this,
                        "Error: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayUserData(User user) {
        // Mostrar nombre y email en el header
        tvProfileName.setText(user.getNombre());
        tvProfileEmail.setText(user.getEmail());

        // Llenar campos editables
        etProfileName.setText(user.getNombre());
        etProfileUsername.setText(user.getUsername());
        etProfileEmail.setText(user.getEmail());

        // Verificar si es cuenta de Google
        isGoogleAccount = (user.getPassword() == null || user.getPassword().isEmpty());

        if (isGoogleAccount) {
            // Es cuenta de Google
            tvAccountType.setText("Cuenta con Google");
            tvAccountTypeDesc.setText("Registrado con Google Sign-In");
            ivAccountTypeIcon.setImageResource(R.drawable.ic_google);

            // Cargar foto de Google si existe
            if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                loadProfilePhoto(user.getPhotoUrl());
            } else {
                ivProfilePhoto.setImageResource(R.drawable.ic_person);
            }
        } else {
            // Es cuenta con email
            tvAccountType.setText("Cuenta con email");
            tvAccountTypeDesc.setText("Registrado con correo electrónico");
            ivAccountTypeIcon.setImageResource(R.drawable.ic_email);
            ivProfilePhoto.setImageResource(R.drawable.ic_person);
        }
    }

    private void loadProfilePhoto(String photoUrl) {
        Glide.with(this)
                .load(photoUrl)
                .transform(new CircleCrop())
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(ivProfilePhoto);
    }

    private void saveChanges() {
        String newName = etProfileName.getText().toString().trim();
        String newUsername = etProfileUsername.getText().toString().trim();

        // Validaciones
        if (newName.isEmpty()) {
            Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newName.length() < 3) {
            Toast.makeText(this, "El nombre debe tener al menos 3 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newUsername.isEmpty()) {
            Toast.makeText(this, "El usuario no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newUsername.length() < 4) {
            Toast.makeText(this, "El usuario debe tener al menos 4 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar si hubo cambios
        if (newName.equals(currentUser.getNombre()) &&
                newUsername.equals(currentUser.getUsername())) {
            Toast.makeText(this, "No hay cambios para guardar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Deshabilitar botón mientras se guarda
        btnSaveChanges.setEnabled(false);
        btnSaveChanges.setText("Guardando...");

        // Actualizar usuario
        currentUser.setNombre(newName);
        currentUser.setUsername(newUsername);

        dbHelper.updateUser(currentUser, new DatabaseHelper.OnOperationCompleteListener() {
            @Override
            public void onSuccess() {
                // Actualizar SharedPreferences
                SharedPreferences prefs = getSharedPreferences("ConFinPrefs", MODE_PRIVATE);
                prefs.edit()
                        .putString("userName", newName)
                        .apply();

                Toast.makeText(ProfileActivity.this,
                        "✅ Perfil actualizado",
                        Toast.LENGTH_SHORT).show();

                // Actualizar UI
                tvProfileName.setText(newName);

                btnSaveChanges.setEnabled(true);
                btnSaveChanges.setText("Guardar cambios");

                // 👇 ESTO ES LO NUEVO - Notificar a HomeActivity
                setResult(RESULT_OK);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ProfileActivity.this,
                        "❌ Error: " + error,
                        Toast.LENGTH_SHORT).show();

                btnSaveChanges.setEnabled(true);
                btnSaveChanges.setText("Guardar cambios");
            }
        });
    }
}