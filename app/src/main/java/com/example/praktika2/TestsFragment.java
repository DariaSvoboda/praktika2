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
import java.util.Map;

public class TestsFragment extends Fragment {

    private String classId;
    private RecyclerView recyclerViewTests;
    private TextView textViewEmptyTests;
    private List<TestModel> testList;
    private TestListAdapter testListAdapter;
    private FirebaseFirestore db;

    public TestsFragment() { }

    public TestsFragment(String classId) {
        this.classId = classId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tests, container, false);

        db = DatabaseHelper.getInstance();
        textViewEmptyTests = view.findViewById(R.id.textViewEmptyTests);
        recyclerViewTests = view.findViewById(R.id.recyclerViewTests);

        testList = new ArrayList<>();
        testListAdapter = new TestListAdapter(testList, testModel -> {
            Intent intent = new Intent(getContext(), ViewTestActivity.class);
            intent.putExtra("classId", classId);
            intent.putExtra("testId", testModel.getTestId());
            intent.putExtra("title", testModel.getTitle());
            intent.putExtra("authorId", testModel.getAuthorId());
            intent.putExtra("questionCount", testModel.getQuestionCount());
            startActivity(intent);
        });

        recyclerViewTests.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewTests.setAdapter(testListAdapter);

        loadTests();

        return view;
    }

    private void loadTests() {
        db.collection("classes").document(classId)
                .collection("tests")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    testList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String testId = doc.getId();
                        String title = doc.getString("title");
                        String authorId = doc.getString("authorId");
                        int questionCount = doc.getLong("questionCount") != null ?
                                doc.getLong("questionCount").intValue() : 0;

                        TestModel test = new TestModel(testId, title, authorId, null);
                        test.setQuestionCount(questionCount);
                        testList.add(test);
                    }
                    testListAdapter.notifyDataSetChanged();
                    updateEmptyView();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Ошибка загрузки: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateEmptyView() {
        if (testList.isEmpty()) {
            textViewEmptyTests.setVisibility(View.VISIBLE);
            recyclerViewTests.setVisibility(View.GONE);
        } else {
            textViewEmptyTests.setVisibility(View.GONE);
            recyclerViewTests.setVisibility(View.VISIBLE);
        }
    }
}