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
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Màn hình Đăng ký tài khoản mới.
 */
public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilEmail, tilPassword, tilConfirm;
    private TextInputEditText etName, etEmail, etPassword, etConfirm;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userDAO = new UserDAO(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tilName = findViewById(R.id.til_name);
        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        tilConfirm = findViewById(R.id.til_confirm);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirm = findViewById(R.id.et_confirm);

        findViewById(R.id.btn_register).setOnClickListener(v -> handleRegister());
        findViewById(R.id.tv_go_login).setOnClickListener(v -> finish());
    }

    private void handleRegister() {
        String name = textOf(etName);
        String email = textOf(etEmail);
        String password = textOf(etPassword);
        String confirm = textOf(etConfirm);

        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirm.setError(null);

        if (name.isEmpty()) {
            tilName.setError("Vui lòng nhập họ và tên");
            etName.requestFocus();
            return;
        }
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
        if (password.length() < 6) {
            tilPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etPassword.requestFocus();
            return;
        }
        if (!password.equals(confirm)) {
            tilConfirm.setError("Mật khẩu xác nhận không khớp");
            etConfirm.requestFocus();
            return;
        }
        if (userDAO.isEmailExists(email)) {
            tilEmail.setError("Email này đã được đăng ký");
            etEmail.requestFocus();
            return;
        }

        User user = userDAO.register(name, email, password);
        if (user == null) {
            Toast.makeText(this, "Không thể tạo tài khoản, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tự động đăng nhập ngay sau khi đăng ký thành công
        AppPrefs.saveUserSession(this, user);
        Toast.makeText(this, "Đăng ký tài khoản thành công!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private static String textOf(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}
