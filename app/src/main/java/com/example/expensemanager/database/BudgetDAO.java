package com.example.expensemanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.expensemanager.model.Budget;

import static com.example.expensemanager.database.DatabaseHelper.*;

/**
 * Data Access Object cho bảng budgets (hạn mức chi tiêu theo tháng).
 */
public class BudgetDAO {

    private final DatabaseHelper dbHelper;

    public BudgetDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /** @return Budget của tháng hoặc null nếu chưa thiết lập. */
    public Budget getByMonth(String monthYear) {
        try (Cursor c = dbHelper.getReadableDatabase().query(TABLE_BUDGETS, null,
                COL_BG_MONTH_YEAR + " = ?", new String[]{monthYear}, null, null, null)) {
            if (c.moveToFirst()) {
                return new Budget(
                        c.getInt(c.getColumnIndexOrThrow(COL_BG_ID)),
                        c.getString(c.getColumnIndexOrThrow(COL_BG_MONTH_YEAR)),
                        c.getDouble(c.getColumnIndexOrThrow(COL_BG_LIMIT)));
            }
        }
        return null;
    }

    public double getLimit(String monthYear) {
        Budget b = getByMonth(monthYear);
        return b == null ? 0 : b.getLimitAmount();
    }

    /** Thêm mới hoặc cập nhật hạn mức của tháng. */
    public void setLimit(String monthYear, double limit) {
        ContentValues v = new ContentValues();
        v.put(COL_BG_MONTH_YEAR, monthYear);
        v.put(COL_BG_LIMIT, limit);
        dbHelper.getWritableDatabase().insertWithOnConflict(TABLE_BUDGETS, null, v,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void delete(String monthYear) {
        dbHelper.getWritableDatabase().delete(TABLE_BUDGETS, COL_BG_MONTH_YEAR + " = ?",
                new String[]{monthYear});
    }

    public void deleteAll() {
        dbHelper.getWritableDatabase().delete(TABLE_BUDGETS, null, null);
    }
}
