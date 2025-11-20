package com.upc.confin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashMap;
import java.util.Map;

public class AgregarCategoriaActivity extends AppCompatActivity {

    private EditText etNombreCategoria;
    private Spinner spinnerTipo;
    private Button btnGuardar;
    private Button btnCancelar;

    private DatabaseHelper dbHelper;

    private Map<Integer, String> iconMap;
    private String iconoSeleccionado = "ic_restaurant";
    private FrameLayout iconoSeleccionadoFrame = null;

    private String colorSeleccionado = "#FF5722";
    private View colorSeleccionadoView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_categoria);

        initViews();

        dbHelper = DatabaseHelper.getInstance();

        setupIconMap();
        setupIconListeners();
        setupColorListeners();
        setupButtonListeners();
        setupSpinner();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        etNombreCategoria = findViewById(R.id.etNombreCategoria);
        spinnerTipo = findViewById(R.id.spinnerTipo);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnCancelar = findViewById(R.id.btnCancelar);
    }

    private void setupSpinner() {
        String[] tipos = {"Gasto", "Ingreso"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                tipos
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerTipo.setAdapter(adapter);
    }

    private void setupIconMap() {
        iconMap = new HashMap<>();
        iconMap.put(R.id.iconAlimentacion, "ic_alimentacion");
        iconMap.put(R.id.iconTransporte, "ic_transporte");
        iconMap.put(R.id.iconEntretenimiento, "ic_entretenimiento");
        iconMap.put(R.id.iconHogar, "ic_hogar");
        iconMap.put(R.id.iconCompras, "ic_compras");
        iconMap.put(R.id.iconFavorito, "ic_favorito");
        iconMap.put(R.id.iconAvion, "ic_avion");
        iconMap.put(R.id.iconDocumento, "ic_documento");
        iconMap.put(R.id.iconDeporte, "ic_deporte");
        iconMap.put(R.id.iconMaletin, "ic_maletin");
        iconMap.put(R.id.iconRegalo, "ic_regalo");
    }

    private void setupIconListeners() {
        for (Map.Entry<Integer, String> entry : iconMap.entrySet()) {
            FrameLayout iconFrame = findViewById(entry.getKey());
            iconFrame.setOnClickListener(v -> {
                if (iconoSeleccionadoFrame != null) {
                    iconoSeleccionadoFrame.setBackgroundResource(R.drawable.icon_selector_background);
                }

                iconoSeleccionado = entry.getValue();
                iconoSeleccionadoFrame = iconFrame;
                iconFrame.setBackgroundColor(Color.parseColor("#E0E0E0"));
            });
        }

        FrameLayout firstIcon = findViewById(R.id.iconAlimentacion);
        firstIcon.performClick();
    }

    private void setupColorListeners() {
        Map<Integer, String> colorMap = new HashMap<>();
        colorMap.put(R.id.colorNegro, "#000000");
        colorMap.put(R.id.colorGrisOscuro, "#4A4A4A");
        colorMap.put(R.id.colorGris, "#9E9E9E");
        colorMap.put(R.id.colorGrisClaro, "#CCCCCC");
        colorMap.put(R.id.colorBlanco, "#E8E8E8");

        for (Map.Entry<Integer, String> entry : colorMap.entrySet()) {
            View colorView = findViewById(entry.getKey());
            colorView.setOnClickListener(v -> {
                if (colorSeleccionadoView != null) {
                    colorSeleccionadoView.setScaleX(1.0f);
                    colorSeleccionadoView.setScaleY(1.0f);
                }

                colorSeleccionado = entry.getValue();
                colorSeleccionadoView = colorView;

                colorView.setScaleX(1.2f);
                colorView.setScaleY(1.2f);
            });
        }

        View firstColor = findViewById(R.id.colorNegro);
        firstColor.performClick();
        colorSeleccionado = "#FF5722";
    }

    private void setupButtonListeners() {
        btnGuardar.setOnClickListener(v -> guardarCategoria());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void guardarCategoria() {
        String nombre = etNombreCategoria.getText().toString().trim();
        if (nombre.isEmpty()) {
            Toast.makeText(this, "Ingrese un nombre para la categoría", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener tipoId según la selección
        String tipoId = spinnerTipo.getSelectedItemPosition() == 0
                ? DatabaseHelper.TIPO_GASTO_ID
                : DatabaseHelper.TIPO_INGRESO_ID;

        Category category = new Category(
                null,
                nombre,
                tipoId,
                iconoSeleccionado,
                colorSeleccionado
        );

        dbHelper.addCategory(category, new DatabaseHelper.OnOperationCompleteListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(AgregarCategoriaActivity.this,
                        "✅ Categoría creada",
                        Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(AgregarCategoriaActivity.this,
                        "❌ Error: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}