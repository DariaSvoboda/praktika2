package com.example.praktika2;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddMaterialActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RadioGroup radioGroupType;
    private EditText editTextTitle, editTextContent;
    private Button buttonSaveMaterial;
    private ProgressBar progressBar;
    private String classId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_material);

        mAuth = AuthHelper.getInstance();
        db = DatabaseHelper.getInstance();
        classId = getIntent().getStringExtra("classId");

        radioGroupType = findViewById(R.id.radioGroupType);
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextContent = findViewById(R.id.editTextContent);
        buttonSaveMaterial = findViewById(R.id.buttonSaveMaterial);
        progressBar = findViewById(R.id.progressBar);

        // Меняем подсказку при выборе типа
        radioGroupType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioText) {
                editTextContent.setHint("Введите текст конспекта...");
            } else {
                editTextContent.setHint("Введите ссылку (https://...)");
            }
        });

        buttonSaveMaterial.setOnClickListener(v -> saveMaterial());
    }

    private void saveMaterial() {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            editTextTitle.setError("Введите название");
            return;
        }

        if (TextUtils.isEmpty(content)) {
            editTextContent.setError("Введите содержимое");
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        // Определяем тип материала
        boolean isText = radioGroupType.getCheckedRadioButtonId() == R.id.radioText;
        String type = isText ? "text" : "link";

        // Если ссылка — проверяем формат
        if (!isText && !content.startsWith("http")) {
            editTextContent.setError("Ссылка должна начинаться с http:// или https://");
            return;
        }

        showLoading(true);

        Map<String, Object> materialData = new HashMap<>();
        materialData.put("title", title);
        materialData.put("type", type);
        materialData.put("content", content);
        materialData.put("authorId", user.getUid());

        db.collection("classes").document(classId)
                .collection("materials")
                .add(materialData)
                .addOnSuccessListener(documentReference -> {
                    showLoading(false);
                    Toast.makeText(this, "Материал добавлен!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        buttonSaveMaterial.setEnabled(!isLoading);
    }
}