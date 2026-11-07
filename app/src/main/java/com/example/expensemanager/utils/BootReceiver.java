package com.example.expensemanager.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Đặt lại lịch nhắc nhở sau khi thiết bị khởi động lại (AlarmManager bị xóa khi reboot).
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())
                && AppPrefs.isReminderEnabled(context)) {
            NotificationHelper.scheduleDailyReminder(context);
        }
    }
}
