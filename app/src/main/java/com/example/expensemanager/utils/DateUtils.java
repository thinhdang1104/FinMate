package com.example.expensemanager.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Các hàm xử lý ngày tháng.
 */
public final class DateUtils {

    public static final Locale VI = new Locale("vi", "VN");

    private DateUtils() {
    }

    public static long startOfMonth(Calendar month) {
        Calendar c = (Calendar) month.clone();
        c.set(Calendar.DAY_OF_MONTH, 1);
        resetTime(c);
        return c.getTimeInMillis();
    }

    public static long endOfMonth(Calendar month) {
        Calendar c = (Calendar) month.clone();
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTimeInMillis();
    }

    private static void resetTime(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }

    /** Khóa tháng dạng MM-yyyy dùng cho bảng budgets. */
    public static String monthKey(Calendar c) {
        return String.format(Locale.US, "%02d-%04d", c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR));
    }

    public static String monthKey(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return monthKey(c);
    }

    /** VD: Tháng 11/2026 */
    public static String monthTitle(Calendar c) {
        return "Tháng " + (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.YEAR);
    }

    public static boolean isCurrentMonth(Calendar c) {
        Calendar now = Calendar.getInstance();
        return now.get(Calendar.YEAR) == c.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == c.get(Calendar.MONTH);
    }

    public static String formatDate(long millis) {
        return new SimpleDateFormat("dd/MM/yyyy", VI).format(new Date(millis));
    }

    /** Nhãn hiển thị cho nhóm ngày: Hôm nay / Hôm qua / Thứ Hai, 02/11/2026 */
    public static String formatDayHeader(long millis) {
        Calendar today = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        String ddMM = new SimpleDateFormat("dd/MM/yyyy", VI).format(new Date(millis));
        if (isSameDay(millis, today.getTimeInMillis())) return "Hôm nay, " + ddMM;
        if (isSameDay(millis, yesterday.getTimeInMillis())) return "Hôm qua, " + ddMM;
        String s = new SimpleDateFormat("EEEE, dd/MM/yyyy", VI).format(new Date(millis));
        return s.substring(0, 1).toUpperCase(VI) + s.substring(1);
    }

    /** Nhãn ngắn gọn: Hôm nay / Hôm qua / 02/11/2026 */
    public static String formatShortDay(long millis) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        if (isSameDay(millis, System.currentTimeMillis())) return "Hôm nay";
        if (isSameDay(millis, yesterday.getTimeInMillis())) return "Hôm qua";
        return formatDate(millis);
    }

    public static boolean isSameDay(long a, long b) {
        Calendar ca = Calendar.getInstance();
        ca.setTimeInMillis(a);
        Calendar cb = Calendar.getInstance();
        cb.setTimeInMillis(b);
        return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR)
                && ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR);
    }

    public static String formatTime(int hour, int minute) {
        return String.format(Locale.US, "%02d:%02d", hour, minute);
    }
}
