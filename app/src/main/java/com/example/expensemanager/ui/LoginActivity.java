package com.example.expensemanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expensemanager.R;
import com.example.expensemanager.database.UserDAO;
import com.example.expensemanager.model.User;
import com.example.expensemanager.utils.AppPrefs;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Màn hình Đăng nhập (Launcher Activity).
 * Nếu người dùng đã đăng nhập từ trước, tự động chuyển thẳng vào MainActivity.
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Kiểm tra phiên đăng nhập đã lưu
        if (AppPrefs.isLoggedIn(this)) {
            goToMain();
            return;
        }

        setContentView(R.layout.activity_login);
        userDAO = new UserDAO(this);

        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);

        findViewById(R.id.btn_login).setOnClickListener(v -> handleLogin());

        // Nút điền nhanh tài khoản Demo phục vụ bảo vệ đồ án / tiểu luận
        findViewById(R.id.btn_demo_fill).setOnClickListener(v -> {
            etEmail.setText("sv@finmate.vn");
            etPassword.setText("123456");
            tilEmail.setError(null);
            tilPassword.setError(null);
        });

        findViewById(R.id.tv_go_register).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void handleLogin() {
        String email = textOf(etEmail);
        String password = textOf(etPassword);

        tilEmail.setError(null);
        tilPassword.setError(null);

        if (email.isEmpty()) {
            tilEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Định dạng email không hợp lệ");
            etEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            tilPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return;
        }

        User user = userDAO.login(email, password);
        if (user == null) {
            tilPassword.setError("Email hoặc mật khẩu không chính xác");
            return;
        }

        AppPrefs.saveUserSession(this, user);
        Toast.makeText(this, "Xin chào, " + user.getFullName() + "!", Toast.LENGTH_SHORT).show();
        goToMain();
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private static String textOf(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}
