package com.example.praktika2;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TakeTestActivity extends AppCompatActivity {

    private TextView textViewTestTitle, textViewProgress, textViewQuestionText;
    private LinearLayout layoutOptions;
    private Button buttonPrevious, buttonNext, buttonFinish;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String classId, testId, testTitle;

    private List<Map<String, Object>> questions;
    private List<List<Integer>> selectedAnswers; // ответы ученика на каждый вопрос
    private int currentQuestionIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_test);

        db = DatabaseHelper.getInstance();
        mAuth = AuthHelper.getInstance();

        classId = getIntent().getStringExtra("classId");
        testId = getIntent().getStringExtra("testId");
        testTitle = getIntent().getStringExtra("title");

        textViewTestTitle = findViewById(R.id.textViewTestTitle);
        textViewProgress = findViewById(R.id.textViewProgress);
        textViewQuestionText = findViewById(R.id.textViewQuestionText);
        layoutOptions = findViewById(R.id.layoutOptions);
        buttonPrevious = findViewById(R.id.buttonPrevious);
        buttonNext = findViewById(R.id.buttonNext);
        buttonFinish = findViewById(R.id.buttonFinish);

        textViewTestTitle.setText(testTitle);
        selectedAnswers = new ArrayList<>();

        buttonPrevious.setOnClickListener(v -> navigateQuestion(-1));
        buttonNext.setOnClickListener(v -> navigateQuestion(1));
        buttonFinish.setOnClickListener(v -> finishTest());

        loadQuestions();
    }

    private void loadQuestions() {
        db.collection("classes").document(classId)
                .collection("tests").document(testId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    questions = (List<Map<String, Object>>) documentSnapshot.get("questions");

                    if (questions != null && !questions.isEmpty()) {
                        // Инициализируем массив ответов
                        for (int i = 0; i < questions.size(); i++) {
                            selectedAnswers.add(new ArrayList<>());
                        }
                        showQuestion(0);
                    } else {
                        Toast.makeText(this, "В тесте нет вопросов", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки теста", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void showQuestion(int index) {
        currentQuestionIndex = index;
        Map<String, Object> question = questions.get(index);

        String text = (String) question.get("text");
        String type = (String) question.get("type");
        List<String> options = (List<String>) question.get("options");

        textViewQuestionText.setText(text);
        textViewProgress.setText("Вопрос " + (index + 1) + " из " + questions.size());

        // Очищаем варианты
        layoutOptions.removeAllViews();

        // Восстанавливаем выбранные ответы
        List<Integer> savedAnswers = selectedAnswers.get(index);

        // Создаём варианты ответов
        for (int i = 0; i < options.size(); i++) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText((i + 1) + ". " + options.get(i));
            checkBox.setTextSize(16);
            checkBox.setPadding(16, 12, 16, 12);
            checkBox.setBackgroundColor(getColor(android.R.color.white));

            // Восстанавливаем выбор
            if (savedAnswers.contains(i)) {
                checkBox.setChecked(true);
            }

            final int optionIndex = i;

            // Для типа "single" — radio-поведение (только один выбор)
            if (type.equals("single")) {
                checkBox.setOnClickListener(v -> {
                    // Снимаем все галочки
                    for (int j = 0; j < layoutOptions.getChildCount(); j++) {
                        CheckBox cb = (CheckBox) layoutOptions.getChildAt(j);
                        cb.setChecked(false);
                    }
                    // Ставим галочку только на выбранном
                    checkBox.setChecked(true);

                    // Сохраняем ответ
                    List<Integer> answers = selectedAnswers.get(currentQuestionIndex);
                    answers.clear();
                    answers.add(optionIndex);
                });
            } else {
                // Для типа "multiple" — обычное поведение
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    List<Integer> answers = selectedAnswers.get(currentQuestionIndex);
                    if (isChecked) {
                        if (!answers.contains(optionIndex)) {
                            answers.add(optionIndex);
                        }
                    } else {
                        answers.remove((Integer) optionIndex);
                    }
                });
            }

            layoutOptions.addView(checkBox);
        }

        // Обновляем кнопки навигации
        buttonPrevious.setVisibility(index > 0 ? View.VISIBLE : View.GONE);
        if (index == questions.size() - 1) {
            buttonNext.setVisibility(View.GONE);
            buttonFinish.setVisibility(View.VISIBLE);
        } else {
            buttonNext.setVisibility(View.VISIBLE);
            buttonFinish.setVisibility(View.GONE);
        }
    }

    private void saveAnswer(int optionIndex, boolean isChecked) {
        List<Integer> answers = selectedAnswers.get(currentQuestionIndex);
        if (isChecked) {
            if (!answers.contains(optionIndex)) {
                answers.add(optionIndex);
            }
        } else {
            answers.remove((Integer) optionIndex);
        }
    }

    private void navigateQuestion(int direction) {
        int newIndex = currentQuestionIndex + direction;
        if (newIndex >= 0 && newIndex < questions.size()) {
            showQuestion(newIndex);
        }
    }

    private void finishTest() {
        // Подсчитываем результат
        int totalQuestions = questions.size();
        int correctCount = 0;

        for (int i = 0; i < questions.size(); i++) {
            Map<String, Object> question = questions.get(i);
            List<Long> correctAnswersLong = (List<Long>) question.get("correctAnswers");
            List<Integer> studentAnswers = selectedAnswers.get(i);

            // Преобразуем Long в Integer
            List<Integer> correctAnswers = new ArrayList<>();
            if (correctAnswersLong != null) {
                for (Long l : correctAnswersLong) {
                    correctAnswers.add(l.intValue());
                }
            }

            // Сравниваем ответы
            if (studentAnswers.size() == correctAnswers.size() &&
                    studentAnswers.containsAll(correctAnswers)) {
                correctCount++;
            }
        }

        int score = (int) Math.round((correctCount * 100.0) / totalQuestions);

        // Сохраняем результат
        saveResult(score, correctCount, totalQuestions);
    }

    private void saveResult(int score, int correctCount, int totalQuestions) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        // Преобразуем ответы для Firestore
        List<Map<String, Object>> answersForFirestore = new ArrayList<>();
        for (List<Integer> answer : selectedAnswers) {
            Map<String, Object> answerMap = new HashMap<>();
            List<Long> answerLongs = new ArrayList<>();
            for (Integer ans : answer) {
                answerLongs.add(ans.longValue());
            }
            answerMap.put("selected", answerLongs);
            answersForFirestore.add(answerMap);
        }

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("testId", testId);
        resultData.put("testTitle", testTitle);
        resultData.put("classId", classId);
        resultData.put("studentId", user.getUid());
        resultData.put("studentEmail", user.getEmail());
        resultData.put("score", score);
        resultData.put("correctCount", correctCount);
        resultData.put("totalQuestions", totalQuestions);
        resultData.put("answers", answersForFirestore);
        resultData.put("date", com.google.firebase.Timestamp.now());

        db.collection("testResults")
                .add(resultData)
                .addOnSuccessListener(documentReference -> {
                    // Показываем результат
                    showResultDialog(score, correctCount, totalQuestions);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка сохранения результата: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showResultDialog(int score, int correctCount, int totalQuestions) {
        String message = "Правильных ответов: " + correctCount + " из " + totalQuestions +
                "\n\nРезультат: " + score + "%";

        String emoji;
        if (score >= 90) emoji = "🏆";
        else if (score >= 70) emoji = "👍";
        else if (score >= 50) emoji = "📚";
        else emoji = "💪";

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(emoji + " Тест завершён!")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}