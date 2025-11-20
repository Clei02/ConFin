package com.upc.confin;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AgregarTransaccionActivity extends AppCompatActivity {

    private Spinner spinnerTipo;
    private Spinner spinnerCategoria;
    private EditText etDescripcion;
    private EditText etMonto;
    private EditText etFecha;
    private Button btnGuardar;
    private Button btnCancelar;

    private DatabaseHelper dbHelper;
    private List<Category> categoriasList;
    private String tipoSeleccionado = "GASTO";
    private long fechaSeleccionada;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_transaccion);

        // Verificar sesión
        SharedPreferences prefs = getSharedPreferences("ConFinPrefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", null);

        if (userId == null) {
            finish();
            return;
        }

        initViews();

        dbHelper = DatabaseHelper.getInstance();
        dbHelper.setCurrentUserId(userId);
        categoriasList = new ArrayList<>();

        calendar = Calendar.getInstance();
        fechaSeleccionada = calendar.getTimeInMillis();
        updateDateField();

        setupSpinners();
        setupDatePicker();
        setupButtons();
    }

    private void initViews() {
        spinnerTipo = findViewById(R.id.spinnerTipo);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        etDescripcion = findViewById(R.id.etDescripcion);
        etMonto = findViewById(R.id.etMonto);
        etFecha = findViewById(R.id.etFecha);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnCancelar = findViewById(R.id.btnCancelar);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupSpinners() {
        String[] tipos = {"Gasto", "Ingreso"};
        ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                tipos
        );
        adapterTipo.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerTipo.setAdapter(adapterTipo);

        spinnerTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tipoSeleccionado = position == 0 ? "GASTO" : "INGRESO";
                loadCategoriesByType(tipoSeleccionado);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        loadCategoriesByType("GASTO");
    }

    private void loadCategoriesByType(String tipo) {
        String tipoId = tipo.equals("GASTO")
                ? DatabaseHelper.TIPO_GASTO_ID
                : DatabaseHelper.TIPO_INGRESO_ID;

        dbHelper.loadCategoriesByTypeId(tipoId, new DatabaseHelper.OnCategoriesLoadedListener() {
            @Override
            public void onCategoriesLoaded(List<Category> categories) {
                categoriasList = categories;
                updateCategorySpinner();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(AgregarTransaccionActivity.this,
                        "Error cargando categorías: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCategorySpinner() {
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("Seleccione una categoría");

        for (Category category : categoriasList) {
            categoryNames.add(category.getNombre());
        }

        ArrayAdapter<String> adapterCategoria = new ArrayAdapter<String>(
                this,
                R.layout.spinner_item,
                categoryNames
        ) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                android.widget.TextView textView = (android.widget.TextView) view;
                if (position == 0) {
                    textView.setTextColor(getResources().getColor(android.R.color.darker_gray));
                } else {
                    textView.setTextColor(getResources().getColor(android.R.color.black));
                }
                return view;
            }
        };

        adapterCategoria.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterCategoria);

        spinnerCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view != null && position == 0) {
                    ((android.widget.TextView) view).setTextColor(
                            getResources().getColor(android.R.color.darker_gray));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupDatePicker() {
        etFecha.setFocusable(false);
        etFecha.setClickable(true);

        etFecha.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    AgregarTransaccionActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        fechaSeleccionada = calendar.getTimeInMillis();
                        updateDateField();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
    }

    private void updateDateField() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "CO"));
        etFecha.setText(sdf.format(calendar.getTime()));
    }

    private void setupButtons() {
        btnGuardar.setOnClickListener(v -> guardarTransaccion());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void guardarTransaccion() {
        String descripcion = etDescripcion.getText().toString().trim();
        String montoStr = etMonto.getText().toString().trim();
        int categoriaPosition = spinnerCategoria.getSelectedItemPosition();

        if (descripcion.isEmpty()) {
            Toast.makeText(this, "Ingrese una descripción", Toast.LENGTH_SHORT).show();
            return;
        }

        if (montoStr.isEmpty()) {
            Toast.makeText(this, "Ingrese un monto", Toast.LENGTH_SHORT).show();
            return;
        }

        if (categoriaPosition == 0) {
            Toast.makeText(this, "Seleccione una categoría", Toast.LENGTH_SHORT).show();
            return;
        }

        double monto;
        try {
            monto = Double.parseDouble(montoStr);
            if (monto <= 0) {
                Toast.makeText(this, "El monto debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Monto inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        Category categoriaSeleccionada = categoriasList.get(categoriaPosition - 1);

        String tipoId = tipoSeleccionado.equals("GASTO")
                ? DatabaseHelper.TIPO_GASTO_ID
                : DatabaseHelper.TIPO_INGRESO_ID;

        Transaction transaction = new Transaction(
                null,
                tipoId,
                descripcion,
                monto,
                categoriaSeleccionada.getId(),
                fechaSeleccionada
        );

        dbHelper.addTransaction(transaction, new DatabaseHelper.OnOperationCompleteListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(AgregarTransaccionActivity.this,
                        "✅ Transacción guardada",
                        Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(AgregarTransaccionActivity.this,
                        "❌ Error: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}