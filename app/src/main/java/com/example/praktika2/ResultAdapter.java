package com.example.praktika2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {

    private List<TestResultModel> resultList;
    private boolean showStudent; // показывать ли email ученика

    public ResultAdapter(List<TestResultModel> resultList, boolean showStudent) {
        this.resultList = resultList;
        this.showStudent = showStudent;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TestResultModel result = resultList.get(position);

        holder.textViewTestTitle.setText(result.getTestTitle());
        holder.textViewDetails.setText(result.getCorrectCount() + " из " + result.getTotalQuestions() + " правильных");

        // Оценка в процентах
        int score = result.getScore();
        holder.textViewScore.setText(score + "%");

        // Цвет оценки
        int color;
        if (score >= 90) color = android.graphics.Color.rgb(76, 175, 80);
        else if (score >= 70) color = android.graphics.Color.rgb(255, 152, 0);
        else color = android.graphics.Color.rgb(244, 67, 54);
        holder.textViewScore.setTextColor(color);

        // Дата
        if (result.getDate() != null) {
            Date date = result.getDate().toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            holder.textViewDate.setText(sdf.format(date));
        }

        // Email ученика (только для преподавателя)
        if (showStudent) {
            holder.textViewStudent.setVisibility(View.VISIBLE);
            holder.textViewStudent.setText(result.getStudentEmail());
        } else {
            holder.textViewStudent.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return resultList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTestTitle, textViewScore, textViewDetails, textViewDate, textViewStudent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTestTitle = itemView.findViewById(R.id.textViewTestTitle);
            textViewScore = itemView.findViewById(R.id.textViewScore);
            textViewDetails = itemView.findViewById(R.id.textViewDetails);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewStudent = itemView.findViewById(R.id.textViewStudent);
        }
    }
}