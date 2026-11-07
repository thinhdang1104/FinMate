package com.example.expensemanager.model;

/**
 * Thực thể Danh mục thu/chi (bảng categories).
 */
public class Category {

    public static final int TYPE_EXPENSE = 0;
    public static final int TYPE_INCOME = 1;

    private int id;
    private String name;
    private int type;
    private String icon;
    private String color;

    public Category() {
    }

    public Category(int id, String name, int type, String icon, String color) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.color = color;
    }

    public Category(String name, int type, String icon, String color) {
        this(0, name, type, icon, color);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getType() { return type; }
    public void setType(int type) { this.type = type; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
