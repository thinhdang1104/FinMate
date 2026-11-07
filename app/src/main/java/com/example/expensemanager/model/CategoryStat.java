package com.example.expensemanager.model;

/**
 * Dữ liệu thống kê tổng tiền theo danh mục (dùng cho biểu đồ tròn và bảng xếp hạng).
 */
public class CategoryStat {

    private final int categoryId;
    private final String name;
    private final String icon;
    private final String color;
    private final double total;
    private float percent;

    public CategoryStat(int categoryId, String name, String icon, String color, double total) {
        this.categoryId = categoryId;
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.total = total;
    }

    public int getCategoryId() { return categoryId; }
    public String getName() { return name; }
    public String getIcon() { return icon; }
    public String getColor() { return color; }
    public double getTotal() { return total; }

    public float getPercent() { return percent; }
    public void setPercent(float percent) { this.percent = percent; }
}
