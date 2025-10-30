package com.upc.confin;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;

public class AgregarTransaccionActivity extends AppCompatActivity {

    private Spinner spinnerTipo;
    private Spinner spinnerCategoria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_transaccion);

        spinnerTipo = findViewById(R.id.spinnerTipo);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);

        setupSpinners();
    }

    private void setupSpinners() {
        // Configurar Spinner Tipo
        String[] tipos = {"Gasto", "Ingreso"};
        ArrayAdapter<String> adapterTipo = new ArrayAdapter<String>(
                this,
                R.layout.spinner_item,
                tipos
        ) {
            @Override
            public boolean isEnabled(int position) {
                return true;
            }
        };
        adapterTipo.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerTipo.setAdapter(adapterTipo);

        // Configurar Spinner Categoría con placeholder
        String[] categorias = {
                "Seleccione una categoría",
                "Alimentación",
                "Transporte",
                "Entretenimiento",
                "Salud",
                "Educación",
                "Servicios",
                "Otros"
        };

        ArrayAdapter<String> adapterCategoria = new ArrayAdapter<String>(
                this,
                R.layout.spinner_item,
                categorias
        ) {
            @Override
            public boolean isEnabled(int position) {
                // Deshabilitar el primer item (placeholder)
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                android.widget.TextView textView = (android.widget.TextView) view;
                if (position == 0) {
                    // Hacer el placeholder gris
                    textView.setTextColor(getResources().getColor(android.R.color.darker_gray));
                } else {
                    textView.setTextColor(getResources().getColor(android.R.color.black));
                }
                return view;
            }
        };
        adapterCategoria.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterCategoria);

        // Listener para validar que no se quede en placeholder
        spinnerCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // Está en placeholder
                    ((android.widget.TextView) view).setTextColor(getResources().getColor(android.R.color.darker_gray));
                } else {
                    ((android.widget.TextView) view).setTextColor(getResources().getColor(android.R.color.black));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}