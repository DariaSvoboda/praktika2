package com.example.praktika2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditTestActivity extends AppCompatActivity {

    private EditText editTextTitle;
    private Button buttonUpdateTitle, buttonManageQuestions;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private String classId, testId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_test);

        db = DatabaseHelper.getInstance();

        classId = getIntent().getStringExtra("classId");
        testId = getIntent().getStringExtra("testId");
        String title = getIntent().getStringExtra("title");

        editTextTitle = findViewById(R.id.editTextTitle);
        buttonUpdateTitle = findViewById(R.id.buttonUpdateTitle);
        buttonManageQuestions = findViewById(R.id.buttonManageQuestions);
        progressBar = findViewById(R.id.progressBar);

        editTextTitle.setText(title);

        buttonUpdateTitle.setOnClickListener(v -> updateTitle());

        buttonManageQuestions.setOnClickListener(v -> {
            Intent intent = new Intent(EditTestActivity.this, EditTestQuestionsActivity.class);
            intent.putExtra("classId", classId);
            intent.putExtra("testId", testId);
            intent.putExtra("title", editTextTitle.getText().toString().trim());
            startActivity(intent);
        });
    }

    private void updateTitle() {
        String newTitle = editTextTitle.getText().toString().trim();

        if (TextUtils.isEmpty(newTitle)) {
            editTextTitle.setError("Введите название");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        buttonUpdateTitle.setEnabled(false);

        Map<String, Object> update = new HashMap<>();
        update.put("title", newTitle);

        db.collection("classes").document(classId)
                .collection("tests").document(testId)
                .update(update)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    buttonUpdateTitle.setEnabled(true);
                    Toast.makeText(this, "Название обновлено", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    buttonUpdateTitle.setEnabled(true);
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}