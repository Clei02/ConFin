package com.upc.confin;

import android.util.Log;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {

    private static final String TAG = "DatabaseHelper";
    private static DatabaseHelper instance;
    private final DatabaseReference database;

    private static String currentUserId = null;

    public static final String TIPO_GASTO_ID = "tipo_001";
    public static final String TIPO_INGRESO_ID = "tipo_002";

    // Interfaces para callbacks
    public interface OnCategoriesLoadedListener {
        void onCategoriesLoaded(List<Category> categories);
        void onError(String error);
    }

    public interface OnTransactionsLoadedListener {
        void onTransactionsLoaded(List<Transaction> transactions);
        void onError(String error);
    }

    public interface OnOperationCompleteListener {
        void onSuccess();
        void onError(String error);
    }

    public interface OnUserLoadedListener {
        void onUserLoaded(User user);
        void onUserNotFound();
        void onError(String error);
    }

    public interface OnLoginListener {
        void onLoginSuccess(String userId, String userName, String userEmail);
        void onLoginFailed(String error);
    }

    public interface OnEmailCheckListener {
        void onEmailExists();
        void onEmailAvailable();
        void onError(String error);
    }

    private DatabaseHelper() {
        database = FirebaseDatabase.getInstance("https://confindb-default-rtdb.firebaseio.com/")
                .getReference();
    }

    public static synchronized DatabaseHelper getInstance() {
        if (instance == null) {
            instance = new DatabaseHelper();
        }
        return instance;
    }

    public void setCurrentUserId(String userId) {
        currentUserId = userId;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    // ==================== USUARIOS ====================

    // Verificar si el email ya existe
    public void checkEmailExists(String email, OnEmailCheckListener listener) {
        database.child("usuarios")
                .orderByChild("email")
                .equalTo(email.toLowerCase())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            listener.onEmailExists();
                        } else {
                            listener.onEmailAvailable();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }

    // Registrar nuevo usuario con email
    public void registerUser(String nombre, String username, String email, String password,
                             OnOperationCompleteListener listener) {

        // Verificar que el email no exista
        checkEmailExists(email, new OnEmailCheckListener() {
            @Override
            public void onEmailExists() {
                listener.onError("Este email ya está registrado");
            }

            @Override
            public void onEmailAvailable() {
                // Crear usuario
                String userId = database.child("usuarios").push().getKey();

                if (userId == null) {
                    listener.onError("Error al generar ID de usuario");
                    return;
                }

                String hashedPassword = PasswordHelper.encryptPassword(password);

                User user = new User(userId, nombre, username, email.toLowerCase(), hashedPassword);

                database.child("usuarios").child(userId)
                        .setValue(user)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Usuario registrado exitosamente");
                            listener.onSuccess();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error registrando usuario: " + e.getMessage());
                            listener.onError(e.getMessage());
                        });
            }

            @Override
            public void onError(String error) {
                listener.onError(error);
            }
        });
    }

    // Registrar usuario con Google (sin contraseña)
    public void registerUserWithGoogle(String userId, String nombre, String email, String photoUrl,
                                       OnOperationCompleteListener listener) {

        User user = new User(userId, nombre, email, email.toLowerCase(), null, photoUrl);

        database.child("usuarios").child(userId)
                .setValue(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Usuario de Google registrado exitosamente");
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error registrando usuario de Google: " + e.getMessage());
                    listener.onError(e.getMessage());
                });
    }

    // Login de usuario con email y contraseña
    public void loginUser(String email, String password, OnLoginListener listener) {
        database.child("usuarios")
                .orderByChild("email")
                .equalTo(email.toLowerCase())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            listener.onLoginFailed("Email no registrado");
                            return;
                        }

                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            User user = userSnapshot.getValue(User.class);

                            if (user != null) {
                                // Verificar contraseña
                                if (user.getPassword() != null &&
                                        PasswordHelper.verifyPassword(password, user.getPassword())) {
                                    currentUserId = user.getId();
                                    listener.onLoginSuccess(user.getId(), user.getNombre(), user.getEmail());
                                    return;
                                } else {
                                    listener.onLoginFailed("Contraseña incorrecta");
                                    return;
                                }
                            }
                        }
                        listener.onLoginFailed("Error en los datos del usuario");
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Error en login: " + error.getMessage());
                        listener.onLoginFailed("Error de conexión: " + error.getMessage());
                    }
                });
    }

    // Obtener usuario por ID
    public void getUserById(String userId, OnUserLoadedListener listener) {
        database.child("usuarios").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            listener.onUserLoaded(user);
                        } else {
                            listener.onUserNotFound();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }

    // Actualizar datos del usuario
    public void updateUser(User user, OnOperationCompleteListener listener) {
        if (user.getId() == null) {
            listener.onError("ID de usuario no válido");
            return;
        }

        database.child("usuarios").child(user.getId())
                .setValue(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Usuario actualizado exitosamente");
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error actualizando usuario: " + e.getMessage());
                    listener.onError(e.getMessage());
                });
    }

    // ==================== TIPOS ====================

    public void initializeDefaultTypes(OnOperationCompleteListener listener) {
        Tipo tipoGasto = new Tipo(TIPO_GASTO_ID, "GASTO");
        Tipo tipoIngreso = new Tipo(TIPO_INGRESO_ID, "INGRESO");

        database.child("tipos").child(TIPO_GASTO_ID).setValue(tipoGasto);
        database.child("tipos").child(TIPO_INGRESO_ID).setValue(tipoIngreso)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // ==================== CATEGORÍAS (COMPARTIDAS) ====================

    public void loadCategories(OnCategoriesLoadedListener listener) {
        database.child("categorias")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Category> categories = new ArrayList<>();
                        for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                            Category category = categorySnapshot.getValue(Category.class);
                            if (category != null) {
                                categories.add(category);
                            }
                        }
                        listener.onCategoriesLoaded(categories);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Error loading categories: " + error.getMessage());
                        listener.onError(error.getMessage());
                    }
                });
    }

    public void loadCategoriesByTypeId(String tipoId, OnCategoriesLoadedListener listener) {
        database.child("categorias")
                .orderByChild("tipoId").equalTo(tipoId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Category> categories = new ArrayList<>();
                        for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                            Category category = categorySnapshot.getValue(Category.class);
                            if (category != null) {
                                categories.add(category);
                            }
                        }
                        listener.onCategoriesLoaded(categories);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Error loading categories by type: " + error.getMessage());
                        listener.onError(error.getMessage());
                    }
                });
    }

    public void addCategory(Category category, OnOperationCompleteListener listener) {
        String categoryId = database.child("categorias").push().getKey();

        if (categoryId == null) {
            listener.onError("Error al generar ID de categoría");
            return;
        }

        category.setId(categoryId);

        database.child("categorias").child(categoryId)
                .setValue(category)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Categoría agregada exitosamente");
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding category: " + e.getMessage());
                    listener.onError(e.getMessage());
                });
    }

    public void getCategoryById(String categoryId, OnCategoriesLoadedListener listener) {
        database.child("categorias").child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        Category category = snapshot.getValue(Category.class);
                        List<Category> list = new ArrayList<>();
                        if (category != null) {
                            list.add(category);
                        }
                        listener.onCategoriesLoaded(list);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }

    // ==================== TRANSACCIONES (POR USUARIO) ====================

    public void loadTransactions(OnTransactionsLoadedListener listener) {
        if (currentUserId == null) {
            listener.onError("Usuario no autenticado");
            return;
        }

        database.child("transacciones").child(currentUserId)
                .orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Transaction> transactions = new ArrayList<>();
                        for (DataSnapshot transactionSnapshot : snapshot.getChildren()) {
                            Transaction transaction = transactionSnapshot.getValue(Transaction.class);
                            if (transaction != null) {
                                transactions.add(0, transaction);
                            }
                        }
                        listener.onTransactionsLoaded(transactions);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Error loading transactions: " + error.getMessage());
                        listener.onError(error.getMessage());
                    }
                });
    }

    public void loadTransactionsByTypeId(String tipoId, OnTransactionsLoadedListener listener) {
        if (currentUserId == null) {
            listener.onError("Usuario no autenticado");
            return;
        }

        database.child("transacciones").child(currentUserId)
                .orderByChild("tipoId").equalTo(tipoId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Transaction> transactions = new ArrayList<>();
                        for (DataSnapshot transactionSnapshot : snapshot.getChildren()) {
                            Transaction transaction = transactionSnapshot.getValue(Transaction.class);
                            if (transaction != null) {
                                transactions.add(0, transaction);
                            }
                        }
                        listener.onTransactionsLoaded(transactions);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Error loading transactions by type: " + error.getMessage());
                        listener.onError(error.getMessage());
                    }
                });
    }

    public void addTransaction(Transaction transaction, OnOperationCompleteListener listener) {
        if (currentUserId == null) {
            listener.onError("Usuario no autenticado");
            return;
        }

        String transactionId = database.child("transacciones").child(currentUserId).push().getKey();

        if (transactionId == null) {
            listener.onError("Error al generar ID de transacción");
            return;
        }

        transaction.setId(transactionId);
        transaction.setTimestamp(System.currentTimeMillis());

        database.child("transacciones").child(currentUserId).child(transactionId)
                .setValue(transaction)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Transacción agregada exitosamente");
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding transaction: " + e.getMessage());
                    listener.onError(e.getMessage());
                });
    }

    // ==================== CATEGORÍAS POR DEFECTO ====================

    public void createDefaultCategories(OnOperationCompleteListener listener) {
        List<Category> defaultCategories = new ArrayList<>();

        defaultCategories.add(new Category("cat_001", "Alimentación", TIPO_GASTO_ID, "ic_alimentacion", "#FF5722"));
        defaultCategories.add(new Category("cat_002", "Transporte", TIPO_GASTO_ID, "ic_transporte", "#2196F3"));
        defaultCategories.add(new Category("cat_003", "Entretenimiento", TIPO_GASTO_ID, "ic_entretenimiento", "#9C27B0"));
        defaultCategories.add(new Category("cat_004", "Compras", TIPO_GASTO_ID, "ic_compras", "#FF9800"));
        defaultCategories.add(new Category("cat_005", "Hogar", TIPO_GASTO_ID, "ic_hogar", "#4CAF50"));
        defaultCategories.add(new Category("cat_006", "Salud", TIPO_GASTO_ID, "ic_favorito", "#F44336"));
        defaultCategories.add(new Category("cat_007", "Salario", TIPO_INGRESO_ID, "ic_maletin", "#4CAF50"));
        defaultCategories.add(new Category("cat_008", "Freelance", TIPO_INGRESO_ID, "ic_documento", "#00BCD4"));
        defaultCategories.add(new Category("cat_009", "Inversiones", TIPO_INGRESO_ID, "ic_avion", "#3F51B5"));

        DatabaseReference categoriesRef = database.child("categorias");

        for (Category category : defaultCategories) {
            categoriesRef.child(category.getId()).setValue(category);
        }

        listener.onSuccess();
        Log.d(TAG, "Categorías por defecto creadas");
    }
}