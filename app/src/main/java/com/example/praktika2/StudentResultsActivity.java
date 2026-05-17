package com.example.praktika2;

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

public class StudentResultsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewResults;
    private TextView textViewEmpty;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<TestResultModel> resultList;
    private ResultAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_results);

        db = DatabaseHelper.getInstance();
        mAuth = AuthHelper.getInstance();

        recyclerViewResults = findViewById(R.id.recyclerViewResults);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        progressBar = findViewById(R.id.progressBar);

        resultList = new ArrayList<>();
        adapter = new ResultAdapter(resultList, false); // false = не показывать email ученика
        recyclerViewResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewResults.setAdapter(adapter);

        loadResults();
    }

    private void loadResults() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Вы не авторизованы", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        textViewEmpty.setVisibility(View.GONE);

        db.collection("testResults")
                .whereEqualTo("studentId", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    resultList.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        textViewEmpty.setVisibility(View.VISIBLE);
                        recyclerViewResults.setVisibility(View.GONE);
                    } else {
                        textViewEmpty.setVisibility(View.GONE);
                        recyclerViewResults.setVisibility(View.VISIBLE);

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            TestResultModel result = new TestResultModel();
                            result.setResultId(doc.getId());
                            result.setTestTitle(doc.getString("testTitle"));

                            Long scoreLong = doc.getLong("score");
                            result.setScore(scoreLong != null ? scoreLong.intValue() : 0);

                            Long correctLong = doc.getLong("correctCount");
                            result.setCorrectCount(correctLong != null ? correctLong.intValue() : 0);

                            Long totalLong = doc.getLong("totalQuestions");
                            result.setTotalQuestions(totalLong != null ? totalLong.intValue() : 0);

                            result.setDate(doc.getTimestamp("date"));
                            result.setStudentEmail(doc.getString("studentEmail"));

                            resultList.add(result);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    // Показываем количество найденных результатов
                    if (!resultList.isEmpty()) {
                        Toast.makeText(StudentResultsActivity.this,
                                "Загружено результатов: " + resultList.size(),
                                Toast.LENGTH_SHORT).show();
                    }

                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    textViewEmpty.setVisibility(View.VISIBLE);
                    recyclerViewResults.setVisibility(View.GONE);
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
    }
