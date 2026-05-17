package com.example.praktika2;

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

public class EditMaterialActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextContent;
    private Button buttonSave;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private String classId, materialId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_material);

        db = DatabaseHelper.getInstance();

        classId = getIntent().getStringExtra("classId");
        materialId = getIntent().getStringExtra("materialId");
        String title = getIntent().getStringExtra("title");
        String content = getIntent().getStringExtra("content");

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextContent = findViewById(R.id.editTextContent);
        buttonSave = findViewById(R.id.buttonSave);
        progressBar = findViewById(R.id.progressBar);

        // Заполняем текущими данными
        editTextTitle.setText(title);
        editTextContent.setText(content);

        buttonSave.setOnClickListener(v -> saveChanges());
    }

    private void saveChanges() {
        String newTitle = editTextTitle.getText().toString().trim();
        String newContent = editTextContent.getText().toString().trim();

        if (TextUtils.isEmpty(newTitle)) {
            editTextTitle.setError("Введите название");
            return;
        }
        if (TextUtils.isEmpty(newContent)) {
            editTextContent.setError("Введите содержимое");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        buttonSave.setEnabled(false);

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", newTitle);
        updates.put("content", newContent);

        db.collection("classes").document(classId)
                .collection("materials").document(materialId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Изменения сохранены", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    buttonSave.setEnabled(true);
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}