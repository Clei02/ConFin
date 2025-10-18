package com.upc.confin;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class CategoriasActivity extends AppCompatActivity {

    private LinearLayout contentAlimentacion;
    private ImageView iconExpandAlimentacion;
    private boolean isAlimentacionExpanded = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorias);

        // Referencias a las vistas
        LinearLayout headerAlimentacion = findViewById(R.id.headerAlimentacion);
        contentAlimentacion = findViewById(R.id.contentAlimentacion);
        iconExpandAlimentacion = findViewById(R.id.iconExpandAlimentacion);

        // Listener para expandir/colapsar Alimentación
        headerAlimentacion.setOnClickListener(v -> toggleAlimentacion());
    }

    private void toggleAlimentacion() {
        if (isAlimentacionExpanded) {
            // Colapsar
            contentAlimentacion.setVisibility(View.GONE);
            iconExpandAlimentacion.setRotation(0);
        } else {
            // Expandir
            contentAlimentacion.setVisibility(View.VISIBLE);
            iconExpandAlimentacion.setRotation(180);
        }
        isAlimentacionExpanded = !isAlimentacionExpanded;
    }
}