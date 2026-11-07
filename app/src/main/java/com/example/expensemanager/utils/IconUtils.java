package com.example.expensemanager.utils;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;

import com.example.expensemanager.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ánh xạ tên icon (lưu trong DB) sang drawable resource và các tiện ích màu sắc.
 */
public final class IconUtils {

    private static final Map<String, Integer> ICONS = new LinkedHashMap<>();

    static {
        ICONS.put("ic_food", R.drawable.ic_food);
        ICONS.put("ic_transport", R.drawable.ic_transport);
        ICONS.put("ic_shopping", R.drawable.ic_shopping);
        ICONS.put("ic_bill", R.drawable.ic_bill);
        ICONS.put("ic_home", R.drawable.ic_home);
        ICONS.put("ic_education", R.drawable.ic_education);
        ICONS.put("ic_entertainment", R.drawable.ic_entertainment);
        ICONS.put("ic_health", R.drawable.ic_health);
        ICONS.put("ic_salary", R.drawable.ic_salary);
        ICONS.put("ic_bonus", R.drawable.ic_bonus);
        ICONS.put("ic_invest", R.drawable.ic_invest);
        ICONS.put("ic_trending_down", R.drawable.ic_trending_down);
        ICONS.put("ic_calendar", R.drawable.ic_calendar);
        ICONS.put("ic_edit", R.drawable.ic_edit);
        ICONS.put("ic_flag", R.drawable.ic_flag);
        ICONS.put("ic_other", R.drawable.ic_other);
    }

    /** Bảng màu cho danh mục mới. */
    public static final String[] PALETTE = {
            "#F97316", "#EF4444", "#EC4899", "#A855F7", "#8B5CF6", "#3B82F6",
            "#0EA5E9", "#06B6D4", "#14B8A6", "#16A34A", "#84CC16", "#EAB308",
            "#F59E0B", "#78716C", "#64748B"
    };

    private IconUtils() {
    }

    public static int getIconRes(String name) {
        Integer res = name == null ? null : ICONS.get(name);
        return res != null ? res : R.drawable.ic_other;
    }

    public static List<String> getAllIconNames() {
        return new ArrayList<>(ICONS.keySet());
    }

    public static int parseColor(String hex, int fallback) {
        try {
            return hex == null ? fallback : Color.parseColor(hex);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    /** Giảm độ đậm màu (alpha 0-255) để làm nền tròn cho icon. */
    public static int withAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    /** Hiển thị icon danh mục: nền tròn nhạt + icon cùng tông màu. */
    public static void bindCategoryIcon(View container, ImageView icon, String iconName, String colorHex) {
        int color = parseColor(colorHex, Color.parseColor("#64748B"));
        container.setBackgroundTintList(ColorStateList.valueOf(withAlpha(color, 36)));
        icon.setImageResource(getIconRes(iconName));
        icon.setImageTintList(ColorStateList.valueOf(color));
    }
}
