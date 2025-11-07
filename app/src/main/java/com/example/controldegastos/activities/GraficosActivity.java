package com.example.controldegastos.activities;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.controldegastos.R;
import com.example.controldegastos.database.DatabaseHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GraficosActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private PieChart pieChart;
    private BarChart barChart;
    private LineChart lineChart;
    private TextView tvSinDatos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graficos);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("📊 Gráficos");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Inicializar vistas
        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);
        lineChart = findViewById(R.id.lineChart);
        tvSinDatos = findViewById(R.id.tvSinDatos);

        // Inicializar base de datos
        dbHelper = new DatabaseHelper(this);

        // Cargar gráficos
        cargarGraficos();
    }

    private void cargarGraficos() {
        // Verificar si hay datos
        if (dbHelper.obtenerTotalTransaccionesMes() == 0) {
            tvSinDatos.setVisibility(View.VISIBLE);
            pieChart.setVisibility(View.GONE);
            barChart.setVisibility(View.GONE);
            lineChart.setVisibility(View.GONE);
            return;
        }

        tvSinDatos.setVisibility(View.GONE);
        pieChart.setVisibility(View.VISIBLE);
        barChart.setVisibility(View.VISIBLE);
        lineChart.setVisibility(View.VISIBLE);

        configurarGraficoPastel();
        configurarGraficoBarras();
        configurarGraficoLineas();
    }

    // ============ GRÁFICO DE PASTEL (Gastos por Categoría) ============
    private void configurarGraficoPastel() {
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colores = new ArrayList<>();

        Cursor cursor = dbHelper.obtenerGastosPorCategoriaMes();
        if (cursor.moveToFirst()) {
            do {
                String nombreCategoria = cursor.getString(0);
                String colorHex = cursor.getString(1);
                String icono = cursor.getString(2);
                float total = cursor.getFloat(3);

                entries.add(new PieEntry(total, icono + " " + nombreCategoria));

                // Convertir color hex a int
                try {
                    colores.add(Color.parseColor(colorHex));
                } catch (Exception e) {
                    colores.add(Color.GRAY);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (entries.isEmpty()) {
            pieChart.setVisibility(View.GONE);
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colores);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setSliceSpace(2f);

        // Formato de valores (mostrar como moneda)
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                NumberFormat formato = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
                return formato.format(value);
            }
        });

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        // Configuraciones visuales
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText("Gastos por\nCategoría");
        pieChart.setCenterTextSize(14f);
        pieChart.setRotationAngle(0);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);

        // Leyenda
        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(10f);

        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    // ============ GRÁFICO DE BARRAS (Ingresos vs Gastos) ============
    private void configurarGraficoBarras() {
        List<BarEntry> ingresosEntries = new ArrayList<>();
        List<BarEntry> gastosEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        // Mapa para organizar datos por mes
        Map<String, Float> ingresosMap = new HashMap<>();
        Map<String, Float> gastosMap = new HashMap<>();
        List<String> meses = new ArrayList<>();

        Cursor cursor = dbHelper.obtenerIngresosGastosPorMes(6); // Últimos 6 meses
        if (cursor.moveToFirst()) {
            do {
                String mes = cursor.getString(0); // Formato: YYYY-MM
                String tipo = cursor.getString(1);
                float total = cursor.getFloat(2);

                if (!meses.contains(mes)) {
                    meses.add(mes);
                }

                if (tipo.equals("ingreso")) {
                    ingresosMap.put(mes, total);
                } else {
                    gastosMap.put(mes, total);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (meses.isEmpty()) {
            barChart.setVisibility(View.GONE);
            return;
        }

        // Crear entradas para el gráfico
        for (int i = 0; i < meses.size(); i++) {
            String mes = meses.get(i);
            float ingresos = ingresosMap.getOrDefault(mes, 0f);
            float gastos = gastosMap.getOrDefault(mes, 0f);

            ingresosEntries.add(new BarEntry(i, ingresos));
            gastosEntries.add(new BarEntry(i, gastos));

            // Formatear etiqueta del mes
            try {
                String[] partes = mes.split("-");
                int mesNum = Integer.parseInt(partes[1]);
                String[] nombresMeses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun",
                        "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
                labels.add(nombresMeses[mesNum - 1]);
            } catch (Exception e) {
                labels.add(mes);
            }
        }

        BarDataSet dataSetIngresos = new BarDataSet(ingresosEntries, "Ingresos");
        dataSetIngresos.setColor(Color.parseColor("#4CAF50"));
        dataSetIngresos.setValueTextSize(10f);

        BarDataSet dataSetGastos = new BarDataSet(gastosEntries, "Gastos");
        dataSetGastos.setColor(Color.parseColor("#F44336"));
        dataSetGastos.setValueTextSize(10f);

        // Formato de valores
        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value == 0) return "";
                return NumberFormat.getCurrencyInstance(new Locale("es", "CO")).format(value);
            }
        };
        dataSetIngresos.setValueFormatter(formatter);
        dataSetGastos.setValueFormatter(formatter);

        BarData data = new BarData(dataSetIngresos, dataSetGastos);
        data.setBarWidth(0.35f);

        barChart.setData(data);
        barChart.groupBars(0f, 0.3f, 0.02f);

        // Configuraciones
        Description desc = new Description();
        desc.setText("Ingresos vs Gastos (Últimos 6 meses)");
        desc.setTextSize(12f);
        barChart.setDescription(desc);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setDrawGridLines(false);

        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisRight().setEnabled(false);

        Legend legend = barChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(true);

        barChart.animateY(1000);
        barChart.invalidate();
    }

    // ============ GRÁFICO DE LÍNEAS (Evolución de Gastos) ============
    private void configurarGraficoLineas() {
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        Cursor cursor = dbHelper.obtenerGastosDiarios(30); // Últimos 30 días
        if (cursor.moveToFirst()) {
            int index = 0;
            do {
                String fecha = cursor.getString(0);
                float gastos = cursor.getFloat(1);

                entries.add(new Entry(index, gastos));

                // Formatear fecha para etiqueta
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
                    labels.add(outputFormat.format(inputFormat.parse(fecha)));
                } catch (Exception e) {
                    labels.add(fecha);
                }

                index++;
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (entries.isEmpty()) {
            lineChart.setVisibility(View.GONE);
            return;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Gastos Diarios");
        dataSet.setColor(Color.parseColor("#E74C3C"));
        dataSet.setCircleColor(Color.parseColor("#E74C3C"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(9f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#FFCDD2"));
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // Formato de valores
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value == 0) return "";
                return NumberFormat.getCurrencyInstance(new Locale("es", "CO")).format(value);
            }
        });

        LineData data = new LineData(dataSet);
        lineChart.setData(data);

        // Configuraciones
        Description desc = new Description();
        desc.setText("Evolución de Gastos (Últimos 30 días)");
        desc.setTextSize(12f);
        lineChart.setDescription(desc);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45f);
        xAxis.setDrawGridLines(false);

        lineChart.getAxisLeft().setAxisMinimum(0f);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getLegend().setEnabled(false);

        lineChart.animateX(1000);
        lineChart.invalidate();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}