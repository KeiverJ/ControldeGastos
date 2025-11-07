package com.example.controldegastos.activities;

import android.app.AlertDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controldegastos.R;
import com.example.controldegastos.adapters.CategoriaAdapter;
import com.example.controldegastos.database.DatabaseHelper;
import com.example.controldegastos.models.Categoria;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class CategoriasActivity extends AppCompatActivity implements CategoriaAdapter.OnCategoriaActionListener {

    private RecyclerView recyclerView;
    private CategoriaAdapter adapter;
    private List<Categoria> listaCategorias;
    private DatabaseHelper dbHelper;
    private FloatingActionButton fabAgregarCategoria;

    // Colores predefinidos
    private final String[] coloresDisponibles = {
            "#FF6B6B", "#4ECDC4", "#95E1D3", "#F38181", "#AA96DA",
            "#FCBAD3", "#FFB347", "#DDA0DD", "#98D8C8", "#F7B7A3",
            "#7EC8E3", "#C1E1C1", "#B4A7D6", "#FFD700", "#D2691E",
            "#FF69B4", "#FFC0CB", "#87CEEB", "#DC143C", "#A8D8EA",
            "#38B000", "#5FD068", "#8BC34A", "#7CB342", "#9CCC65",
            "#AED581", "#C5E1A5", "#DCEDC8", "#689F38"
    };

    // Emojis comunes
    private final String[] emojisDisponibles = {
            "🍔", "🚗", "🎮", "💊", "📚", "💡", "🏠", "👕", "🐾", "🎁",
            "💻", "💪", "🛡️", "🍽️", "☕", "🛍️", "💄", "✈️", "📋", "📦",
            "💰", "💼", "📈", "🎯", "🏪", "🏘️", "🏆", "💳", "💵", "🎨",
            "📱", "🎵", "🎬", "📷", "⚽", "🎪", "🌟", "❤️", "🔥", "⭐"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorias);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Gestionar Categorías");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Inicializar vistas
        recyclerView = findViewById(R.id.recyclerViewCategorias);
        fabAgregarCategoria = findViewById(R.id.fabAgregarCategoria);

        // Inicializar base de datos
        dbHelper = new DatabaseHelper(this);

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listaCategorias = new ArrayList<>();
        adapter = new CategoriaAdapter(this, listaCategorias, this);
        recyclerView.setAdapter(adapter);

        // Cargar categorías
        cargarCategorias();

        // FAB para agregar categoría
        fabAgregarCategoria.setOnClickListener(v -> mostrarDialogoAgregarCategoria());
    }

    private void cargarCategorias() {
        listaCategorias.clear();
        Cursor cursor = dbHelper.obtenerTodasCategorias();

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String nombre = cursor.getString(1);
                String tipo = cursor.getString(2);
                String color = cursor.getString(3);
                String icono = cursor.getString(4);

                Categoria categoria = new Categoria(id, nombre, tipo, color, icono);
                listaCategorias.add(categoria);
            } while (cursor.moveToNext());
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    private void mostrarDialogoAgregarCategoria() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_agregar_categoria, null);
        builder.setView(dialogView);

        EditText etNombre = dialogView.findViewById(R.id.etNombreCategoria);
        RadioGroup rgTipo = dialogView.findViewById(R.id.rgTipoCategoria);
        RadioButton rbIngreso = dialogView.findViewById(R.id.rbIngresoCategoria);
        RadioButton rbGasto = dialogView.findViewById(R.id.rbGastoCategoria);
        EditText etIcono = dialogView.findViewById(R.id.etIconoCategoria);
        TextView tvColorSeleccionado = dialogView.findViewById(R.id.tvColorSeleccionado);
        View viewColorPreview = dialogView.findViewById(R.id.viewColorPreview);

        // Color por defecto
        final String[] colorSeleccionado = {"#5DADE2"};
        viewColorPreview.setBackgroundColor(Color.parseColor(colorSeleccionado[0]));

        // Selector de color
        dialogView.findViewById(R.id.btnSeleccionarColor).setOnClickListener(v -> {
            mostrarSelectorColor(color -> {
                colorSeleccionado[0] = color;
                viewColorPreview.setBackgroundColor(Color.parseColor(color));
                tvColorSeleccionado.setText(color);
            });
        });

        // Selector de emoji
        dialogView.findViewById(R.id.btnSeleccionarEmoji).setOnClickListener(v -> {
            mostrarSelectorEmoji(emoji -> {
                etIcono.setText(emoji);
            });
        });

        builder.setTitle("➕ Nueva Categoría")
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();
                    String icono = etIcono.getText().toString().trim();

                    if (nombre.isEmpty()) {
                        Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (icono.isEmpty()) {
                        icono = "📌"; // Emoji por defecto
                    }

                    String tipo = rbIngreso.isChecked() ? "ingreso" : "gasto";

                    long resultado = dbHelper.insertarCategoria(nombre, tipo, colorSeleccionado[0], icono);

                    if (resultado != -1) {
                        Toast.makeText(this, "✅ Categoría creada exitosamente", Toast.LENGTH_SHORT).show();
                        cargarCategorias();
                    } else {
                        Toast.makeText(this, "❌ Error al crear la categoría", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarSelectorColor(OnColorSelectedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona un color");

        String[] nombresColores = new String[coloresDisponibles.length];
        for (int i = 0; i < coloresDisponibles.length; i++) {
            nombresColores[i] = "Color " + (i + 1);
        }

        builder.setItems(nombresColores, (dialog, which) -> {
            listener.onColorSelected(coloresDisponibles[which]);
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void mostrarSelectorEmoji(OnEmojiSelectedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona un emoji");

        builder.setItems(emojisDisponibles, (dialog, which) -> {
            listener.onEmojiSelected(emojisDisponibles[which]);
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    @Override
    public void onEditarCategoria(Categoria categoria) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_editar_categoria, null);
        builder.setView(dialogView);

        EditText etNombre = dialogView.findViewById(R.id.etNombreCategoriaEditar);
        EditText etIcono = dialogView.findViewById(R.id.etIconoCategoriaEditar);
        TextView tvColorSeleccionado = dialogView.findViewById(R.id.tvColorSeleccionadoEditar);
        View viewColorPreview = dialogView.findViewById(R.id.viewColorPreviewEditar);

        // Pre-llenar datos
        etNombre.setText(categoria.getNombre());
        etIcono.setText(categoria.getIcono());
        tvColorSeleccionado.setText(categoria.getColor());
        viewColorPreview.setBackgroundColor(Color.parseColor(categoria.getColor()));

        final String[] colorSeleccionado = {categoria.getColor()};

        // Selector de color
        dialogView.findViewById(R.id.btnSeleccionarColorEditar).setOnClickListener(v -> {
            mostrarSelectorColor(color -> {
                colorSeleccionado[0] = color;
                viewColorPreview.setBackgroundColor(Color.parseColor(color));
                tvColorSeleccionado.setText(color);
            });
        });

        // Selector de emoji
        dialogView.findViewById(R.id.btnSeleccionarEmojiEditar).setOnClickListener(v -> {
            mostrarSelectorEmoji(emoji -> {
                etIcono.setText(emoji);
            });
        });

        builder.setTitle("✏️ Editar Categoría")
                .setPositiveButton("Actualizar", (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();
                    String icono = etIcono.getText().toString().trim();

                    if (nombre.isEmpty()) {
                        Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int resultado = dbHelper.actualizarCategoria(categoria.getId(), nombre, colorSeleccionado[0], icono);

                    if (resultado > 0) {
                        Toast.makeText(this, "✅ Categoría actualizada", Toast.LENGTH_SHORT).show();
                        cargarCategorias();
                    } else {
                        Toast.makeText(this, "❌ Error al actualizar", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onEliminarCategoria(Categoria categoria) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Categoría")
                .setMessage("¿Estás seguro de eliminar esta categoría?\n\nNota: Las transacciones asociadas no se eliminarán.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    dbHelper.eliminarCategoria(categoria.getId());
                    Toast.makeText(this, "Categoría eliminada", Toast.LENGTH_SHORT).show();
                    cargarCategorias();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // Interfaces para callbacks
    interface OnColorSelectedListener {
        void onColorSelected(String color);
    }

    interface OnEmojiSelectedListener {
        void onEmojiSelected(String emoji);
    }
}