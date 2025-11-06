package com.example.controldegastos.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controldegastos.R;
import com.example.controldegastos.adapters.TransaccionAdapter;
import com.example.controldegastos.database.DatabaseHelper;
import com.example.controldegastos.models.Transaccion;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;

public class MainActivity extends AppCompatActivity implements TransaccionAdapter.OnTransaccionChangeListener {

    private RecyclerView recyclerView;
    private TransaccionAdapter adapter;
    private List<Transaccion> listaTransacciones;
    private DatabaseHelper dbHelper;

    private TextView tvBalance, tvTotalIngresos, tvTotalGastos;
    private TextView tvBienvenida, tvFechaHoy;
    private LinearLayout tvSinTransacciones;
    private FloatingActionButton fabAgregar;

    private static final String PREFS_NAME = "MisPreferencias";
    private static final String KEY_PRIMERA_VEZ = "primeraVez";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Control de Gastos");

        // Inicializar vistas
        inicializarVistas();

        // Inicializar base de datos
        dbHelper = new DatabaseHelper(this);

        // Mostrar bienvenida personalizada
        mostrarBienvenida();

        // Mostrar fecha actual
        mostrarFechaActual();

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listaTransacciones = new ArrayList<>();
        adapter = new TransaccionAdapter(this, listaTransacciones, this);
        recyclerView.setAdapter(adapter);

        // Cargar datos
        cargarTransacciones();
        actualizarResumen();

