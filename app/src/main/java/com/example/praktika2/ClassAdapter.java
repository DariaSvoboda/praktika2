package com.example.praktika2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {

    private List<ClassModel> classList;
    private OnClassClickListener listener;

    // Интерфейс для обработки нажатий
    public interface OnClassClickListener {
        void onClassClick(ClassModel classModel);
    }

    public ClassAdapter(List<ClassModel> classList, OnClassClickListener listener) {
        this.classList = classList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_class, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        ClassModel classModel = classList.get(position);

        holder.textViewClassName.setText(classModel.getName());
        holder.textViewClassCode.setText(classModel.getCode());

        if (classModel.isCreator()) {
            holder.textViewRole.setText("Создатель");
            holder.textViewRole.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
        } else {
            holder.textViewRole.setText("Соавтор");
            holder.textViewRole.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_blue_dark));
        }

        // Обработка нажатия
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClassClick(classModel);
            }
        });
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    static class ClassViewHolder extends RecyclerView.ViewHolder {
        TextView textViewClassName, textViewClassCode, textViewRole;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewClassName = itemView.findViewById(R.id.textViewClassName);
            textViewClassCode = itemView.findViewById(R.id.textViewClassCode);
            textViewRole = itemView.findViewById(R.id.textViewRole);
        }
    }
}