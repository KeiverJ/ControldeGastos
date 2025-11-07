package com.example.controldegastos.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.controldegastos.R;
import com.example.controldegastos.database.DatabaseHelper;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EstadisticasActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    // TextViews para mostrar estadísticas
    private TextView tvMesActual;
    private TextView tvTotalIngresosMes, tvTotalGastosMes, tvBalanceMes;
    private TextView tvIngresosMesAnterior, tvGastosMesAnterior;
    private TextView tvComparacionIngresos, tvComparacionGastos;
    private TextView tvCategoriaMasGastada, tvMontoCategoria;
    private TextView tvTotalTransacciones, tvDiasConGastos;
    private TextView tvPromedioDiario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Estadísticas");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Inicializar vistas
        inicializarVistas();

        // Inicializar base de datos
        dbHelper = new DatabaseHelper(this);

        // Cargar estadísticas
        cargarEstadisticas();
    }

    private void inicializarVistas() {
        tvMesActual = findViewById(R.id.tvMesActual);
        tvTotalIngresosMes = findViewById(R.id.tvTotalIngresosMes);
        tvTotalGastosMes = findViewById(R.id.tvTotalGastosMes);
        tvBalanceMes = findViewById(R.id.tvBalanceMes);
        tvIngresosMesAnterior = findViewById(R.id.tvIngresosMesAnterior);
        tvGastosMesAnterior = findViewById(R.id.tvGastosMesAnterior);
        tvComparacionIngresos = findViewById(R.id.tvComparacionIngresos);
        tvComparacionGastos = findViewById(R.id.tvComparacionGastos);
        tvCategoriaMasGastada = findViewById(R.id.tvCategoriaMasGastada);
        tvMontoCategoria = findViewById(R.id.tvMontoCategoria);
        tvTotalTransacciones = findViewById(R.id.tvTotalTransacciones);
        tvDiasConGastos = findViewById(R.id.tvDiasConGastos);
        tvPromedioDiario = findViewById(R.id.tvPromedioDiario);
    }

    private void cargarEstadisticas() {
        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

        // Mes actual
        SimpleDateFormat formatoMes = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
        String mesActual = formatoMes.format(Calendar.getInstance().getTime());
        mesActual = mesActual.substring(0, 1).toUpperCase() + mesActual.substring(1);
        tvMesActual.setText("📅 " + mesActual);

        // Totales del mes actual
        double ingresosMes = dbHelper.obtenerTotalMesActual("ingreso");
        double gastosMes = dbHelper.obtenerTotalMesActual("gasto");
        double balanceMes = ingresosMes - gastosMes;

        tvTotalIngresosMes.setText(formatoMoneda.format(ingresosMes));
        tvTotalGastosMes.setText(formatoMoneda.format(gastosMes));
        tvBalanceMes.setText(formatoMoneda.format(balanceMes));

        // Color del balance
        if (balanceMes >= 0) {
            tvBalanceMes.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvBalanceMes.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        // Totales del mes anterior
        double ingresosMesAnterior = dbHelper.obtenerTotalMesAnterior("ingreso");
        double gastosMesAnterior = dbHelper.obtenerTotalMesAnterior("gasto");

        tvIngresosMesAnterior.setText(formatoMoneda.format(ingresosMesAnterior));
        tvGastosMesAnterior.setText(formatoMoneda.format(gastosMesAnterior));

        // Comparación con mes anterior
        calcularComparacion(ingresosMes, ingresosMesAnterior, tvComparacionIngresos, true);
        calcularComparacion(gastosMes, gastosMesAnterior, tvComparacionGastos, false);

        // Categoría más gastada
        String[] categoriaMasGastada = dbHelper.obtenerCategoriaMasGastada();
        tvCategoriaMasGastada.setText(categoriaMasGastada[1] + " " + categoriaMasGastada[0]);
        tvMontoCategoria.setText(formatoMoneda.format(Double.parseDouble(categoriaMasGastada[2])));

        // Otras estadísticas
        int totalTransacciones = dbHelper.obtenerTotalTransaccionesMes();
        int diasConGastos = dbHelper.obtenerDiasConGastosMes();
        double promedioDiario = dbHelper.obtenerPromedioGastoDiario();

        tvTotalTransacciones.setText(String.valueOf(totalTransacciones));
        tvDiasConGastos.setText(diasConGastos + " días");
        tvPromedioDiario.setText(formatoMoneda.format(promedioDiario));
    }

    private void calcularComparacion(double actual, double anterior, TextView textView, boolean esIngreso) {
        if (anterior == 0) {
            textView.setText("Sin datos del mes anterior");
            textView.setTextColor(getResources().getColor(android.R.color.darker_gray));
            return;
        }

        double diferencia = actual - anterior;
        double porcentaje = (diferencia / anterior) * 100;

        String signo = diferencia >= 0 ? "+" : "";
        String emoji = "";
        int color;

        if (esIngreso) {
            // Para ingresos: más es mejor
            if (diferencia > 0) {
                emoji = "📈 ";
                color = android.R.color.holo_green_dark;
            } else {
                emoji = "📉 ";
                color = android.R.color.holo_red_dark;
            }
        } else {
            // Para gastos: menos es mejor
            if (diferencia > 0) {
                emoji = "⚠️ ";
                color = android.R.color.holo_orange_dark;
            } else {
                emoji = "✅ ";
                color = android.R.color.holo_green_dark;
            }
        }

        textView.setText(emoji + signo + String.format("%.1f", porcentaje) + "%");
        textView.setTextColor(getResources().getColor(color));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}