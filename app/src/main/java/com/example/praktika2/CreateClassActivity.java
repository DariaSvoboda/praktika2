package com.example.praktika2;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CreateClassActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText editTextClassName;
    private Button buttonCreate;
    private LinearLayout layoutCode;
    private TextView textViewCode;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_class);

        // Инициализация
        mAuth = AuthHelper.getInstance();
        db = DatabaseHelper.getInstance();

        // Привязка UI
        editTextClassName = findViewById(R.id.editTextClassName);
        buttonCreate = findViewById(R.id.buttonCreate);
        layoutCode = findViewById(R.id.layoutCode);
        textViewCode = findViewById(R.id.textViewCode);
        progressBar = findViewById(R.id.progressBar);

        // Кнопка создания класса
        buttonCreate.setOnClickListener(v -> {
            String className = editTextClassName.getText().toString().trim();

            if (TextUtils.isEmpty(className)) {
                editTextClassName.setError("Введите название класса");
                editTextClassName.requestFocus();
                return;
            }

            createClass(className);
        });
    }

    private void createClass(String className) {
        showLoading(true);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Вы не авторизованы", Toast.LENGTH_SHORT).show();
            showLoading(false);
            return;
        }

        // Генерируем уникальный код
        String classCode = generateClassCode();

        // Данные класса
        Map<String, Object> classData = new HashMap<>();
        classData.put("name", className);
        classData.put("code", classCode);
        classData.put("creatorId", user.getUid());
        classData.put("teacherIds", Arrays.asList(user.getUid()));  // создатель сразу в списке преподавателей
        classData.put("createdAt", Timestamp.now());

        // Сохраняем в Firestore
        db.collection("classes")
                .add(classData)
                .addOnSuccessListener(documentReference -> {
                    String classId = documentReference.getId();
                    Log.d("FIREBASE", "Класс создан: " + classId + ", код: " + classCode);

                    showLoading(false);

                    // Показываем код
                    layoutCode.setVisibility(View.VISIBLE);
                    textViewCode.setText(classCode);

                    // Меняем кнопку
                    buttonCreate.setText("Класс создан!");
                    buttonCreate.setEnabled(false);
                    buttonCreate.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(android.R.color.darker_gray)));

                    Toast.makeText(this, "Класс успешно создан!", Toast.LENGTH_SHORT).show();

                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.w("FIREBASE", "Ошибка создания класса", e);
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // Генерация случайного 6-значного кода
    private String generateClassCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }

        return code.toString();
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            buttonCreate.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            buttonCreate.setEnabled(true);
        }
    }
}