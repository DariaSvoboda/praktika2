package com.example.praktika2;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private String classId;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, String classId) {
        super(fragmentActivity);
        this.classId = classId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new TestsFragment(classId);
            case 1:
                return new MaterialsFragment(classId);
            default:
                return new TestsFragment(classId);
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Две вкладки: Тесты и Материалы
    }
}