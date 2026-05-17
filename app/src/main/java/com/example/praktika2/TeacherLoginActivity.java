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
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class TeacherLoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin, buttonRegister;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_login);

        // Инициализация Firebase Auth
        mAuth = AuthHelper.getInstance();

        // Привязка UI
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);
        progressBar = findViewById(R.id.progressBar);

        // Кнопка "Войти"
        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (!validateInput(email, password)) {
                return;
            }

            loginUser(email, password);
        });

        // Кнопка "Зарегистрироваться"
        buttonRegister.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (!validateInput(email, password)) {
                return;
            }

            registerUser(email, password);
        });
    }

    // Проверка полей
    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Введите email");
            editTextEmail.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Введите пароль");
            editTextPassword.requestFocus();
            return false;
        }
        if (password.length() < 6) {
            editTextPassword.setError("Пароль должен быть минимум 6 символов");
            editTextPassword.requestFocus();
            return false;
        }
        return true;
    }

    // Вход
    private void loginUser(String email, String password) {
        showLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d("FIREBASE", "Вход выполнен: " + user.getUid());
                        Toast.makeText(this, "Вход выполнен!", Toast.LENGTH_SHORT).show();
                        goToDashboard();
                        // ПОЗЖЕ: переход в личный кабинет преподавателя
                        // Intent intent = new Intent(TeacherLoginActivity.this, TeacherDashboardActivity.class);
                        // startActivity(intent);
                        // finish();

                    } else {
                        Log.w("FIREBASE", "Ошибка входа", task.getException());
                        Toast.makeText(this,
                                "Ошибка: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Регистрация
    private void registerUser(String email, String password) {
        showLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d("FIREBASE", "Регистрация успешна: " + user.getUid());

                        // Сохраняем данные в Firestore
                        saveTeacherToFirestore(user.getUid(), email);

                    } else {
                        showLoading(false);
                        Log.w("FIREBASE", "Ошибка регистрации", task.getException());
                        Toast.makeText(this,
                                "Ошибка: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void saveTeacherToFirestore(String userId, String email) {
        FirebaseFirestore db = DatabaseHelper.getInstance();

        Map<String, Object> teacher = new HashMap<>();
        teacher.put("email", email);
        teacher.put("role", "teacher");
        teacher.put("createdAt", com.google.firebase.Timestamp.now());

        db.collection("users").document(userId)
                .set(teacher)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Log.d("FIREBASE", "Данные сохранены в Firestore");
                    Toast.makeText(TeacherLoginActivity.this,
                            "Регистрация успешна!",
                            Toast.LENGTH_SHORT).show();

                    // Переход в личный кабинет
                    goToDashboard();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.w("FIREBASE", "Ошибка сохранения в Firestore", e);
                    Toast.makeText(TeacherLoginActivity.this,
                            "Ошибка сохранения данных: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    // Метод для перехода в личный кабинет
    private void goToDashboard() {
        Intent intent = new Intent(TeacherLoginActivity.this, TeacherDashboardActivity.class);
        startActivity(intent);
        finish();
    }

    // Показать/скрыть ProgressBar
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            buttonLogin.setEnabled(false);
            buttonRegister.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            buttonLogin.setEnabled(true);
            buttonRegister.setEnabled(true);
        }
    }
}