package com.example.expensemanager.utils;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.expensemanager.R;
import com.example.expensemanager.database.BudgetDAO;
import com.example.expensemanager.database.TransactionDAO;
import com.example.expensemanager.model.Transaction;
import com.example.expensemanager.ui.MainActivity;

import java.util.Calendar;

/**
 * Tạo kênh thông báo, hẹn giờ nhắc nhở hằng ngày (AlarmManager) và cảnh báo vượt ngân sách.
 */
public final class NotificationHelper {

    public static final String CHANNEL_REMINDER = "channel_reminder";
    public static final String CHANNEL_BUDGET = "channel_budget";

    private static final int NOTI_REMINDER_ID = 1001;
    private static final int NOTI_BUDGET_ID = 1002;
    private static final int REQ_ALARM = 2001;

    private NotificationHelper() {
    }

    public static void createChannels(Context context) {
        NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm == null) return;
        nm.createNotificationChannel(new NotificationChannel(CHANNEL_REMINDER,
                "Nhắc nhở ghi chép", NotificationManager.IMPORTANCE_DEFAULT));
        nm.createNotificationChannel(new NotificationChannel(CHANNEL_BUDGET,
                "Cảnh báo ngân sách", NotificationManager.IMPORTANCE_HIGH));
    }

    public static boolean canPostNotifications(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private static PendingIntent openAppIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static void notify(Context context, String channel, int id, String title, String text) {
        if (!canPostNotifications(context)) return;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setColor(ContextCompat.getColor(context, R.color.primary))
                .setContentIntent(openAppIntent(context))
                .setAutoCancel(true);
        try {
            NotificationManagerCompat.from(context).notify(id, builder.build());
        } catch (SecurityException ignored) {
            // Người dùng đã thu hồi quyền thông báo
        }
    }

    public static void showReminder(Context context) {
        notify(context, CHANNEL_REMINDER, NOTI_REMINDER_ID,
                "Đã ghi chép chi tiêu hôm nay chưa?",
                "Dành 1 phút ghi lại các khoản thu chi để quản lý tài chính tốt hơn nhé.");
    }

    // ------------------------- Nhắc nhở hằng ngày -------------------------

    private static PendingIntent reminderPendingIntent(Context context) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        return PendingIntent.getBroadcast(context, REQ_ALARM, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    public static void scheduleDailyReminder(Context context) {
        int hour = AppPrefs.getReminderHour(context);
        int minute = AppPrefs.getReminderMinute(context);
        Calendar time = Calendar.getInstance();
        time.set(Calendar.HOUR_OF_DAY, hour);
        time.set(Calendar.MINUTE, minute);
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MILLISECOND, 0);
        if (time.getTimeInMillis() <= System.currentTimeMillis()) {
            time.add(Calendar.DAY_OF_YEAR, 1);
        }
        AlarmManager am = context.getSystemService(AlarmManager.class);
        if (am == null) return;
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, reminderPendingIntent(context));
    }

    public static void cancelDailyReminder(Context context) {
        AlarmManager am = context.getSystemService(AlarmManager.class);
        if (am != null) am.cancel(reminderPendingIntent(context));
    }

    // ------------------------- Cảnh báo ngân sách -------------------------

    /**
     * Kiểm tra ngân sách tháng chứa ngày {@code dateMillis}.
     *
     * @return thông báo cảnh báo (để hiển thị Toast) hoặc null nếu chưa tới ngưỡng.
     */
    public static String checkBudget(Context context, long dateMillis) {
        String key = DateUtils.monthKey(dateMillis);
        double limit = new BudgetDAO(context).getLimit(key);
        if (limit <= 0) return null;

        Calendar month = Calendar.getInstance();
        month.setTimeInMillis(dateMillis);
        double spent = new TransactionDAO(context).sumByRange(
                DateUtils.startOfMonth(month), DateUtils.endOfMonth(month), Transaction.TYPE_EXPENSE);
        double percent = spent * 100 / limit;

        String title;
        String text;
        if (percent >= 100) {
            title = "Bạn đã vượt ngân sách " + DateUtils.monthTitle(month).toLowerCase(DateUtils.VI);
            text = "Đã chi " + CurrencyUtils.format(spent) + " / " + CurrencyUtils.format(limit)
                    + " (vượt " + CurrencyUtils.format(spent - limit) + ").";
        } else if (percent >= 80) {
            title = "Sắp chạm hạn mức ngân sách";
            text = "Bạn đã dùng " + Math.round(percent) + "% ngân sách. Còn lại "
                    + CurrencyUtils.format(limit - spent) + ".";
        } else {
            return null;
        }
        notify(context, CHANNEL_BUDGET, NOTI_BUDGET_ID, title, text);
        return title + ". " + text;
    }
}
