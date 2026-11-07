package com.example.expensemanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.expensemanager.model.Category;

import java.util.ArrayList;
import java.util.List;

import static com.example.expensemanager.database.DatabaseHelper.*;

/**
 * Data Access Object cho bảng categories.
 */
public class CategoryDAO {

    private final DatabaseHelper dbHelper;

    public CategoryDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public List<Category> getAll() {
        return query(null, null);
    }

    public List<Category> getByType(int type) {
        return query(COL_CAT_TYPE + " = ?", new String[]{String.valueOf(type)});
    }

    public Category getById(int id) {
        List<Category> list = query(COL_CAT_ID + " = ?", new String[]{String.valueOf(id)});
        return list.isEmpty() ? null : list.get(0);
    }

    public long insert(Category category) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.insert(TABLE_CATEGORIES, null, toValues(category));
    }

    public int update(Category category) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.update(TABLE_CATEGORIES, toValues(category),
                COL_CAT_ID + " = ?", new String[]{String.valueOf(category.getId())});
    }

    public int delete(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(TABLE_CATEGORIES, COL_CAT_ID + " = ?", new String[]{String.valueOf(id)});
    }

    private List<Category> query(String selection, String[] args) {
        List<Category> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(TABLE_CATEGORIES, null, selection, args, null, null, COL_CAT_ID + " ASC")) {
            while (c.moveToNext()) {
                list.add(new Category(
                        c.getInt(c.getColumnIndexOrThrow(COL_CAT_ID)),
                        c.getString(c.getColumnIndexOrThrow(COL_CAT_NAME)),
                        c.getInt(c.getColumnIndexOrThrow(COL_CAT_TYPE)),
                        c.getString(c.getColumnIndexOrThrow(COL_CAT_ICON)),
                        c.getString(c.getColumnIndexOrThrow(COL_CAT_COLOR))));
            }
        }
        return list;
    }

    private ContentValues toValues(Category c) {
        ContentValues v = new ContentValues();
        v.put(COL_CAT_NAME, c.getName());
        v.put(COL_CAT_TYPE, c.getType());
        v.put(COL_CAT_ICON, c.getIcon());
        v.put(COL_CAT_COLOR, c.getColor());
        return v;
    }
}
