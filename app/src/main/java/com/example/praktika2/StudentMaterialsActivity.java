package com.example.praktika2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StudentMaterialsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMaterials;
    private TextView textViewEmpty;
    private FirebaseFirestore db;
    private String classId;
    private List<MaterialModel> materialList;
    private MaterialAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_materials);

        db = DatabaseHelper.getInstance();
        classId = getIntent().getStringExtra("classId");

        recyclerViewMaterials = findViewById(R.id.recyclerViewMaterials);
        textViewEmpty = findViewById(R.id.textViewEmpty);

        materialList = new ArrayList<>();
        adapter = new MaterialAdapter(materialList, material -> {
            Intent intent = new Intent(StudentMaterialsActivity.this, ViewMaterialActivity.class);
            intent.putExtra("classId", classId);
            intent.putExtra("materialId", material.getMaterialId());
            intent.putExtra("title", material.getTitle());
            intent.putExtra("type", material.getType());
            intent.putExtra("content", material.getContent());
            intent.putExtra("authorId", material.getAuthorId());
            startActivity(intent);
        });

        recyclerViewMaterials.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMaterials.setAdapter(adapter);

        loadMaterials();
    }

    private void loadMaterials() {
        db.collection("classes").document(classId)
                .collection("materials")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    materialList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        MaterialModel material = new MaterialModel();
                        material.setMaterialId(doc.getId());
                        material.setTitle(doc.getString("title"));
                        material.setType(doc.getString("type"));
                        material.setContent(doc.getString("content"));
                        material.setAuthorId(doc.getString("authorId"));
                        materialList.add(material);
                    }
                    adapter.notifyDataSetChanged();

                    if (materialList.isEmpty()) {
                        textViewEmpty.setVisibility(View.VISIBLE);
                        recyclerViewMaterials.setVisibility(View.GONE);
                    } else {
                        textViewEmpty.setVisibility(View.GONE);
                        recyclerViewMaterials.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки", Toast.LENGTH_SHORT).show();
                });
    }
}