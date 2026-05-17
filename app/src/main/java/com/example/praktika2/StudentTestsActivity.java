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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StudentTestsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewTests;
    private TextView textViewEmpty;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private String classId;
    private List<TestModel> testList;
    private TestListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_tests);

        db = DatabaseHelper.getInstance();
        classId = getIntent().getStringExtra("classId");

        recyclerViewTests = findViewById(R.id.recyclerViewTests);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        progressBar = findViewById(R.id.progressBar);

        testList = new ArrayList<>();
        adapter = new TestListAdapter(testList, testModel -> {
            // Переход к прохождению теста
            Intent intent = new Intent(StudentTestsActivity.this, TakeTestActivity.class);
            intent.putExtra("classId", classId);
            intent.putExtra("testId", testModel.getTestId());
            intent.putExtra("title", testModel.getTitle());
            intent.putExtra("questionCount", testModel.getQuestionCount());
            startActivity(intent);
        });

        recyclerViewTests.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTests.setAdapter(adapter);

        loadTests();
    }

    private void loadTests() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("classes").document(classId)
                .collection("tests")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
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

                    adapter.notifyDataSetChanged();

                    if (testList.isEmpty()) {
                        textViewEmpty.setVisibility(View.VISIBLE);
                        recyclerViewTests.setVisibility(View.GONE);
                    } else {
                        textViewEmpty.setVisibility(View.GONE);
                        recyclerViewTests.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Ошибка загрузки: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}