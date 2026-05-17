package com.example.praktika2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MaterialAdapter extends RecyclerView.Adapter<MaterialAdapter.ViewHolder> {

    private List<MaterialModel> materialList;
    private OnMaterialClickListener listener;

    public interface OnMaterialClickListener {
        void onMaterialClick(MaterialModel material);
    }

    public MaterialAdapter(List<MaterialModel> materialList, OnMaterialClickListener listener) {
        this.materialList = materialList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_material, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MaterialModel material = materialList.get(position);

        holder.textViewMaterialTitle.setText(material.getTitle());
        holder.textViewMaterialType.setText(material.getType().equals("text") ? "Текстовый конспект" : "Ссылка");

        // Превью содержимого
        String preview = material.getContent();
        if (preview != null && preview.length() > 100) {
            preview = preview.substring(0, 100) + "...";
        }
        holder.textViewMaterialPreview.setText(preview);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMaterialClick(material);
            }
        });
    }

    @Override
    public int getItemCount() {
        return materialList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMaterialTitle, textViewMaterialType, textViewMaterialPreview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMaterialTitle = itemView.findViewById(R.id.textViewMaterialTitle);
            textViewMaterialType = itemView.findViewById(R.id.textViewMaterialType);
            textViewMaterialPreview = itemView.findViewById(R.id.textViewMaterialPreview);
        }
    }
}