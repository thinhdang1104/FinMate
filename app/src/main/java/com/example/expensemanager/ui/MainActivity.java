package com.example.expensemanager.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.expensemanager.R;
import com.example.expensemanager.ui.fragments.HomeFragment;
import com.example.expensemanager.ui.fragments.SettingsFragment;
import com.example.expensemanager.ui.fragments.StatisticsFragment;
import com.example.expensemanager.ui.fragments.TransactionListFragment;
import com.example.expensemanager.utils.AppPrefs;
import com.example.expensemanager.utils.NotificationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Activity chính chứa thanh điều hướng dưới đáy và các Fragment.
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Chuyển về màn hình đăng nhập nếu chưa có phiên hợp lệ
        if (!AppPrefs.isLoggedIn(this)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        NotificationHelper.createChannels(this);

        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_add) {
                startActivity(new Intent(this, AddEditTransactionActivity.class));
                return false;
            }
            Fragment fragment;
            if (id == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (id == R.id.nav_transactions) {
                fragment = new TransactionListFragment();
            } else if (id == R.id.nav_statistics) {
                fragment = new StatisticsFragment();
            } else {
                fragment = new SettingsFragment();
            }
            showFragment(fragment);
            return true;
        });
        bottomNav.setOnItemReselectedListener(item -> { /* bỏ qua khi chọn lại tab hiện tại */ });

        if (savedInstanceState == null) {
            showFragment(new HomeFragment());
        }
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    /** Cho phép Fragment chuyển sang tab khác (VD: "Xem tất cả" ở trang chủ). */
    public void navigateTo(int menuItemId) {
        bottomNav.setSelectedItemId(menuItemId);
    }
}
