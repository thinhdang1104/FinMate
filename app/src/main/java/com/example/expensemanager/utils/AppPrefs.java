package com.example.expensemanager.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.expensemanager.model.User;

/**
 * Lưu phiên đăng nhập và cài đặt người dùng bằng SharedPreferences.
 */
public final class AppPrefs {

    private static final String PREFS_NAME = "expense_prefs";
    private static final String KEY_REMINDER_ENABLED = "reminder_enabled";
    private static final String KEY_REMINDER_HOUR = "reminder_hour";
    private static final String KEY_REMINDER_MINUTE = "reminder_minute";

    // Phiên đăng nhập
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";

    private AppPrefs() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ------------------------- Phiên đăng nhập -------------------------

    public static void saveUserSession(Context c, User user) {
        prefs(c).edit()
                .putInt(KEY_USER_ID, user.getId())
                .putString(KEY_USER_NAME, user.getFullName())
                .putString(KEY_USER_EMAIL, user.getEmail())
                .apply();
    }

    public static void clearUserSession(Context c) {
        prefs(c).edit()
                .remove(KEY_USER_ID)
                .remove(KEY_USER_NAME)
                .remove(KEY_USER_EMAIL)
                .apply();
    }

    public static boolean isLoggedIn(Context c) {
        return getUserId(c) > 0;
    }

    public static int getUserId(Context c) {
        return prefs(c).getInt(KEY_USER_ID, -1);
    }

    public static String getUserName(Context c) {
        return prefs(c).getString(KEY_USER_NAME, "Người dùng");
    }

    public static String getUserEmail(Context c) {
        return prefs(c).getString(KEY_USER_EMAIL, "");
    }

    // ------------------------- Nhắc nhở -------------------------

    public static boolean isReminderEnabled(Context c) {
        return prefs(c).getBoolean(KEY_REMINDER_ENABLED, false);
    }

    public static void setReminderEnabled(Context c, boolean enabled) {
        prefs(c).edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply();
    }

    public static int getReminderHour(Context c) {
        return prefs(c).getInt(KEY_REMINDER_HOUR, 20);
    }

    public static int getReminderMinute(Context c) {
        return prefs(c).getInt(KEY_REMINDER_MINUTE, 0);
    }

    public static void setReminderTime(Context c, int hour, int minute) {
        prefs(c).edit().putInt(KEY_REMINDER_HOUR, hour).putInt(KEY_REMINDER_MINUTE, minute).apply();
    }
}
