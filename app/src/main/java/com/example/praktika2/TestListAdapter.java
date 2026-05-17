package com.example.praktika2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TestListAdapter extends RecyclerView.Adapter<TestListAdapter.ViewHolder> {

    private List<TestModel> testList;
    private OnTestClickListener listener;

    public interface OnTestClickListener {
        void onTestClick(TestModel testModel);
    }

    public TestListAdapter(List<TestModel> testList, OnTestClickListener listener) {
        this.testList = testList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_class, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TestModel test = testList.get(position);
        holder.textViewClassName.setText(test.getTitle());
        holder.textViewClassCode.setText("Вопросов: " + test.getQuestionCount());
        holder.textViewRole.setText("Автор");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTestClick(test);
            }
        });
    }

    @Override
    public int getItemCount() {
        return testList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewClassName, textViewClassCode, textViewRole;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewClassName = itemView.findViewById(R.id.textViewClassName);
            textViewClassCode = itemView.findViewById(R.id.textViewClassCode);
            textViewRole = itemView.findViewById(R.id.textViewRole);
        }
    }
}