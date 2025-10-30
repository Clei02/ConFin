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

    // Por ahora usamos un userId fijo (luego lo cambiaremos con autenticación real)
    private static final String USER_ID = "user_demo_001";

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

    // Constructor privado (Singleton)
    private DatabaseHelper() {
        database = FirebaseDatabase.getInstance("https://confindb-default-rtdb.firebaseio.com/")
                .getReference();
    }

    // Obtener instancia única
    public static synchronized DatabaseHelper getInstance() {
        if (instance == null) {
            instance = new DatabaseHelper();
        }
        return instance;
    }

    // ==================== CATEGORÍAS ====================

    // Cargar todas las categorías
    public void loadCategories(OnCategoriesLoadedListener listener) {
        database.child("usuarios").child(USER_ID).child("categorias")
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

    // Cargar categorías por tipo (INGRESO o GASTO)
    public void loadCategoriesByType(String tipo, OnCategoriesLoadedListener listener) {
        database.child("usuarios").child(USER_ID).child("categorias")
                .orderByChild("tipo").equalTo(tipo)
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

    // Agregar nueva categoría
    public void addCategory(Category category, OnOperationCompleteListener listener) {
        String categoryId = database.child("usuarios").child(USER_ID)
                .child("categorias").push().getKey();

        if (categoryId == null) {
            listener.onError("Error al generar ID de categoría");
            return;
        }

        category.setId(categoryId);

        database.child("usuarios").child(USER_ID).child("categorias").child(categoryId)
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

    // Obtener categoría por ID
    public void getCategoryById(String categoryId, OnCategoriesLoadedListener listener) {
        database.child("usuarios").child(USER_ID).child("categorias").child(categoryId)
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

    // ==================== TRANSACCIONES ====================

    // Cargar todas las transacciones
    public void loadTransactions(OnTransactionsLoadedListener listener) {
        database.child("usuarios").child(USER_ID).child("transacciones")
                .orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Transaction> transactions = new ArrayList<>();
                        for (DataSnapshot transactionSnapshot : snapshot.getChildren()) {
                            Transaction transaction = transactionSnapshot.getValue(Transaction.class);
                            if (transaction != null) {
                                transactions.add(0, transaction); // Agregar al inicio (más reciente primero)
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

    // Cargar transacciones por tipo (INGRESO o GASTO)
    public void loadTransactionsByType(String tipo, OnTransactionsLoadedListener listener) {
        database.child("usuarios").child(USER_ID).child("transacciones")
                .orderByChild("tipo").equalTo(tipo)
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

    // Agregar nueva transacción
    public void addTransaction(Transaction transaction, OnOperationCompleteListener listener) {
        String transactionId = database.child("usuarios").child(USER_ID)
                .child("transacciones").push().getKey();

        if (transactionId == null) {
            listener.onError("Error al generar ID de transacción");
            return;
        }

        transaction.setId(transactionId);
        transaction.setTimestamp(System.currentTimeMillis());

        database.child("usuarios").child(USER_ID).child("transacciones").child(transactionId)
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

    // Crear categorías por defecto para nuevos usuarios
    public void createDefaultCategories(OnOperationCompleteListener listener) {
        List<Category> defaultCategories = new ArrayList<>();

        // Categorías de GASTO
        defaultCategories.add(new Category("cat_001", "Alimentación", "GASTO", "ic_restaurant", "#FF5722"));
        defaultCategories.add(new Category("cat_002", "Transporte", "GASTO", "ic_transporte", "#2196F3"));
        defaultCategories.add(new Category("cat_003", "Entretenimiento", "GASTO", "ic_entretenimiento", "#9C27B0"));
        defaultCategories.add(new Category("cat_004", "Compras", "GASTO", "ic_compras", "#FF9800"));
        defaultCategories.add(new Category("cat_005", "Hogar", "GASTO", "ic_hogar", "#4CAF50"));
        defaultCategories.add(new Category("cat_006", "Salud", "GASTO", "ic_favorito", "#F44336"));

        // Categorías de INGRESO
        defaultCategories.add(new Category("cat_007", "Salario", "INGRESO", "ic_maletin", "#4CAF50"));
        defaultCategories.add(new Category("cat_008", "Freelance", "INGRESO", "ic_documento", "#00BCD4"));
        defaultCategories.add(new Category("cat_009", "Inversiones", "INGRESO", "ic_chart", "#3F51B5"));

        // Guardar todas en Firebase
        DatabaseReference categoriesRef = database.child("usuarios").child(USER_ID).child("categorias");

        for (Category category : defaultCategories) {
            categoriesRef.child(category.getId()).setValue(category);
        }

        listener.onSuccess();
        Log.d(TAG, "Categorías por defecto creadas");
    }
}