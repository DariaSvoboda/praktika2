package com.example.praktika2;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MaterialsFragment extends Fragment {

    private String classId;
    private RecyclerView recyclerViewMaterials;
    private TextView textViewEmptyMaterials;
    private List<MaterialModel> materialList;
    private MaterialAdapter materialAdapter;
    private FirebaseFirestore db;

    public MaterialsFragment() { }

    public MaterialsFragment(String classId) {
        this.classId = classId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_materials, container, false);

        db = DatabaseHelper.getInstance();
        textViewEmptyMaterials = view.findViewById(R.id.textViewEmptyMaterials);
        recyclerViewMaterials = view.findViewById(R.id.recyclerViewMaterials);

        materialList = new ArrayList<>();
        materialAdapter = new MaterialAdapter(materialList, material -> {
            Intent intent = new Intent(getContext(), ViewMaterialActivity.class);
            intent.putExtra("classId", classId);
            intent.putExtra("materialId", material.getMaterialId());
            intent.putExtra("title", material.getTitle());
            intent.putExtra("type", material.getType());
            intent.putExtra("content", material.getContent());
            intent.putExtra("authorId", material.getAuthorId());
            startActivity(intent);
        });

        recyclerViewMaterials.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewMaterials.setAdapter(materialAdapter);

        loadMaterials();

        return view;
    }

    private void loadMaterials() {
        db.collection("classes").document(classId)
                .collection("materials")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    materialList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String materialId = doc.getId();
                        String title = doc.getString("title");
                        String type = doc.getString("type");
                        String content = doc.getString("content");
                        String authorId = doc.getString("authorId");

                        MaterialModel material = new MaterialModel(materialId, type, title, content, authorId);
                        materialList.add(material);
                    }
                    materialAdapter.notifyDataSetChanged();
                    updateEmptyView();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Ошибка загрузки: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateEmptyView() {
        if (materialList.isEmpty()) {
            textViewEmptyMaterials.setVisibility(View.VISIBLE);
            recyclerViewMaterials.setVisibility(View.GONE);
        } else {
            textViewEmptyMaterials.setVisibility(View.GONE);
            recyclerViewMaterials.setVisibility(View.VISIBLE);
        }
    }
}