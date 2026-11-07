package com.example.expensemanager.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Lưu cài đặt người dùng bằng SharedPreferences.
 */
public final class AppPrefs {

    private static final String PREFS_NAME = "expense_prefs";
    private static final String KEY_REMINDER_ENABLED = "reminder_enabled";
    private static final String KEY_REMINDER_HOUR = "reminder_hour";
    private static final String KEY_REMINDER_MINUTE = "reminder_minute";

    private AppPrefs() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

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
