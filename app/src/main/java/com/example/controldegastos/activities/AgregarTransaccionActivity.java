package com.example.controldegastos.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.controldegastos.R;
import com.example.controldegastos.database.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AgregarTransaccionActivity extends AppCompatActivity {

    private EditText etMonto, etDescripcion, etFecha;
    private RadioGroup rgTipo;
    private RadioButton rbIngreso, rbGasto;
    private Spinner spinnerCategorias;
    private Button btnGuardar, btnCancelar;

    private DatabaseHelper dbHelper;
    private Calendar calendario;
    private long fechaSeleccionada;

    private List<String[]> listaCategorias;
    private String tipoSeleccionado = "gasto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_transaccion);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Nueva Transacción");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Inicializar vistas
        inicializarVistas();

        // Inicializar base de datos
        dbHelper = new DatabaseHelper(this);
        calendario = Calendar.getInstance();
        fechaSeleccionada = calendario.getTimeInMillis();

        // Configurar fecha actual
        actualizarTextoFecha();

        // Cargar categorías de gastos por defecto
        cargarCategorias("gasto");

        // Listener para cambio de tipo
        rgTipo.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbIngreso) {
                tipoSeleccionado = "ingreso";
                cargarCategorias("ingreso");
            } else {
                tipoSeleccionado = "gasto";
                cargarCategorias("gasto");
            }
        });

        // Selector de fecha
        etFecha.setOnClickListener(v -> mostrarDatePicker());

        // Botón Guardar
        btnGuardar.setOnClickListener(v -> guardarTransaccion());

        // Botón Cancelar
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void inicializarVistas() {
        etMonto = findViewById(R.id.etMonto);
        etDescripcion = findViewById(R.id.etDescripcion);
        etFecha = findViewById(R.id.etFecha);
        rgTipo = findViewById(R.id.rgTipo);
        rbIngreso = findViewById(R.id.rbIngreso);
        rbGasto = findViewById(R.id.rbGasto);
        spinnerCategorias = findViewById(R.id.spinnerCategorias);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnCancelar = findViewById(R.id.btnCancelar);
    }

    private void cargarCategorias(String tipo) {
        listaCategorias = dbHelper.obtenerCategoriasPorTipo(tipo);

        List<String> nombresCategorias = new ArrayList<>();
        for (String[] categoria : listaCategorias) {
            nombresCategorias.add(categoria[3] + " " + categoria[1]); // icono + nombre
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                nombresCategorias
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategorias.setAdapter(adapter);
    }

    private void mostrarDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendario.set(Calendar.YEAR, year);
                    calendario.set(Calendar.MONTH, month);
                    calendario.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    fechaSeleccionada = calendario.getTimeInMillis();
                    actualizarTextoFecha();
                },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void actualizarTextoFecha() {
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "ES"));
        etFecha.setText(formato.format(calendario.getTime()));
    }

    private void guardarTransaccion() {
        // Validar monto
        String montoStr = etMonto.getText().toString().trim();
        if (montoStr.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa un monto", Toast.LENGTH_SHORT).show();
            etMonto.requestFocus();
            return;
        }

        double monto = Double.parseDouble(montoStr);
        if (monto <= 0) {
            Toast.makeText(this, "El monto debe ser mayor a 0", Toast.LENGTH_SHORT).show();
            etMonto.requestFocus();
            return;
        }

        // Validar descripción
        String descripcion = etDescripcion.getText().toString().trim();
        if (descripcion.isEmpty()) {
            descripcion = tipoSeleccionado.equals("ingreso") ? "Ingreso" : "Gasto";
        }

        // Obtener categoría seleccionada
        int posicionCategoria = spinnerCategorias.getSelectedItemPosition();
        if (posicionCategoria < 0 || listaCategorias.isEmpty()) {
            Toast.makeText(this, "Por favor selecciona una categoría", Toast.LENGTH_SHORT).show();
            return;
        }

        int idCategoria = Integer.parseInt(listaCategorias.get(posicionCategoria)[0]);

        // Insertar en la base de datos
        long resultado = dbHelper.insertarTransaccion(
                monto,
                descripcion,
                fechaSeleccionada,
                tipoSeleccionado,
                idCategoria
        );

        if (resultado != -1) {
            Toast.makeText(this, "Transacción guardada exitosamente", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error al guardar la transacción", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}