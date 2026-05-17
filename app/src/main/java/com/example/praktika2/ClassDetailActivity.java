package com.example.praktika2;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ClassDetailActivity extends AppCompatActivity {

    private TextView textViewDetailClassName, textViewDetailCode, textViewDetailRole;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    private String classId, className, classCode, creatorId;
    private boolean isCreator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_detail);

        // Получаем данные из Intent
        classId = getIntent().getStringExtra("classId");
        className = getIntent().getStringExtra("className");
        classCode = getIntent().getStringExtra("classCode");
        creatorId = getIntent().getStringExtra("creatorId");
        isCreator = getIntent().getBooleanExtra("isCreator", false);

        // Привязка UI
        textViewDetailClassName = findViewById(R.id.textViewDetailClassName);
        textViewDetailCode = findViewById(R.id.textViewDetailCode);
        textViewDetailRole = findViewById(R.id.textViewDetailRole);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // Заполняем данные
        textViewDetailClassName.setText(className);
        textViewDetailCode.setText(classCode);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getUid().equals(creatorId)) {
            textViewDetailRole.setText("Создатель");
            textViewDetailRole.setTextColor(getColor(android.R.color.holo_green_light));
        } else {
            textViewDetailRole.setText("Соавтор");
            textViewDetailRole.setTextColor(getColor(android.R.color.holo_blue_light));
        }

        // Настройка ViewPager с вкладками
        ViewPagerAdapter adapter = new ViewPagerAdapter(this, classId);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("Тесты");
                    } else {
                        tab.setText("Материалы");
                    }
                }).attach();
        Button buttonCreateTest = findViewById(R.id.buttonCreateTest);
        Button buttonAddMaterial = findViewById(R.id.buttonAddMaterial);

        buttonCreateTest.setOnClickListener(v -> {
            Intent intent = new Intent(ClassDetailActivity.this, CreateTestActivity.class);
            intent.putExtra("classId", classId);
            startActivity(intent);
        });
        Button buttonViewResults = findViewById(R.id.buttonViewResults);
        buttonViewResults.setOnClickListener(v -> {
            Intent intent = new Intent(ClassDetailActivity.this, TeacherResultsActivity.class);
            intent.putExtra("classId", classId);
            startActivity(intent);
        });
        buttonAddMaterial.setOnClickListener(v -> {
            Intent intent = new Intent(ClassDetailActivity.this, AddMaterialActivity.class);
            intent.putExtra("classId", classId);
            startActivity(intent);
        });

    }

    // ====== ДОБАВЬ ЭТИ ДВА МЕТОДА ======

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_class_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_create_test) {
            Intent intent = new Intent(ClassDetailActivity.this, CreateTestActivity.class);
            intent.putExtra("classId", classId);
            startActivity(intent);
            return true;
        }
        if (item.getItemId() == R.id.action_add_material) {
            Intent intent = new Intent(ClassDetailActivity.this, AddMaterialActivity.class);
            intent.putExtra("classId", classId);
            startActivity(intent);
            return true;
        }
        if (item.getItemId() == R.id.action_view_results) {
            Intent intent = new Intent(ClassDetailActivity.this, TeacherResultsActivity.class);
            intent.putExtra("classId", classId);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}