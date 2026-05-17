package com.example.praktika2;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class ViewTestActivity extends AppCompatActivity {

    private TextView textViewTestTitle, textViewQuestionCount;
    private RecyclerView recyclerViewQuestions;
    private LinearLayout layoutActions;
    private Button buttonEditTest, buttonDeleteTest;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String classId, testId, title, authorId;
    private List<Map<String, Object>> questions;
    private int questionCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_test);

        db = DatabaseHelper.getInstance();
        mAuth = AuthHelper.getInstance();

        classId = getIntent().getStringExtra("classId");
        testId = getIntent().getStringExtra("testId");
        title = getIntent().getStringExtra("title");
        authorId = getIntent().getStringExtra("authorId");
        questionCount = getIntent().getIntExtra("questionCount", 0);

        textViewTestTitle = findViewById(R.id.textViewTestTitle);
        textViewQuestionCount = findViewById(R.id.textViewQuestionCount);
        recyclerViewQuestions = findViewById(R.id.recyclerViewQuestions);
        layoutActions = findViewById(R.id.layoutActions);
        buttonEditTest = findViewById(R.id.buttonEditTest);
        buttonDeleteTest = findViewById(R.id.buttonDeleteTest);

        textViewTestTitle.setText(title);
        textViewQuestionCount.setText("Вопросов: " + questionCount);

        recyclerViewQuestions.setLayoutManager(new LinearLayoutManager(this));

        // Загружаем вопросы
        loadQuestions();

        // Проверяем авторство
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getUid().equals(authorId)) {
            layoutActions.setVisibility(View.VISIBLE);
        }

        buttonEditTest.setOnClickListener(v -> {
            Intent intent = new Intent(ViewTestActivity.this, EditTestActivity.class);
            intent.putExtra("classId", classId);
            intent.putExtra("testId", testId);
            intent.putExtra("title", title);
            startActivity(intent);
        });

        buttonDeleteTest.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Удалить тест")
                    .setMessage("Вы уверены, что хотите удалить тест «" + title + "»?")
                    .setPositiveButton("Удалить", (dialog, which) -> deleteTest())
                    .setNegativeButton("Отмена", null)
                    .show();
        });
    }

    private void loadQuestions() {
        db.collection("classes").document(classId)
                .collection("tests").document(testId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    questions = (List<Map<String, Object>>) documentSnapshot.get("questions");
                    if (questions != null) {
                        QuestionViewAdapter adapter = new QuestionViewAdapter(questions);
                        recyclerViewQuestions.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки вопросов", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteTest() {
        db.collection("classes").document(classId)
                .collection("tests").document(testId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Тест удалён", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Адаптер для отображения вопросов
    private static class QuestionViewAdapter extends RecyclerView.Adapter<QuestionViewAdapter.ViewHolder> {

        private List<Map<String, Object>> questions;

        public QuestionViewAdapter(List<Map<String, Object>> questions) {
            this.questions = questions;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_question_view, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map<String, Object> question = questions.get(position);

            String text = (String) question.get("text");
            String type = (String) question.get("type");
            List<String> options = (List<String>) question.get("options");
            List<Long> correctAnswersLong = (List<Long>) question.get("correctAnswers");

            holder.textViewQNumber.setText((position + 1) + ".");
            holder.textViewQText.setText(text);
            holder.textViewQType.setText(type.equals("single") ? "Один правильный ответ" : "Несколько правильных ответов");

            // Отображаем варианты ответов
            holder.layoutOptions.removeAllViews();
            for (int i = 0; i < options.size(); i++) {
                TextView optionView = new TextView(holder.itemView.getContext());
                optionView.setText((i + 1) + ". " + options.get(i));
                optionView.setTextSize(14);
                optionView.setTextColor(holder.itemView.getContext().getColor(android.R.color.black));
                optionView.setPadding(0, 4, 0, 4);

                // Проверяем, правильный ли это ответ
                boolean isCorrect = false;
                if (correctAnswersLong != null) {
                    for (Long correct : correctAnswersLong) {
                        if (correct.intValue() == i) {
                            isCorrect = true;
                            break;
                        }
                    }
                }

                if (isCorrect) {
                    optionView.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
                    optionView.setText(optionView.getText() + " ✓");
                }

                holder.layoutOptions.addView(optionView);
            }
        }

        @Override
        public int getItemCount() {
            return questions.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textViewQNumber, textViewQText, textViewQType;
            LinearLayout layoutOptions;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewQNumber = itemView.findViewById(R.id.textViewQNumber);
                textViewQText = itemView.findViewById(R.id.textViewQText);
                textViewQType = itemView.findViewById(R.id.textViewQType);
                layoutOptions = itemView.findViewById(R.id.layoutOptions);
            }
        }
    }
}