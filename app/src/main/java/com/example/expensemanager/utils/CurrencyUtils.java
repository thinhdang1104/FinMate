package com.example.expensemanager.utils;

import com.example.expensemanager.model.Transaction;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Định dạng tiền tệ VNĐ, VD: 1.250.000 ₫
 */
public final class CurrencyUtils {

    private static final DecimalFormat FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        FORMAT = new DecimalFormat("#,##0", symbols);
    }

    private CurrencyUtils() {
    }

    public static String format(double amount) {
        return FORMAT.format(Math.round(amount)) + " ₫";
    }

    /** Có dấu +/- theo loại giao dịch. */
    public static String formatSigned(double amount, int type) {
        return (type == Transaction.TYPE_INCOME ? "+" : "-") + format(amount);
    }

    /** Chỉ số, không ký hiệu tiền tệ (dùng trong ô nhập). */
    public static String formatNumber(long number) {
        return FORMAT.format(number);
    }

    /** Chuyển chuỗi "1.250.000" về số 1250000. */
    public static double parse(String text) {
        if (text == null) return 0;
        String digits = text.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return 0;
        try {
            return Double.parseDouble(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** Dạng rút gọn cho trục biểu đồ: 1,5tr / 500k / 2 tỷ. */
    public static String formatShort(double value) {
        double abs = Math.abs(value);
        String result;
        if (abs >= 1_000_000_000) {
            result = String.format(Locale.US, "%.1f tỷ", value / 1_000_000_000);
        } else if (abs >= 1_000_000) {
            result = String.format(Locale.US, "%.1ftr", value / 1_000_000);
        } else if (abs >= 1_000) {
            result = String.format(Locale.US, "%.0fk", value / 1_000);
        } else {
            result = String.format(Locale.US, "%.0f", value);
        }
        return result.replace(".0", "").replace('.', ',');
    }
}
