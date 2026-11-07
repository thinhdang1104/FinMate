package com.example.expensemanager.model;

/**
 * Thực thể Ngân sách theo tháng (bảng budgets).
 */
public class Budget {

    private int id;
    private String monthYear;   // định dạng MM-yyyy, VD: 11-2026
    private double limitAmount;

    public Budget() {
    }

    public Budget(int id, String monthYear, double limitAmount) {
        this.id = id;
        this.monthYear = monthYear;
        this.limitAmount = limitAmount;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMonthYear() { return monthYear; }
    public void setMonthYear(String monthYear) { this.monthYear = monthYear; }

    public double getLimitAmount() { return limitAmount; }
    public void setLimitAmount(double limitAmount) { this.limitAmount = limitAmount; }
}
