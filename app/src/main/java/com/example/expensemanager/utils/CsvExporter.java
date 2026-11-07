package com.example.expensemanager.utils;

import android.content.Context;

import com.example.expensemanager.database.TransactionDAO;
import com.example.expensemanager.model.Transaction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Xuất toàn bộ giao dịch ra file CSV (UTF-8 có BOM để Excel hiển thị đúng tiếng Việt).
 */
public final class CsvExporter {

    private CsvExporter() {
    }

    public static File export(Context context) throws IOException {
        List<Transaction> list = new TransactionDAO(context).getAll();

        File baseDir = context.getExternalFilesDir(null);
        if (baseDir == null) baseDir = context.getFilesDir();
        File dir = new File(baseDir, "exports");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Không tạo được thư mục " + dir.getAbsolutePath());
        }
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File file = new File(dir, "ChiTieu_" + stamp + ".csv");

        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            w.write('\uFEFF');
            w.write("Ngày,Loại,Danh mục,Số tiền,Ghi chú\n");
            for (Transaction t : list) {
                w.write(DateUtils.formatDate(t.getDate()) + ","
                        + (t.isIncome() ? "Thu nhập" : "Chi tiêu") + ","
                        + escape(t.getCategoryName()) + ","
                        + Math.round(t.getAmount()) + ","
                        + escape(t.getNote()) + "\n");
            }
        }
        return file;
    }

    private static String escape(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
