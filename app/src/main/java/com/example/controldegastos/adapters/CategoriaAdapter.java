package com.example.controldegastos.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controldegastos.R;
import com.example.controldegastos.models.Categoria;

import java.util.List;

public class CategoriaAdapter extends RecyclerView.Adapter<CategoriaAdapter.CategoriaViewHolder> {

    private Context context;
    private List<Categoria> listaCategorias;
    private OnCategoriaActionListener listener;

    public interface OnCategoriaActionListener {
        void onEditarCategoria(Categoria categoria);
        void onEliminarCategoria(Categoria categoria);
    }

    public CategoriaAdapter(Context context, List<Categoria> listaCategorias, OnCategoriaActionListener listener) {
        this.context = context;
        this.listaCategorias = listaCategorias;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_categoria, parent, false);
        return new CategoriaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriaViewHolder holder, int position) {
        Categoria categoria = listaCategorias.get(position);

        holder.tvIcono.setText(categoria.getIcono());
        holder.tvNombre.setText(categoria.getNombre());
        holder.tvTipo.setText(categoria.getTipo().equals("ingreso") ? "↗️ Ingreso" : "↘️ Gasto");

        // Color del badge de tipo
        if (categoria.getTipo().equals("ingreso")) {
            holder.tvTipo.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.tvTipo.setTextColor(Color.parseColor("#F44336"));
        }

        // Color de la categoría
        try {
            holder.viewColor.setBackgroundColor(Color.parseColor(categoria.getColor()));
        } catch (Exception e) {
            holder.viewColor.setBackgroundColor(Color.parseColor("#9E9E9E"));
        }

        // Botón Editar
        holder.btnEditar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditarCategoria(categoria);
            }
        });

        // Botón Eliminar
        holder.btnEliminar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEliminarCategoria(categoria);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaCategorias.size();
    }

    public static class CategoriaViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvIcono, tvNombre, tvTipo;
        View viewColor;
        ImageButton btnEditar, btnEliminar;

        public CategoriaViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardCategoria);
            tvIcono = itemView.findViewById(R.id.tvIconoCategoria);
            tvNombre = itemView.findViewById(R.id.tvNombreCategoria);
            tvTipo = itemView.findViewById(R.id.tvTipoCategoria);
            viewColor = itemView.findViewById(R.id.viewColorCategoria);
            btnEditar = itemView.findViewById(R.id.btnEditarCategoria);
            btnEliminar = itemView.findViewById(R.id.btnEliminarCategoria);
        }
    }
}