package com.example.expensemanager.model;

/**
 * Thực thể Giao dịch thu/chi (bảng transactions).
 * Các trường categoryName/categoryIcon/categoryColor được lấy thêm qua JOIN để hiển thị.
 */
public class Transaction {

    public static final int TYPE_EXPENSE = Category.TYPE_EXPENSE;
    public static final int TYPE_INCOME = Category.TYPE_INCOME;

    private int id;
    private double amount;
    private int type;
    private int categoryId;
    private String note;
    private long date;

    // Thông tin danh mục (JOIN)
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;

    public Transaction() {
    }

    public Transaction(double amount, int type, int categoryId, String note, long date) {
        this.amount = amount;
        this.type = type;
        this.categoryId = categoryId;
        this.note = note;
        this.date = date;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public int getType() { return type; }
    public void setType(int type) { this.type = type; }

    public boolean isIncome() { return type == TYPE_INCOME; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getCategoryIcon() { return categoryIcon; }
    public void setCategoryIcon(String categoryIcon) { this.categoryIcon = categoryIcon; }

    public String getCategoryColor() { return categoryColor; }
    public void setCategoryColor(String categoryColor) { this.categoryColor = categoryColor; }
}
