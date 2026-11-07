package com.example.expensemanager.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.expensemanager.R;
import com.example.expensemanager.database.BudgetDAO;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Các hộp thoại dùng chung.
 */
public final class DialogUtils {

    private DialogUtils() {
    }

    /** Hộp thoại thiết lập ngân sách tháng. */
    public static void showBudgetDialog(Context context, String monthKey, String monthTitle, Runnable onSaved) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_input_amount, null);
        TextInputEditText et = view.findViewById(R.id.et_amount);
        et.addTextChangedListener(new MoneyTextWatcher(et));

        BudgetDAO dao = new BudgetDAO(context);
        double current = dao.getLimit(monthKey);
        if (current > 0) et.setText(CurrencyUtils.formatNumber((long) current));

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setTitle("Ngân sách " + monthTitle.toLowerCase(DateUtils.VI))
                .setMessage("Đặt hạn mức chi tiêu tối đa. Ứng dụng sẽ cảnh báo khi bạn dùng 80% và khi vượt hạn mức.")
                .setView(view)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", (d, w) -> {
                    double value = CurrencyUtils.parse(et.getText() == null ? "" : et.getText().toString());
                    if (value <= 0) {
                        Toast.makeText(context, "Vui lòng nhập số tiền hợp lệ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dao.setLimit(monthKey, value);
                    Toast.makeText(context, "Đã lưu ngân sách", Toast.LENGTH_SHORT).show();
                    if (onSaved != null) onSaved.run();
                });
        if (current > 0) {
            builder.setNeutralButton("Xóa", (d, w) -> {
                dao.delete(monthKey);
                if (onSaved != null) onSaved.run();
            });
        }
        builder.show();
    }
}
