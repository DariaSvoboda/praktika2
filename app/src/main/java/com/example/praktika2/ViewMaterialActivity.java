package com.example.praktika2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ViewMaterialActivity extends AppCompatActivity {

    private TextView textViewMaterialTitle, textViewMaterialType, textViewMaterialContent;
    private LinearLayout layoutActions;
    private Button buttonEdit, buttonDelete, buttonOpenLink;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String classId, materialId, title, type, content, authorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_material);

        db = DatabaseHelper.getInstance();
        mAuth = AuthHelper.getInstance();

        // Получаем данные из Intent
        classId = getIntent().getStringExtra("classId");
        materialId = getIntent().getStringExtra("materialId");
        title = getIntent().getStringExtra("title");
        type = getIntent().getStringExtra("type");
        content = getIntent().getStringExtra("content");
        authorId = getIntent().getStringExtra("authorId");

        // Привязка UI
        textViewMaterialTitle = findViewById(R.id.textViewMaterialTitle);
        textViewMaterialType = findViewById(R.id.textViewMaterialType);
        textViewMaterialContent = findViewById(R.id.textViewMaterialContent);
        layoutActions = findViewById(R.id.layoutActions);
        buttonEdit = findViewById(R.id.buttonEdit);
        buttonDelete = findViewById(R.id.buttonDelete);
        buttonOpenLink = findViewById(R.id.buttonOpenLink);

        // Заполняем данными
        textViewMaterialTitle.setText(title);
        textViewMaterialType.setText(type.equals("text") ? "Текстовый конспект" : "Ссылка на ресурс");
        textViewMaterialContent.setText(content);

        // Если это ссылка — показываем кнопку «Открыть»
        if (type.equals("link")) {
            buttonOpenLink.setVisibility(View.VISIBLE);
            buttonOpenLink.setOnClickListener(v -> {
                String url = content;
                if (!url.startsWith("http")) {
                    url = "https://" + url;
                }
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            });
        }

        // Проверяем, является ли текущий пользователь автором
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getUid().equals(authorId)) {
            layoutActions.setVisibility(View.VISIBLE);
        }

        // Редактирование
        buttonEdit.setOnClickListener(v -> {
            Intent intent = new Intent(ViewMaterialActivity.this, EditMaterialActivity.class);
            intent.putExtra("classId", classId);
            intent.putExtra("materialId", materialId);
            intent.putExtra("title", title);
            intent.putExtra("type", type);
            intent.putExtra("content", content);
            startActivity(intent);
        });

        // Удаление
        buttonDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Удалить материал")
                    .setMessage("Вы уверены, что хотите удалить «" + title + "»?")
                    .setPositiveButton("Удалить", (dialog, which) -> deleteMaterial())
                    .setNegativeButton("Отмена", null)
                    .show();
        });
    }

    private void deleteMaterial() {
        db.collection("classes").document(classId)
                .collection("materials").document(materialId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Материал удалён", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}