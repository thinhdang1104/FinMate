package com.example.expensemanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.expensemanager.model.Category;

/**
 * Lớp quản lý cơ sở dữ liệu SQLite: tạo bảng users, categories, transactions, budgets
 * và dữ liệu mặc định.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "ExpenseManager.db";
    public static final int DB_VERSION = 2;

    // Bảng users
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_NAME = "full_name";
    public static final String COL_USER_EMAIL = "email";
    public static final String COL_USER_PASSWORD = "password_hash";
    public static final String COL_USER_CREATED_AT = "created_at";

    // Bảng categories
    public static final String TABLE_CATEGORIES = "categories";
    public static final String COL_CAT_ID = "id";
    public static final String COL_CAT_NAME = "name";
    public static final String COL_CAT_TYPE = "type";
    public static final String COL_CAT_ICON = "icon";
    public static final String COL_CAT_COLOR = "color";

    // Bảng transactions
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String COL_TR_ID = "id";
    public static final String COL_TR_USER_ID = "user_id";
    public static final String COL_TR_AMOUNT = "amount";
    public static final String COL_TR_TYPE = "type";
    public static final String COL_TR_CATEGORY_ID = "category_id";
    public static final String COL_TR_NOTE = "note";
    public static final String COL_TR_DATE = "date";

    // Bảng budgets
    public static final String TABLE_BUDGETS = "budgets";
    public static final String COL_BG_ID = "id";
    public static final String COL_BG_USER_ID = "user_id";
    public static final String COL_BG_MONTH_YEAR = "month_year";
    public static final String COL_BG_LIMIT = "limit_amount";

    private static final String SQL_CREATE_USERS =
            "CREATE TABLE " + TABLE_USERS + " ("
                    + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_USER_NAME + " TEXT NOT NULL, "
                    + COL_USER_EMAIL + " TEXT UNIQUE NOT NULL, "
                    + COL_USER_PASSWORD + " TEXT NOT NULL, "
                    + COL_USER_CREATED_AT + " INTEGER NOT NULL)";

    private static final String SQL_CREATE_CATEGORIES =
            "CREATE TABLE " + TABLE_CATEGORIES + " ("
                    + COL_CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_CAT_NAME + " TEXT NOT NULL, "
                    + COL_CAT_TYPE + " INTEGER NOT NULL, "
                    + COL_CAT_ICON + " TEXT, "
                    + COL_CAT_COLOR + " TEXT)";

    private static final String SQL_CREATE_TRANSACTIONS =
            "CREATE TABLE " + TABLE_TRANSACTIONS + " ("
                    + COL_TR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_TR_USER_ID + " INTEGER NOT NULL DEFAULT 1, "
                    + COL_TR_AMOUNT + " REAL NOT NULL, "
                    + COL_TR_TYPE + " INTEGER NOT NULL, "
                    + COL_TR_CATEGORY_ID + " INTEGER NOT NULL, "
                    + COL_TR_NOTE + " TEXT, "
                    + COL_TR_DATE + " INTEGER NOT NULL, "
                    + "FOREIGN KEY(" + COL_TR_CATEGORY_ID + ") REFERENCES "
                    + TABLE_CATEGORIES + "(" + COL_CAT_ID + "))";

    private static final String SQL_CREATE_BUDGETS =
            "CREATE TABLE " + TABLE_BUDGETS + " ("
                    + COL_BG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_BG_USER_ID + " INTEGER NOT NULL DEFAULT 1, "
                    + COL_BG_MONTH_YEAR + " TEXT NOT NULL, "
                    + COL_BG_LIMIT + " REAL NOT NULL, "
                    + "UNIQUE(" + COL_BG_USER_ID + ", " + COL_BG_MONTH_YEAR + "))";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USERS);
        db.execSQL(SQL_CREATE_CATEGORIES);
        db.execSQL(SQL_CREATE_TRANSACTIONS);
        db.execSQL(SQL_CREATE_BUDGETS);
        db.execSQL("CREATE INDEX idx_transactions_user_date ON "
                + TABLE_TRANSACTIONS + "(" + COL_TR_USER_ID + ", " + COL_TR_DATE + ")");
        seedDefaultUser(db);
        seedDefaultCategories(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Nâng cấp từ v1 lên v2: thêm bảng users và cột user_id mà không làm mất dữ liệu cũ
            db.execSQL(SQL_CREATE_USERS);
            seedDefaultUser(db);
            try {
                db.execSQL("ALTER TABLE " + TABLE_TRANSACTIONS
                        + " ADD COLUMN " + COL_TR_USER_ID + " INTEGER NOT NULL DEFAULT 1");
            } catch (Exception ignored) {
            }
            // Tạo lại bảng budgets có khóa UNIQUE(user_id, month_year)
            db.execSQL("ALTER TABLE " + TABLE_BUDGETS + " RENAME TO budgets_old");
            db.execSQL(SQL_CREATE_BUDGETS);
            db.execSQL("INSERT OR IGNORE INTO " + TABLE_BUDGETS
                    + " (" + COL_BG_USER_ID + ", " + COL_BG_MONTH_YEAR + ", " + COL_BG_LIMIT + ") "
                    + "SELECT 1, " + COL_BG_MONTH_YEAR + ", " + COL_BG_LIMIT + " FROM budgets_old");
            db.execSQL("DROP TABLE IF EXISTS budgets_old");
        }
    }

    /** Tạo sẵn tài khoản mẫu để sinh viên / giảng viên test nhanh khi demo. */
    private void seedDefaultUser(SQLiteDatabase db) {
        ContentValues v = new ContentValues();
        v.put(COL_USER_NAME, "Sinh viên IT");
        v.put(COL_USER_EMAIL, "sv@finmate.vn");
        v.put(COL_USER_PASSWORD, UserDAO.hashPassword("123456"));
        v.put(COL_USER_CREATED_AT, System.currentTimeMillis());
        db.insertWithOnConflict(TABLE_USERS, null, v, SQLiteDatabase.CONFLICT_IGNORE);
    }

    /** Danh mục mặc định khi cài đặt ứng dụng lần đầu. */
    private void seedDefaultCategories(SQLiteDatabase db) {
        Category[] defaults = {
                // Chi tiêu
                new Category("Ăn uống", Category.TYPE_EXPENSE, "ic_food", "#F97316"),
                new Category("Di chuyển", Category.TYPE_EXPENSE, "ic_transport", "#3B82F6"),
                new Category("Mua sắm", Category.TYPE_EXPENSE, "ic_shopping", "#EC4899"),
                new Category("Hóa đơn", Category.TYPE_EXPENSE, "ic_bill", "#EAB308"),
                new Category("Nhà ở", Category.TYPE_EXPENSE, "ic_home", "#8B5CF6"),
                new Category("Học tập", Category.TYPE_EXPENSE, "ic_education", "#06B6D4"),
                new Category("Giải trí", Category.TYPE_EXPENSE, "ic_entertainment", "#A855F7"),
                new Category("Sức khỏe", Category.TYPE_EXPENSE, "ic_health", "#EF4444"),
                new Category("Khác", Category.TYPE_EXPENSE, "ic_other", "#64748B"),
                // Thu nhập
                new Category("Lương", Category.TYPE_INCOME, "ic_salary", "#16A34A"),
                new Category("Thưởng", Category.TYPE_INCOME, "ic_bonus", "#F59E0B"),
                new Category("Đầu tư", Category.TYPE_INCOME, "ic_invest", "#0EA5E9"),
                new Category("Thu khác", Category.TYPE_INCOME, "ic_other", "#64748B")
        };
        for (Category c : defaults) {
            ContentValues values = new ContentValues();
            values.put(COL_CAT_NAME, c.getName());
            values.put(COL_CAT_TYPE, c.getType());
            values.put(COL_CAT_ICON, c.getIcon());
            values.put(COL_CAT_COLOR, c.getColor());
            db.insert(TABLE_CATEGORIES, null, values);
        }
    }
}
