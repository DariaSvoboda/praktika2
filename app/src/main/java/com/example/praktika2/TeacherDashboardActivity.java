package com.example.praktika2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class TeacherDashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView textViewEmail, textViewRole;
    private Button buttonLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        // Инициализация
        mAuth = AuthHelper.getInstance();
        db = DatabaseHelper.getInstance();

        // Привязка UI
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewRole = findViewById(R.id.textViewRole);
        buttonLogout = findViewById(R.id.buttonLogout);
        Button buttonCreateClass = findViewById(R.id.buttonCreateClass);
        Button buttonJoinClass = findViewById(R.id.buttonJoinClass);
        Button buttonMyClasses = findViewById(R.id.buttonMyClasses);

        buttonCreateClass.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherDashboardActivity.this, CreateClassActivity.class);
            startActivity(intent);
        });

        buttonJoinClass.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherDashboardActivity.this, JoinClassActivity.class);
            startActivity(intent);
        });

        buttonMyClasses.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherDashboardActivity.this, MyClassesActivity.class);
            startActivity(intent);
        });
        // Загружаем данные пользователя
        loadUserData();

        // Кнопка выхода
        buttonLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(TeacherDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Показываем email из Auth
            textViewEmail.setText(user.getEmail());

            // Загружаем роль из Firestore
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String role = documentSnapshot.getString("role");
                            textViewRole.setText(role != null ? role : "не указана");
                        } else {
                            textViewRole.setText("не найдена");
                        }
                    })
                    .addOnFailureListener(e -> {
                        textViewRole.setText("ошибка загрузки");
                        Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}