package com.example.expensemanager.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Nhận sự kiện từ AlarmManager và hiển thị thông báo nhắc ghi chép chi tiêu.
 */
public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (AppPrefs.isReminderEnabled(context)) {
            NotificationHelper.createChannels(context);
            NotificationHelper.showReminder(context);
        }
    }
}
