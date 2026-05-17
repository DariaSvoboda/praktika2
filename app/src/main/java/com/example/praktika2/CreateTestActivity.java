package com.example.praktika2;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CreateTestActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText editTextTestTitle;
    private RecyclerView recyclerViewQuestions;
    private TextView textViewNoQuestions;
    private Button buttonAddQuestion, buttonSaveTest;
    private ProgressBar progressBar;

    private String classId;
    private List<QuestionModel> questionList;
    private QuestionAdapter questionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_test);

        mAuth = AuthHelper.getInstance();
        db = DatabaseHelper.getInstance();

        classId = getIntent().getStringExtra("classId");

        editTextTestTitle = findViewById(R.id.editTextTestTitle);
        recyclerViewQuestions = findViewById(R.id.recyclerViewQuestions);
        textViewNoQuestions = findViewById(R.id.textViewNoQuestions);
        buttonAddQuestion = findViewById(R.id.buttonAddQuestion);
        buttonSaveTest = findViewById(R.id.buttonSaveTest);
        progressBar = findViewById(R.id.progressBar);

        questionList = new ArrayList<>();
        questionAdapter = new QuestionAdapter(questionList);
        recyclerViewQuestions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewQuestions.setAdapter(questionAdapter);

        updateEmptyView();

        // Кнопка добавления вопроса
        buttonAddQuestion.setOnClickListener(v -> showAddQuestionDialog());

        // Кнопка сохранения теста
        buttonSaveTest.setOnClickListener(v -> saveTest());
    }

    private void showAddQuestionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_question, null);
        builder.setView(dialogView);

        EditText editTextQuestionText = dialogView.findViewById(R.id.editTextQuestionText);
        RadioGroup radioGroupType = dialogView.findViewById(R.id.radioGroupType);
        LinearLayout layoutOptions = dialogView.findViewById(R.id.layoutOptions);
        Button buttonAddOption = dialogView.findViewById(R.id.buttonAddOption);
        Button buttonSaveQuestion = dialogView.findViewById(R.id.buttonSaveQuestion);

        // Списки для хранения полей вариантов
        List<EditText> optionEditTexts = new ArrayList<>();
        List<CheckBox> optionCheckBoxes = new ArrayList<>();

        // Добавляем 2 пустых варианта по умолчанию
        addOptionRow(layoutOptions, optionEditTexts, optionCheckBoxes);
        addOptionRow(layoutOptions, optionEditTexts, optionCheckBoxes);

        buttonAddOption.setOnClickListener(v ->
                addOptionRow(layoutOptions, optionEditTexts, optionCheckBoxes));

        // При смене типа вопроса меняем поведение чекбоксов
        radioGroupType.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isSingle = checkedId == R.id.radioSingle;
            for (CheckBox cb : optionCheckBoxes) {
                if (isSingle) {
                    // Для одного ответа — только один можно выбрать
                    cb.setOnCheckedChangeListener(null);
                    cb.setOnClickListener(view -> {
                        // Снимаем все
                        for (CheckBox other : optionCheckBoxes) {
                            other.setChecked(false);
                        }
                        // Ставим выбранный
                        cb.setChecked(true);
                    });
                } else {
                    // Для нескольких — можно выбирать сколько угодно
                    cb.setOnClickListener(null);
                    cb.setOnCheckedChangeListener(null);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        buttonSaveQuestion.setOnClickListener(v -> {
            String questionText = editTextQuestionText.getText().toString().trim();

            if (TextUtils.isEmpty(questionText)) {
                Toast.makeText(CreateTestActivity.this, "Введите текст вопроса", Toast.LENGTH_SHORT).show();
                return;
            }

            // Собираем варианты ответов
            List<String> options = new ArrayList<>();
            List<Integer> correctAnswers = new ArrayList<>();

            for (int i = 0; i < optionEditTexts.size(); i++) {
                String optionText = optionEditTexts.get(i).getText().toString().trim();
                if (!TextUtils.isEmpty(optionText)) {
                    options.add(optionText);
                    if (optionCheckBoxes.get(i).isChecked()) {
                        correctAnswers.add(options.size() - 1);
                    }
                }
            }

            if (options.size() < 2) {
                Toast.makeText(CreateTestActivity.this, "Добавьте минимум 2 варианта ответа", Toast.LENGTH_SHORT).show();
                return;
            }

            if (correctAnswers.isEmpty()) {
                Toast.makeText(CreateTestActivity.this, "Отметьте хотя бы один правильный ответ", Toast.LENGTH_SHORT).show();
                return;
            }

            // Определяем тип вопроса
            boolean isSingle = radioGroupType.getCheckedRadioButtonId() == R.id.radioSingle;
            String type = isSingle ? "single" : "multiple";

            if (isSingle && correctAnswers.size() > 1) {
                Toast.makeText(CreateTestActivity.this, "Для типа «Один ответ» отметьте только один вариант", Toast.LENGTH_SHORT).show();
                return;
            }

            // Создаём вопрос
            QuestionModel question = new QuestionModel(
                    UUID.randomUUID().toString(),
                    questionText,
                    type,
                    options,
                    correctAnswers
            );

            questionList.add(question);
            questionAdapter.notifyItemInserted(questionList.size() - 1);
            updateEmptyView();

            dialog.dismiss();
        });
    }

    private void addOptionRow(LinearLayout layoutOptions, List<EditText> optionEditTexts, List<CheckBox> optionCheckBoxes) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 0, 0, 8);
        row.setLayoutParams(rowParams);

        CheckBox checkBox = new CheckBox(this);
        LinearLayout.LayoutParams checkParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        checkParams.gravity = android.view.Gravity.CENTER_VERTICAL;
        checkBox.setLayoutParams(checkParams);

        EditText editText = new EditText(this);
        LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        editParams.gravity = android.view.Gravity.CENTER_VERTICAL;
        editText.setLayoutParams(editParams);
        editText.setHint("Вариант ответа");
        editText.setPadding(12, 8, 12, 8);

        row.addView(checkBox);
        row.addView(editText);
        layoutOptions.addView(row);

        optionEditTexts.add(editText);
        optionCheckBoxes.add(checkBox);
    }

    private void saveTest() {
        String title = editTextTestTitle.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            editTextTestTitle.setError("Введите название теста");
            return;
        }

        if (questionList.isEmpty()) {
            Toast.makeText(this, "Добавьте хотя бы один вопрос", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        showLoading(true);

        // Преобразуем вопросы в список Map
        List<Map<String, Object>> questionsData = new ArrayList<>();
        for (QuestionModel q : questionList) {
            questionsData.add(q.toMap());
        }

        Map<String, Object> testData = new HashMap<>();
        testData.put("title", title);
        testData.put("authorId", user.getUid());
        testData.put("questions", questionsData);
        testData.put("questionCount", questionList.size());

        // Сохраняем в подколлекцию tests внутри класса
        db.collection("classes").document(classId)
                .collection("tests")
                .add(testData)
                .addOnSuccessListener(documentReference -> {
                    showLoading(false);
                    Toast.makeText(CreateTestActivity.this, "Тест сохранён!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(CreateTestActivity.this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void updateEmptyView() {
        if (questionList.isEmpty()) {
            textViewNoQuestions.setVisibility(View.VISIBLE);
            recyclerViewQuestions.setVisibility(View.GONE);
        } else {
            textViewNoQuestions.setVisibility(View.GONE);
            recyclerViewQuestions.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        buttonAddQuestion.setEnabled(!isLoading);
        buttonSaveTest.setEnabled(!isLoading);
    }

    // ==================== АДАПТЕР ДЛЯ СПИСКА ВОПРОСОВ ====================
    private static class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {

        private List<QuestionModel> questions;

        public QuestionAdapter(List<QuestionModel> questions) {
            this.questions = questions;
        }

        @NonNull
        @Override
        public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_question, parent, false);
            return new QuestionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
            QuestionModel question = questions.get(position);
            holder.textViewQuestionNumber.setText((position + 1) + ".");
            holder.textViewQuestionText.setText(question.getText());
            holder.textViewQuestionType.setText(question.getType().equals("single") ? "Один ответ" : "Несколько ответов");
            holder.textViewOptionsCount.setText("Вариантов: " + question.getOptions().size());
        }

        @Override
        public int getItemCount() {
            return questions.size();
        }

        static class QuestionViewHolder extends RecyclerView.ViewHolder {
            TextView textViewQuestionNumber, textViewQuestionText, textViewQuestionType, textViewOptionsCount;

            public QuestionViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewQuestionNumber = itemView.findViewById(R.id.textViewQuestionNumber);
                textViewQuestionText = itemView.findViewById(R.id.textViewQuestionText);
                textViewQuestionType = itemView.findViewById(R.id.textViewQuestionType);
                textViewOptionsCount = itemView.findViewById(R.id.textViewOptionsCount);
            }
        }
    }
}