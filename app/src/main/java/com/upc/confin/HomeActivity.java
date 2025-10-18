package com.upc.confin; // Reemplaza con tu package name

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerViewTransactions;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Inicializar vistas
        recyclerViewTransactions = findViewById(R.id.recycler_view_transactions);

        // Configurar RecyclerView
        setupRecyclerView();

        // Cargar datos de ejemplo
        loadTransactionData();
    }

    private void setupRecyclerView() {
        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(this, transactionList);
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTransactions.setAdapter(transactionAdapter);
    }

    private void loadTransactionData() {
        // Aquí agregarías los datos reales, por ahora usamos datos de ejemplo
        transactionList.add(new Transaction(R.drawable.ic_shopping_cart, "Supermercado Central", "Alimentación • 15 Ene", 85.50));
        transactionList.add(new Transaction(R.drawable.ic_local_gas_station, "Gasolinera Shell", "Transporte • 14 Ene", 45.00));
        transactionList.add(new Transaction(R.drawable.ic_live_tv, "Netflix", "Entretenimiento • 13 Ene", 12.99));
        transactionList.add(new Transaction(R.drawable.ic_restaurant, "Restaurante Italiano", "Restaurantes • 12 Ene", 67.80));

        // Notificar al adaptador que los datos han cambiado
        transactionAdapter.notifyDataSetChanged();
    }
}