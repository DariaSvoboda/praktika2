package com.example.praktika2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyClassesActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView recyclerViewClasses;
    private ProgressBar progressBar;
    private TextView textViewEmpty;
    private ClassAdapter adapter;
    private List<ClassModel> classList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_classes);

        // Инициализация
        mAuth = AuthHelper.getInstance();
        db = DatabaseHelper.getInstance();

        // Привязка UI
        recyclerViewClasses = findViewById(R.id.recyclerViewClasses);
        progressBar = findViewById(R.id.progressBar);
        textViewEmpty = findViewById(R.id.textViewEmpty);

        // Настройка RecyclerView
        classList = new ArrayList<>();
        adapter = new ClassAdapter(classList, classModel -> {
            Intent intent = new Intent(MyClassesActivity.this, ClassDetailActivity.class);
            intent.putExtra("classId", classModel.getClassId());
            intent.putExtra("className", classModel.getName());
            intent.putExtra("classCode", classModel.getCode());
            intent.putExtra("creatorId", classModel.getCreatorId());
            intent.putExtra("isCreator", classModel.isCreator());
            startActivity(intent);
        });

        recyclerViewClasses.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewClasses.setAdapter(adapter);

        // Загружаем классы
        loadClasses();
    }

    private void loadClasses() {
        showLoading(true);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Вы не авторизованы", Toast.LENGTH_SHORT).show();
            showLoading(false);
            return;
        }

        String userId = user.getUid();

        // Ищем классы, где пользователь есть в teacherIds
        db.collection("classes")
                .whereArrayContains("teacherIds", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    classList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String classId = document.getId();
                        String name = document.getString("name");
                        String code = document.getString("code");
                        String creatorId = document.getString("creatorId");
                        boolean isCreator = userId.equals(creatorId);

                        ClassModel classModel = new ClassModel(classId, name, code, creatorId, isCreator);
                        classList.add(classModel);
                    }

                    adapter.notifyDataSetChanged();
                    showLoading(false);

                    // Показываем заглушку, если список пуст
                    if (classList.isEmpty()) {
                        textViewEmpty.setVisibility(View.VISIBLE);
                        recyclerViewClasses.setVisibility(View.GONE);
                    } else {
                        textViewEmpty.setVisibility(View.GONE);
                        recyclerViewClasses.setVisibility(View.VISIBLE);
                    }

                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Ошибка загрузки: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerViewClasses.setVisibility(View.GONE);
            textViewEmpty.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }
}