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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class JoinClassActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText editTextClassCode;
    private Button buttonJoin, buttonConfirmJoin;
    private ProgressBar progressBar;
    private LinearLayout layoutClassInfo;
    private TextView textViewFoundClassName, textViewFoundCreator, textViewError;

    private String foundClassId;
    private String foundClassName;
    private String foundCreatorId;
    private List<String> currentTeacherIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_class);

        // Инициализация
        mAuth = AuthHelper.getInstance();
        db = DatabaseHelper.getInstance();

        // Привязка UI
        editTextClassCode = findViewById(R.id.editTextClassCode);
        buttonJoin = findViewById(R.id.buttonJoin);
        buttonConfirmJoin = findViewById(R.id.buttonConfirmJoin);
        progressBar = findViewById(R.id.progressBar);
        layoutClassInfo = findViewById(R.id.layoutClassInfo);
        textViewFoundClassName = findViewById(R.id.textViewFoundClassName);
        textViewFoundCreator = findViewById(R.id.textViewFoundCreator);
        textViewError = findViewById(R.id.textViewError);

        // Поиск класса по коду
        buttonJoin.setOnClickListener(v -> {
            String code = editTextClassCode.getText().toString().trim().toUpperCase();

            if (TextUtils.isEmpty(code) || code.length() != 6) {
                textViewError.setVisibility(View.VISIBLE);
                textViewError.setText("Введите код из 6 символов");
                return;
            }

            searchClassByCode(code);
        });

        // Подтверждение присоединения
        buttonConfirmJoin.setOnClickListener(v -> {
            joinToClass();
        });
    }

    private void searchClassByCode(String code) {
        showLoading(true);
        layoutClassInfo.setVisibility(View.GONE);
        textViewError.setVisibility(View.GONE);

        db.collection("classes")
                .whereEqualTo("code", code)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    showLoading(false);

                    if (queryDocumentSnapshots.isEmpty()) {
                        // Класс не найден
                        textViewError.setVisibility(View.VISIBLE);
                        textViewError.setText("Класс с таким кодом не найден");
                    } else {
                        // Берём первый (и единственный) документ
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);

                        foundClassId = document.getId();
                        foundClassName = document.getString("name");
                        foundCreatorId = document.getString("creatorId");
                        currentTeacherIds = (List<String>) document.get("teacherIds");

                        if (currentTeacherIds == null) {
                            currentTeacherIds = new ArrayList<>();
                        }

                        // Проверяем, не состоит ли уже преподаватель в этом классе
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && currentTeacherIds.contains(user.getUid())) {
                            textViewError.setVisibility(View.VISIBLE);
                            textViewError.setText("Вы уже состоите в этом классе");
                            return;
                        }

                        // Показываем информацию о классе
                        textViewFoundClassName.setText(foundClassName);

                        // Загружаем email создателя
                        db.collection("users").document(foundCreatorId)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    String creatorEmail = userDoc.getString("email");
                                    textViewFoundCreator.setText("Создатель: " + (creatorEmail != null ? creatorEmail : "неизвестно"));
                                });

                        layoutClassInfo.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    textViewError.setVisibility(View.VISIBLE);
                    textViewError.setText("Ошибка поиска: " + e.getMessage());
                });
    }

    private void joinToClass() {
        showLoading(true);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Вы не авторизованы", Toast.LENGTH_SHORT).show();
            showLoading(false);
            return;
        }

        // Добавляем текущего пользователя в teacherIds
        currentTeacherIds.add(user.getUid());

        db.collection("classes").document(foundClassId)
                .update("teacherIds", currentTeacherIds)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Log.d("FIREBASE", "Преподаватель добавлен в класс: " + foundClassId);
                    Toast.makeText(this, "Вы присоединились к классу «" + foundClassName + "»!", Toast.LENGTH_LONG).show();
                    finish(); // Возвращаемся назад
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    textViewError.setVisibility(View.VISIBLE);
                    textViewError.setText("Ошибка: " + e.getMessage());
                });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            buttonJoin.setEnabled(false);
            buttonConfirmJoin.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            buttonJoin.setEnabled(true);
            buttonConfirmJoin.setEnabled(true);
        }
    }
}