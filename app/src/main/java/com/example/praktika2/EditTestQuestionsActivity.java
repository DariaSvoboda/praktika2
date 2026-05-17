package com.example.praktika2;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EditTestQuestionsActivity extends AppCompatActivity {

    private TextView textViewTestTitle;
    private RecyclerView recyclerViewQuestions;
    private TextView textViewEmpty;
    private Button buttonAddQuestion, buttonSaveAll;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private String classId, testId, testTitle;
    private List<QuestionModel> questionList;
    private EditQuestionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_test_questions);

        db = DatabaseHelper.getInstance();

        classId = getIntent().getStringExtra("classId");
        testId = getIntent().getStringExtra("testId");
        testTitle = getIntent().getStringExtra("title");

        textViewTestTitle = findViewById(R.id.textViewTestTitle);
        recyclerViewQuestions = findViewById(R.id.recyclerViewQuestions);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        buttonAddQuestion = findViewById(R.id.buttonAddQuestion);
        buttonSaveAll = findViewById(R.id.buttonSaveAll);
        progressBar = findViewById(R.id.progressBar);

        textViewTestTitle.setText(testTitle);

        questionList = new ArrayList<>();
        adapter = new EditQuestionAdapter(questionList);
        recyclerViewQuestions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewQuestions.setAdapter(adapter);

        loadQuestions();

        buttonAddQuestion.setOnClickListener(v -> showAddQuestionDialog());
        buttonSaveAll.setOnClickListener(v -> saveAllQuestions());
    }

    private void loadQuestions() {
        db.collection("classes").document(classId)
                .collection("tests").document(testId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<Map<String, Object>> questionsData =
                            (List<Map<String, Object>>) documentSnapshot.get("questions");

                    questionList.clear();
                    if (questionsData != null) {
                        for (Map<String, Object> qData : questionsData) {
                            QuestionModel q = new QuestionModel();
                            q.setId((String) qData.get("id"));
                            q.setText((String) qData.get("text"));
                            q.setType((String) qData.get("type"));
                            q.setOptions((List<String>) qData.get("options"));

                            List<Long> correctLong = (List<Long>) qData.get("correctAnswers");
                            List<Integer> correctInt = new ArrayList<>();
                            if (correctLong != null) {
                                for (Long l : correctLong) {
                                    correctInt.add(l.intValue());
                                }
                            }
                            q.setCorrectAnswers(correctInt);
                            questionList.add(q);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    updateEmptyView();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки вопросов", Toast.LENGTH_SHORT).show();
                });
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

        List<EditText> optionEditTexts = new ArrayList<>();
        List<CheckBox> optionCheckBoxes = new ArrayList<>();

        addOptionRow(layoutOptions, optionEditTexts, optionCheckBoxes);
        addOptionRow(layoutOptions, optionEditTexts, optionCheckBoxes);

        buttonAddOption.setOnClickListener(v ->
                addOptionRow(layoutOptions, optionEditTexts, optionCheckBoxes));

        radioGroupType.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isSingle = checkedId == R.id.radioSingle;
            for (CheckBox cb : optionCheckBoxes) {
                cb.setOnCheckedChangeListener(null);
                if (isSingle) {
                    cb.setOnClickListener(view -> {
                        for (CheckBox other : optionCheckBoxes) {
                            other.setChecked(false);
                        }
                        cb.setChecked(true);
                    });
                } else {
                    cb.setOnClickListener(null);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        buttonSaveQuestion.setOnClickListener(v -> {
            String qText = editTextQuestionText.getText().toString().trim();
            if (TextUtils.isEmpty(qText)) {
                Toast.makeText(this, "Введите текст вопроса", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> options = new ArrayList<>();
            List<Integer> correctAnswers = new ArrayList<>();

            for (int i = 0; i < optionEditTexts.size(); i++) {
                String opt = optionEditTexts.get(i).getText().toString().trim();
                if (!TextUtils.isEmpty(opt)) {
                    options.add(opt);
                    if (optionCheckBoxes.get(i).isChecked()) {
                        correctAnswers.add(options.size() - 1);
                    }
                }
            }

            if (options.size() < 2) {
                Toast.makeText(this, "Минимум 2 варианта", Toast.LENGTH_SHORT).show();
                return;
            }
            if (correctAnswers.isEmpty()) {
                Toast.makeText(this, "Отметьте правильный ответ", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isSingle = radioGroupType.getCheckedRadioButtonId() == R.id.radioSingle;
            String type = isSingle ? "single" : "multiple";

            if (isSingle && correctAnswers.size() > 1) {
                Toast.makeText(this, "Выберите только один ответ", Toast.LENGTH_SHORT).show();
                return;
            }

            QuestionModel question = new QuestionModel(
                    UUID.randomUUID().toString(), qText, type, options, correctAnswers);

            questionList.add(question);
            adapter.notifyItemInserted(questionList.size() - 1);
            updateEmptyView();
            dialog.dismiss();
        });
    }
    private void showEditQuestionDialog(int position) {
        QuestionModel existingQuestion = questionList.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_question, null);
        builder.setView(dialogView);

        EditText editTextQuestionText = dialogView.findViewById(R.id.editTextQuestionText);
        RadioGroup radioGroupType = dialogView.findViewById(R.id.radioGroupType);
        LinearLayout layoutOptions = dialogView.findViewById(R.id.layoutOptions);
        Button buttonAddOption = dialogView.findViewById(R.id.buttonAddOption);
        Button buttonSaveQuestion = dialogView.findViewById(R.id.buttonSaveQuestion);

        // Заполняем существующими данными
        editTextQuestionText.setText(existingQuestion.getText());

        if (existingQuestion.getType().equals("single")) {
            radioGroupType.check(R.id.radioSingle);
        } else {
            radioGroupType.check(R.id.radioMultiple);
        }

        List<EditText> optionEditTexts = new ArrayList<>();
        List<CheckBox> optionCheckBoxes = new ArrayList<>();

        // Добавляем существующие варианты
        for (int i = 0; i < existingQuestion.getOptions().size(); i++) {
            addOptionRow(layoutOptions, optionEditTexts, optionCheckBoxes);
            optionEditTexts.get(i).setText(existingQuestion.getOptions().get(i));
            if (existingQuestion.getCorrectAnswers().contains(i)) {
                optionCheckBoxes.get(i).setChecked(true);
            }
        }

        // Если вариантов меньше 2, добавляем пустые
        if (existingQuestion.getOptions().size() < 2) {
            addOptionRow(layoutOptions, optionEditTexts, optionCheckBoxes);
            addOptionRow(layoutOptions, optionEditTexts, optionCheckBoxes);
        }

        buttonAddOption.setOnClickListener(v ->
                addOptionRow(layoutOptions, optionEditTexts, optionCheckBoxes));

        // Логика для single/multiple
        radioGroupType.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isSingle = checkedId == R.id.radioSingle;
            for (CheckBox cb : optionCheckBoxes) {
                cb.setOnCheckedChangeListener(null);
                if (isSingle) {
                    cb.setOnClickListener(view -> {
                        for (CheckBox other : optionCheckBoxes) {
                            other.setChecked(false);
                        }
                        cb.setChecked(true);
                    });
                } else {
                    cb.setOnClickListener(null);
                }
            }
        });

        // Меняем текст кнопки
        buttonSaveQuestion.setText("Сохранить изменения");

        AlertDialog dialog = builder.create();
        dialog.show();

        buttonSaveQuestion.setOnClickListener(v -> {
            String qText = editTextQuestionText.getText().toString().trim();
            if (TextUtils.isEmpty(qText)) {
                Toast.makeText(this, "Введите текст вопроса", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> options = new ArrayList<>();
            List<Integer> correctAnswers = new ArrayList<>();

            for (int i = 0; i < optionEditTexts.size(); i++) {
                String opt = optionEditTexts.get(i).getText().toString().trim();
                if (!TextUtils.isEmpty(opt)) {
                    options.add(opt);
                    if (optionCheckBoxes.get(i).isChecked()) {
                        correctAnswers.add(options.size() - 1);
                    }
                }
            }

            if (options.size() < 2) {
                Toast.makeText(this, "Минимум 2 варианта", Toast.LENGTH_SHORT).show();
                return;
            }
            if (correctAnswers.isEmpty()) {
                Toast.makeText(this, "Отметьте правильный ответ", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isSingle = radioGroupType.getCheckedRadioButtonId() == R.id.radioSingle;
            String type = isSingle ? "single" : "multiple";

            if (isSingle && correctAnswers.size() > 1) {
                Toast.makeText(this, "Выберите только один ответ", Toast.LENGTH_SHORT).show();
                return;
            }

            // Обновляем существующий вопрос
            existingQuestion.setText(qText);
            existingQuestion.setType(type);
            existingQuestion.setOptions(options);
            existingQuestion.setCorrectAnswers(correctAnswers);

            adapter.notifyItemChanged(position);
            dialog.dismiss();
        });
    }
    private void addOptionRow(LinearLayout layout, List<EditText> editTexts, List<CheckBox> checkBoxes) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        CheckBox cb = new CheckBox(this);
        EditText et = new EditText(this);
        et.setHint("Вариант ответа");
        et.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        row.addView(cb);
        row.addView(et);
        layout.addView(row);

        editTexts.add(et);
        checkBoxes.add(cb);
    }

    private void saveAllQuestions() {
        if (questionList.isEmpty()) {
            Toast.makeText(this, "Добавьте хотя бы один вопрос", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        buttonSaveAll.setEnabled(false);

        List<Map<String, Object>> questionsData = new ArrayList<>();
        for (QuestionModel q : questionList) {
            questionsData.add(q.toMap());
        }

        Map<String, Object> update = new HashMap<>();
        update.put("questions", questionsData);
        update.put("questionCount", questionList.size());

        db.collection("classes").document(classId)
                .collection("tests").document(testId)
                .update(update)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Вопросы сохранены!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    buttonSaveAll.setEnabled(true);
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void updateEmptyView() {
        textViewEmpty.setVisibility(questionList.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerViewQuestions.setVisibility(questionList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    // Адаптер с кнопкой удаления
    // Адаптер с кнопкой удаления
    private class EditQuestionAdapter extends RecyclerView.Adapter<EditQuestionAdapter.ViewHolder> {

        private List<QuestionModel> questions;

        public EditQuestionAdapter(List<QuestionModel> questions) {
            this.questions = questions;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_question, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            QuestionModel q = questions.get(position);
            holder.textNumber.setText((position + 1) + ".");
            holder.textQuestion.setText(q.getText());
            holder.textType.setText(q.getType().equals("single") ? "Один ответ" : "Несколько ответов");
            holder.textOptionsCount.setText("Вариантов: " + q.getOptions().size());

            // Обычное нажатие — редактирование вопроса
            holder.itemView.setOnClickListener(v -> {
                showEditQuestionDialog(position);
            });

            // Долгое нажатие — удаление
            holder.itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(EditTestQuestionsActivity.this)
                        .setTitle("Удалить вопрос")
                        .setMessage("Удалить вопрос " + (position + 1) + "?")
                        .setPositiveButton("Удалить", (d, w) -> {
                            questions.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, questions.size());
                            updateEmptyView();
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return questions.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textNumber, textQuestion, textType, textOptionsCount;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                textNumber = itemView.findViewById(R.id.textViewQuestionNumber);
                textQuestion = itemView.findViewById(R.id.textViewQuestionText);
                textType = itemView.findViewById(R.id.textViewQuestionType);
                textOptionsCount = itemView.findViewById(R.id.textViewOptionsCount);
            }
        }
    }
}