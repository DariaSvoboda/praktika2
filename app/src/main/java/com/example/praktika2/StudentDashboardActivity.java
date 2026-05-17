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

public class StudentDashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView textViewEmail, textViewClass;
    private Button buttonViewTests, buttonViewMaterials, buttonMyResults, buttonLogout;

    private String classId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        mAuth = AuthHelper.getInstance();
        db = DatabaseHelper.getInstance();

        textViewEmail = findViewById(R.id.textViewEmail);
        textViewClass = findViewById(R.id.textViewClass);
        buttonViewTests = findViewById(R.id.buttonViewTests);
        buttonViewMaterials = findViewById(R.id.buttonViewMaterials);
        buttonMyResults = findViewById(R.id.buttonMyResults);
        buttonLogout = findViewById(R.id.buttonLogout);

        loadStudentData();

        buttonViewTests.setOnClickListener(v -> {
            if (classId != null) {
                Intent intent = new Intent(StudentDashboardActivity.this, StudentTestsActivity.class);
                intent.putExtra("classId", classId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Класс не найден", Toast.LENGTH_SHORT).show();
            }
        });

        buttonViewMaterials.setOnClickListener(v -> {
            if (classId != null) {
                Intent intent = new Intent(StudentDashboardActivity.this, StudentMaterialsActivity.class);
                intent.putExtra("classId", classId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Класс не найден", Toast.LENGTH_SHORT).show();
            }
        });

        buttonMyResults.setOnClickListener(v -> {
            Intent intent = new Intent(StudentDashboardActivity.this, StudentResultsActivity.class);
            startActivity(intent);
        });

        buttonLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(StudentDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadStudentData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            textViewEmail.setText(user.getEmail());

            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String className = documentSnapshot.getString("className");
                            classId = documentSnapshot.getString("classId");
                            textViewClass.setText(className != null ? className : "Не указан");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}