        // FAB para agregar transacción
        fabAgregar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AgregarTransaccionActivity.class);
            startActivity(intent);
        });
    }

    private void inicializarVistas() {
        recyclerView = findViewById(R.id.recyclerViewTransacciones);
        tvBalance = findViewById(R.id.tvBalance);
        tvTotalIngresos = findViewById(R.id.tvTotalIngresos);
        tvTotalGastos = findViewById(R.id.tvTotalGastos);
        tvSinTransacciones = findViewById(R.id.tvSinTransacciones);
        tvBienvenida = findViewById(R.id.tvBienvenida);
        tvFechaHoy = findViewById(R.id.tvFechaHoy);
        fabAgregar = findViewById(R.id.fabAgregar);
    }

    private void mostrarBienvenida() {
        // Obtener hora del día para saludo personalizado
        Calendar calendario = Calendar.getInstance();
        int hora = calendario.get(Calendar.HOUR_OF_DAY);

        String saludo;
        String emoji;

        if (hora >= 5 && hora < 12) {
            saludo = "Buenos días";
            emoji = "☀️";
        } else if (hora >= 12 && hora < 18) {
            saludo = "Buenas tardes";
            emoji = "🌤️";
        } else {
            saludo = "Buenas noches";
            emoji = "🌙";
        }

        tvBienvenida.setText(emoji + " " + saludo + ", Keiver");

        // Toast de bienvenida solo la primera vez
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean esPrimeraVez = prefs.getBoolean(KEY_PRIMERA_VEZ, true);

        if (esPrimeraVez) {
            Toast.makeText(this, "¡Bienvenido a Control de Gastos, Keiver! 🎉", Toast.LENGTH_LONG).show();

            // Guardar que ya no es la primera vez
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_PRIMERA_VEZ, false);
            editor.apply();
        }
    }

    private void mostrarFechaActual() {
        SimpleDateFormat formatoFecha = new SimpleDateFormat("EEEE, dd 'de' MMMM", new Locale("es", "ES"));
        String fechaActual = formatoFecha.format(Calendar.getInstance().getTime());

        // Capitalizar primera letra
        fechaActual = fechaActual.substring(0, 1).toUpperCase() + fechaActual.substring(1);

        tvFechaHoy.setText(fechaActual);
    }

    private void cargarTransacciones() {
        listaTransacciones.clear();
        Cursor cursor = dbHelper.obtenerTodasTransacciones();

        if (cursor.moveToFirst()) {
            tvSinTransacciones.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            do {
                int id = cursor.getInt(0);
                double monto = cursor.getDouble(1);
                String descripcion = cursor.getString(2);
                String fecha = cursor.getString(3);
                String tipo = cursor.getString(4);
                int idCategoria = cursor.getInt(5);

                Transaccion transaccion = new Transaccion(id, monto, descripcion, fecha, idCategoria, tipo);

                // Obtener información de la categoría
                String[] categoria = dbHelper.obtenerCategoriaPorId(idCategoria);
                if (categoria != null) {
                    transaccion.setNombreCategoria(categoria[1]); // nombre
                    transaccion.setColorCategoria(categoria[2]);  // color
                }

                listaTransacciones.add(transaccion);
            } while (cursor.moveToNext());
        } else {
            tvSinTransacciones.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    private void actualizarResumen() {
        double balance = dbHelper.calcularBalance();
        double totalIngresos = dbHelper.calcularTotalPorTipo("ingreso");
        double totalGastos = dbHelper.calcularTotalPorTipo("gasto");

        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

        tvBalance.setText(formatoMoneda.format(balance));
        tvTotalIngresos.setText(formatoMoneda.format(totalIngresos));
        tvTotalGastos.setText(formatoMoneda.format(totalGastos));

        // Color del balance
        if (balance >= 0) {
            tvBalance.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvBalance.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarTransacciones();
        actualizarResumen();
        mostrarBienvenida(); // Actualizar saludo si cambia la hora
    }

    @Override
    public void onTransaccionEliminada() {
        actualizarResumen();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_filtrar) {
            mostrarDialogoFiltros();
            return true;
        } else if (id == R.id.menu_categorias) {
            Toast.makeText(this, "📂 Gestionar categorías (próximamente)", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_acerca) {
            mostrarDialogoAcercaDe();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void mostrarDialogoFiltros() {
        String[] opciones = {"Todas", "Solo Ingresos", "Solo Gastos", "Por Categoría", "Por Fecha"};

        new AlertDialog.Builder(this)
                .setTitle("🔍 Filtrar Transacciones")
                .setItems(opciones, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            cargarTransacciones();
                            Toast.makeText(this, "Mostrando todas las transacciones", Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            filtrarPorTipo("ingreso");
                            break;
                        case 2:
                            filtrarPorTipo("gasto");
                            break;
                        case 3:
                            Toast.makeText(this, "Filtro por categoría (próximamente)", Toast.LENGTH_SHORT).show();
                            break;
                        case 4:
                            Toast.makeText(this, "Filtro por fecha (próximamente)", Toast.LENGTH_SHORT).show();
                            break;
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void filtrarPorTipo(String tipo) {
        listaTransacciones.clear();
        Cursor cursor = dbHelper.obtenerTodasTransacciones();

        if (cursor.moveToFirst()) {
            do {
                String tipoTransaccion = cursor.getString(4);
                if (tipoTransaccion.equals(tipo)) {
                    int id = cursor.getInt(0);
                    double monto = cursor.getDouble(1);
                    String descripcion = cursor.getString(2);
                    String fecha = cursor.getString(3);
                    int idCategoria = cursor.getInt(5);

                    Transaccion transaccion = new Transaccion(id, monto, descripcion, fecha, idCategoria, tipo);

                    String[] categoria = dbHelper.obtenerCategoriaPorId(idCategoria);
                    if (categoria != null) {
                        transaccion.setNombreCategoria(categoria[1]);
                        transaccion.setColorCategoria(categoria[2]);
                    }

                    listaTransacciones.add(transaccion);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        adapter.notifyDataSetChanged();

        String mensaje = tipo.equals("ingreso") ? "↗️ Mostrando solo ingresos" : "↘️ Mostrando solo gastos";
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    private void mostrarDialogoAcercaDe() {
        new AlertDialog.Builder(this)
                .setTitle("ℹ️ Acerca de")
                .setMessage("💙 Control de Gastos v1.0\n\n" +
                        "Creado con ❤️ por Keiver\n\n" +
                        "Una aplicación para gestionar tus finanzas personales de forma sencilla y elegante.\n\n" +
                        "© 2024 - Todos los derechos reservados")
                .setPositiveButton("Cerrar", null)
                .show();
    }
}