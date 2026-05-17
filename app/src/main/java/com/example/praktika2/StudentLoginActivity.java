package com.example.praktika2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentLoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText editTextEmail, editTextPassword, editTextClassCode;
    private Button buttonLogin, buttonRegister;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_login);

        mAuth = AuthHelper.getInstance();
        db = DatabaseHelper.getInstance();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextClassCode = findViewById(R.id.editTextClassCode);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);
        progressBar = findViewById(R.id.progressBar);

        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (!validateEmailAndPassword(email, password)) return;

            loginStudent(email, password);
        });

        buttonRegister.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String classCode = editTextClassCode.getText().toString().trim().toUpperCase();

            if (!validateEmailAndPassword(email, password)) return;

            if (TextUtils.isEmpty(classCode) || classCode.length() != 6) {
                editTextClassCode.setError("Введите код класса из 6 символов");
                return;
            }

            registerStudent(email, password, classCode);
        });
    }

    private boolean validateEmailAndPassword(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Введите email");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Введите пароль");
            return false;
        }
        if (password.length() < 6) {
            editTextPassword.setError("Минимум 6 символов");
            return false;
        }
        return true;
    }

    private void loginStudent(String email, String password) {
        showLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d("FIREBASE", "Вход ученика: " + user.getUid());
                        goToStudentDashboard();
                    } else {
                        Toast.makeText(this,
                                "Ошибка входа: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void registerStudent(String email, String password, String classCode) {
        showLoading(true);

        // Сначала ищем класс по коду
        db.collection("classes")
                .whereEqualTo("code", classCode)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        showLoading(false);
                        editTextClassCode.setError("Класс с таким кодом не найден");
                        return;
                    }

                    // Класс найден, получаем его данные
                    QueryDocumentSnapshot classDoc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                    String classId = classDoc.getId();
                    String className = classDoc.getString("name");

                    // Регистрируем пользователя
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    // Сохраняем ученика в Firestore
                                    saveStudentToFirestore(user.getUid(), email, classId, className);
                                } else {
                                    showLoading(false);
                                    Toast.makeText(StudentLoginActivity.this,
                                            "Ошибка регистрации: " + task.getException().getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            });

                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveStudentToFirestore(String userId, String email, String classId, String className) {
        // Данные ученика
        Map<String, Object> studentData = new HashMap<>();
        studentData.put("email", email);
        studentData.put("role", "student");
        studentData.put("classId", classId);
        studentData.put("className", className);

        db.collection("users").document(userId)
                .set(studentData)
                .addOnSuccessListener(aVoid -> {
                    // Добавляем ученика в список studentIds класса
                    addStudentToClass(classId, userId);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Ошибка сохранения: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addStudentToClass(String classId, String userId) {
        // Получаем текущий список учеников
        db.collection("classes").document(classId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> studentIds = (List<String>) documentSnapshot.get("studentIds");
                    if (studentIds == null) {
                        studentIds = new ArrayList<>();
                    }

                    if (!studentIds.contains(userId)) {
                        studentIds.add(userId);
                    }

                    // Обновляем
                    db.collection("classes").document(classId)
                            .update("studentIds", studentIds)
                            .addOnSuccessListener(aVoid -> {
                                showLoading(false);
                                Toast.makeText(StudentLoginActivity.this,
                                        "Регистрация успешна!",
                                        Toast.LENGTH_SHORT).show();
                                goToStudentDashboard();
                            })
                            .addOnFailureListener(e -> {
                                showLoading(false);
                                Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                });
    }

    private void goToStudentDashboard() {
        Intent intent = new Intent(StudentLoginActivity.this, StudentDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        buttonLogin.setEnabled(!isLoading);
        buttonRegister.setEnabled(!isLoading);
    }
}