package com.example.expensemanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.expensemanager.model.CategoryStat;
import com.example.expensemanager.model.Transaction;

import java.util.ArrayList;
import java.util.List;

import static com.example.expensemanager.database.DatabaseHelper.*;

/**
 * Data Access Object cho bảng transactions: CRUD, tìm kiếm, lọc và các truy vấn thống kê.
 */
public class TransactionDAO {

    private static final String BASE_SELECT =
            "SELECT t.id, t.amount, t.type, t.category_id, t.note, t.date, "
                    + "c.name AS cat_name, c.icon AS cat_icon, c.color AS cat_color "
                    + "FROM " + TABLE_TRANSACTIONS + " t "
                    + "LEFT JOIN " + TABLE_CATEGORIES + " c ON t.category_id = c.id ";

    private final DatabaseHelper dbHelper;

    public TransactionDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    // ------------------------- CRUD -------------------------

    public long insert(Transaction t) {
        return dbHelper.getWritableDatabase().insert(TABLE_TRANSACTIONS, null, toValues(t));
    }

    /** Thêm nhiều giao dịch trong một transaction SQL (nhanh hơn khi tạo dữ liệu mẫu). */
    public void insertAll(List<Transaction> list) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Transaction t : list) {
                db.insert(TABLE_TRANSACTIONS, null, toValues(t));
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public int update(Transaction t) {
        return dbHelper.getWritableDatabase().update(TABLE_TRANSACTIONS, toValues(t),
                COL_TR_ID + " = ?", new String[]{String.valueOf(t.getId())});
    }

    public int delete(int id) {
        return dbHelper.getWritableDatabase().delete(TABLE_TRANSACTIONS,
                COL_TR_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void deleteAll() {
        dbHelper.getWritableDatabase().delete(TABLE_TRANSACTIONS, null, null);
    }

    public Transaction getById(int id) {
        List<Transaction> list = rawList(BASE_SELECT + "WHERE t.id = ?", new String[]{String.valueOf(id)});
        return list.isEmpty() ? null : list.get(0);
    }

    // ------------------------- Truy vấn danh sách -------------------------

    public List<Transaction> getAll() {
        return rawList(BASE_SELECT + "ORDER BY t.date DESC, t.id DESC", null);
    }

    public List<Transaction> getRecent(int limit) {
        return rawList(BASE_SELECT + "ORDER BY t.date DESC, t.id DESC LIMIT " + limit, null);
    }

    /**
     * Lấy giao dịch trong khoảng thời gian, có lọc theo loại và từ khóa.
     *
     * @param type    -1 = tất cả, 0 = chi tiêu, 1 = thu nhập
     * @param keyword tìm theo ghi chú hoặc tên danh mục (có thể rỗng)
     */
    public List<Transaction> getByRange(long start, long end, int type, String keyword) {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE t.date BETWEEN ? AND ? ");
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(start));
        args.add(String.valueOf(end));
        if (type >= 0) {
            sql.append("AND t.type = ? ");
            args.add(String.valueOf(type));
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (t.note LIKE ? OR c.name LIKE ?) ");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
        }
        sql.append("ORDER BY t.date DESC, t.id DESC");
        return rawList(sql.toString(), args.toArray(new String[0]));
    }

    // ------------------------- Thống kê -------------------------

    /** Tổng tiền theo loại trong khoảng thời gian. */
    public double sumByRange(long start, long end, int type) {
        return singleDouble("SELECT COALESCE(SUM(amount), 0) FROM " + TABLE_TRANSACTIONS
                        + " WHERE type = ? AND date BETWEEN ? AND ?",
                new String[]{String.valueOf(type), String.valueOf(start), String.valueOf(end)});
    }

    /** Số dư hiện tại = tổng thu - tổng chi (toàn bộ thời gian). */
    public double getTotalBalance() {
        return singleDouble("SELECT COALESCE(SUM(CASE WHEN type = 1 THEN amount ELSE -amount END), 0) FROM "
                + TABLE_TRANSACTIONS, null);
    }

    /** Tổng tiền theo từng danh mục, sắp xếp giảm dần, kèm phần trăm. */
    public List<CategoryStat> getStatsByCategory(long start, long end, int type) {
        List<CategoryStat> list = new ArrayList<>();
        String sql = "SELECT c.id, c.name, c.icon, c.color, SUM(t.amount) AS total "
                + "FROM " + TABLE_TRANSACTIONS + " t JOIN " + TABLE_CATEGORIES + " c ON t.category_id = c.id "
                + "WHERE t.type = ? AND t.date BETWEEN ? AND ? "
                + "GROUP BY c.id ORDER BY total DESC";
        double sum = 0;
        try (Cursor c = dbHelper.getReadableDatabase().rawQuery(sql,
                new String[]{String.valueOf(type), String.valueOf(start), String.valueOf(end)})) {
            while (c.moveToNext()) {
                CategoryStat stat = new CategoryStat(c.getInt(0), c.getString(1),
                        c.getString(2), c.getString(3), c.getDouble(4));
                sum += stat.getTotal();
                list.add(stat);
            }
        }
        for (CategoryStat s : list) {
            s.setPercent(sum > 0 ? (float) (s.getTotal() * 100 / sum) : 0f);
        }
        return list;
    }

    public int countByCategory(int categoryId) {
        return (int) singleDouble("SELECT COUNT(*) FROM " + TABLE_TRANSACTIONS + " WHERE category_id = ?",
                new String[]{String.valueOf(categoryId)});
    }

    // ------------------------- Helpers -------------------------

    private double singleDouble(String sql, String[] args) {
        try (Cursor c = dbHelper.getReadableDatabase().rawQuery(sql, args)) {
            return c.moveToFirst() ? c.getDouble(0) : 0;
        }
    }

    private List<Transaction> rawList(String sql, String[] args) {
        List<Transaction> list = new ArrayList<>();
        try (Cursor c = dbHelper.getReadableDatabase().rawQuery(sql, args)) {
            while (c.moveToNext()) {
                Transaction t = new Transaction();
                t.setId(c.getInt(0));
                t.setAmount(c.getDouble(1));
                t.setType(c.getInt(2));
                t.setCategoryId(c.getInt(3));
                t.setNote(c.getString(4));
                t.setDate(c.getLong(5));
                t.setCategoryName(c.getString(6));
                t.setCategoryIcon(c.getString(7));
                t.setCategoryColor(c.getString(8));
                list.add(t);
            }
        }
        return list;
    }

    private ContentValues toValues(Transaction t) {
        ContentValues v = new ContentValues();
        v.put(COL_TR_AMOUNT, t.getAmount());
        v.put(COL_TR_TYPE, t.getType());
        v.put(COL_TR_CATEGORY_ID, t.getCategoryId());
        v.put(COL_TR_NOTE, t.getNote());
        v.put(COL_TR_DATE, t.getDate());
        return v;
    }
}
