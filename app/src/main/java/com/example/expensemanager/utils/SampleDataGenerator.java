package com.example.expensemanager.utils;

import android.content.Context;

import com.example.expensemanager.database.BudgetDAO;
import com.example.expensemanager.database.CategoryDAO;
import com.example.expensemanager.database.TransactionDAO;
import com.example.expensemanager.model.Category;
import com.example.expensemanager.model.Transaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

/**
 * Sinh dữ liệu mẫu 3 tháng gần nhất để demo biểu đồ, thống kê.
 */
public final class SampleDataGenerator {

    private static final String[][] NOTES = {
            {"Ăn uống", "Cơm trưa", "Bún bò", "Trà sữa", "Cà phê", "Ăn tối cùng bạn"},
            {"Di chuyển", "Đổ xăng", "Grab đi học", "Gửi xe"},
            {"Mua sắm", "Áo thun", "Đồ dùng cá nhân", "Tai nghe"},
            {"Hóa đơn", "Tiền điện", "Tiền nước", "Internet", "Nạp điện thoại"},
            {"Nhà ở", "Tiền phòng trọ"},
            {"Học tập", "Mua sách", "Khóa học online", "Photo tài liệu"},
            {"Giải trí", "Xem phim", "Netflix", "Đi chơi cuối tuần"},
            {"Sức khỏe", "Mua thuốc", "Phòng gym"}
    };

    private SampleDataGenerator() {
    }

    public static int generate(Context context) {
        CategoryDAO categoryDAO = new CategoryDAO(context);
        List<Category> expenses = categoryDAO.getByType(Category.TYPE_EXPENSE);
        List<Category> incomes = categoryDAO.getByType(Category.TYPE_INCOME);
        if (expenses.isEmpty() || incomes.isEmpty()) return 0;

        Random random = new Random();
        List<Transaction> list = new ArrayList<>();
        Calendar day = Calendar.getInstance();
        day.set(Calendar.HOUR_OF_DAY, 12);

        for (int i = 0; i < 90; i++) {
            long date = day.getTimeInMillis();
            int dom = day.get(Calendar.DAY_OF_MONTH);

            if (dom == 1) {
                list.add(new Transaction(roundK(8_000_000 + random.nextInt(4_000_000)),
                        Transaction.TYPE_INCOME, incomes.get(0).getId(), "Lương tháng", date));
                Category rent = find(expenses, "Nhà ở");
                if (rent != null) {
                    list.add(new Transaction(2_500_000, Transaction.TYPE_EXPENSE, rent.getId(),
                            "Tiền phòng trọ", date));
                }
            }
            if (dom == 15 && random.nextBoolean()) {
                Category extra = incomes.get(random.nextInt(incomes.size()));
                list.add(new Transaction(roundK(500_000 + random.nextInt(2_000_000)),
                        Transaction.TYPE_INCOME, extra.getId(), "Thu nhập thêm", date));
            }

            int count = 1 + random.nextInt(3);
            for (int k = 0; k < count; k++) {
                Category cat = expenses.get(random.nextInt(expenses.size()));
                if ("Nhà ở".equals(cat.getName())) continue;
                double amount = roundK(20_000 + random.nextInt(amountRange(cat.getName())));
                list.add(new Transaction(amount, Transaction.TYPE_EXPENSE, cat.getId(),
                        pickNote(cat.getName(), random), date));
            }
            day.add(Calendar.DAY_OF_YEAR, -1);
        }

        new TransactionDAO(context).insertAll(list);

        BudgetDAO budgetDAO = new BudgetDAO(context);
        String key = DateUtils.monthKey(Calendar.getInstance());
        if (budgetDAO.getLimit(key) <= 0) budgetDAO.setLimit(key, 7_000_000);
        return list.size();
    }

    private static int amountRange(String name) {
        switch (name) {
            case "Mua sắm":
            case "Học tập":
                return 400_000;
            case "Hóa đơn":
            case "Sức khỏe":
                return 250_000;
            case "Giải trí":
                return 200_000;
            default:
                return 100_000;
        }
    }

    private static String pickNote(String categoryName, Random random) {
        for (String[] row : NOTES) {
            if (row[0].equals(categoryName)) return row[1 + random.nextInt(row.length - 1)];
        }
        return "";
    }

    private static Category find(List<Category> list, String name) {
        for (Category c : list) if (name.equals(c.getName())) return c;
        return null;
    }

    private static double roundK(double v) {
        return Math.round(v / 1000) * 1000;
    }
}
