package com.example.expensemanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.expensemanager.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import static com.example.expensemanager.database.DatabaseHelper.*;

/**
 * Data Access Object cho bảng users: Đăng ký, Đăng nhập, Đổi mật khẩu (băm SHA-256).
 */
public class UserDAO {

    private final DatabaseHelper dbHelper;

    public UserDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /** Băm mật khẩu bằng thuật toán SHA-256. */
    public static String hashPassword(String plainText) {
        if (plainText == null) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format(Locale.US, "%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(plainText.hashCode());
        }
    }

    /** Kiểm tra email đã tồn tại trong CSDL chưa. */
    public boolean isEmailExists(String email) {
        if (email == null) return false;
        String normalized = email.trim().toLowerCase(Locale.US);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(TABLE_USERS, new String[]{COL_USER_ID},
                "LOWER(" + COL_USER_EMAIL + ") = ?", new String[]{normalized},
                null, null, null)) {
            return c.moveToFirst();
        }
    }

    /**
     * Đăng ký tài khoản mới.
     *
     * @return đối tượng {@link User} vừa tạo, hoặc {@code null} nếu email đã tồn tại / lỗi.
     */
    public User register(String fullName, String email, String plainPassword) {
        String normalizedEmail = email.trim().toLowerCase(Locale.US);
        if (isEmailExists(normalizedEmail)) return null;

        long now = System.currentTimeMillis();
        String hash = hashPassword(plainPassword);

        ContentValues v = new ContentValues();
        v.put(COL_USER_NAME, fullName.trim());
        v.put(COL_USER_EMAIL, normalizedEmail);
        v.put(COL_USER_PASSWORD, hash);
        v.put(COL_USER_CREATED_AT, now);

        long id = dbHelper.getWritableDatabase().insert(TABLE_USERS, null, v);
        if (id <= 0) return null;
        return new User((int) id, fullName.trim(), normalizedEmail, hash, now);
    }

    /**
     * Xác thực đăng nhập bằng email và mật khẩu.
     *
     * @return {@link User} nếu đúng thông tin, ngược lại trả về {@code null}.
     */
    public User login(String email, String plainPassword) {
        if (email == null || plainPassword == null) return null;
        String normalizedEmail = email.trim().toLowerCase(Locale.US);
        String hash = hashPassword(plainPassword);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(TABLE_USERS, null,
                "LOWER(" + COL_USER_EMAIL + ") = ? AND " + COL_USER_PASSWORD + " = ?",
                new String[]{normalizedEmail, hash}, null, null, null)) {
            if (c.moveToFirst()) {
                return fromCursor(c);
            }
        }
        return null;
    }

    public User getById(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(TABLE_USERS, null, COL_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}, null, null, null)) {
            if (c.moveToFirst()) {
                return fromCursor(c);
            }
        }
        return null;
    }

    /** Đổi mật khẩu (kiểm tra mật khẩu hiện tại trước khi cập nhật). */
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) return false;
        if (!user.getPasswordHash().equals(hashPassword(oldPassword))) {
            return false;
        }
        ContentValues v = new ContentValues();
        v.put(COL_USER_PASSWORD, hashPassword(newPassword));
        int rows = dbHelper.getWritableDatabase().update(TABLE_USERS, v,
                COL_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    private User fromCursor(Cursor c) {
        return new User(
                c.getInt(c.getColumnIndexOrThrow(COL_USER_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_USER_NAME)),
                c.getString(c.getColumnIndexOrThrow(COL_USER_EMAIL)),
                c.getString(c.getColumnIndexOrThrow(COL_USER_PASSWORD)),
                c.getLong(c.getColumnIndexOrThrow(COL_USER_CREATED_AT)));
    }
}
