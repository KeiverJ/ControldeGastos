package com.example.controldegastos.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controldegastos.R;
import com.example.controldegastos.activities.EditarTransaccionActivity;
import com.example.controldegastos.database.DatabaseHelper;
import com.example.controldegastos.models.Transaccion;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransaccionAdapter extends RecyclerView.Adapter<TransaccionAdapter.TransaccionViewHolder> {

    private Context context;
    private List<Transaccion> listaTransacciones;
    private DatabaseHelper dbHelper;
    private OnTransaccionChangeListener listener;

    // Interface para notificar cambios
    public interface OnTransaccionChangeListener {
        void onTransaccionEliminada();
    }

    public TransaccionAdapter(Context context, List<Transaccion> listaTransacciones, OnTransaccionChangeListener listener) {
        this.context = context;
        this.listaTransacciones = listaTransacciones;
        this.dbHelper = new DatabaseHelper(context);
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransaccionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaccion, parent, false);
        return new TransaccionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransaccionViewHolder holder, int position) {
        Transaccion transaccion = listaTransacciones.get(position);

        // Formatear monto
        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        String montoFormateado = formatoMoneda.format(transaccion.getMonto());

        // Configurar textos
        holder.tvMonto.setText(montoFormateado);
        holder.tvDescripcion.setText(transaccion.getDescripcion());
        holder.tvCategoria.setText(transaccion.getNombreCategoria());

        // Formatear fecha
        try {
            Date fecha = new Date(Long.parseLong(transaccion.getFecha()));
            SimpleDateFormat formato = new SimpleDateFormat("dd MMM yyyy", new Locale("es", "ES"));
            holder.tvFecha.setText(formato.format(fecha));
        } catch (Exception e) {
            holder.tvFecha.setText(transaccion.getFecha());
        }

        // Color según tipo
        if (transaccion.getTipo().equals("ingreso")) {
            holder.tvMonto.setTextColor(Color.parseColor("#4CAF50")); // Verde
            holder.tvTipo.setText("+ Ingreso");
            holder.tvTipo.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.tvMonto.setTextColor(Color.parseColor("#F44336")); // Rojo
            holder.tvTipo.setText("- Gasto");
            holder.tvTipo.setTextColor(Color.parseColor("#F44336"));
        }

        // Color de categoría
        try {
            holder.viewColorCategoria.setBackgroundColor(Color.parseColor(transaccion.getColorCategoria()));
        } catch (Exception e) {
            holder.viewColorCategoria.setBackgroundColor(Color.parseColor("#9E9E9E"));
        }

        // Botón Editar
        holder.btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditarTransaccionActivity.class);
            intent.putExtra("id_transaccion", transaccion.getId());
            intent.putExtra("monto", transaccion.getMonto());
            intent.putExtra("descripcion", transaccion.getDescripcion());
            intent.putExtra("fecha", transaccion.getFecha());
            intent.putExtra("tipo", transaccion.getTipo());
            intent.putExtra("id_categoria", transaccion.getIdCategoria());
            context.startActivity(intent);
        });

        // Botón Eliminar
        holder.btnEliminar.setOnClickListener(v -> {
            mostrarDialogoEliminar(transaccion, position);
        });

        // Click en el card para ver detalles
        holder.cardView.setOnClickListener(v -> {
            Toast.makeText(context, "Detalles: " + transaccion.getDescripcion(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return listaTransacciones.size();
    }

    // Método para mostrar diálogo de confirmación al eliminar
    private void mostrarDialogoEliminar(Transaccion transaccion, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Eliminar Transacción")
                .setMessage("¿Estás seguro de eliminar esta transacción?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    // Eliminar de la base de datos
                    dbHelper.eliminarTransaccion(transaccion.getId());

                    // Eliminar de la lista
                    listaTransacciones.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, listaTransacciones.size());

                    Toast.makeText(context, "Transacción eliminada", Toast.LENGTH_SHORT).show();

                    // Notificar al activity para actualizar balance
                    if (listener != null) {
                        listener.onTransaccionEliminada();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Método para actualizar la lista
    public void actualizarLista(List<Transaccion> nuevaLista) {
        this.listaTransacciones = nuevaLista;
        notifyDataSetChanged();
    }

    // ViewHolder
    public static class TransaccionViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvMonto, tvDescripcion, tvCategoria, tvFecha, tvTipo;
        View viewColorCategoria;
        ImageButton btnEditar, btnEliminar;

        public TransaccionViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardTransaccion);
            tvMonto = itemView.findViewById(R.id.tvMonto);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            viewColorCategoria = itemView.findViewById(R.id.viewColorCategoria);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}