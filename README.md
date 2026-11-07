# FinMate – Ứng dụng Quản lý Chi tiêu & Ngân sách Cá nhân (Android)

Ứng dụng di động hỗ trợ ghi chép thu chi hằng ngày, quản lý ngân sách theo tháng và trực quan hóa dữ liệu tài chính cá nhân bằng biểu đồ trên nền tảng **Android (Java + XML + SQLite)**.

---

## Tính năng chính
1. **Tổng quan tài chính (`HomeFragment`):**
   - Hiển thị Số dư hiện tại, Tổng Thu và Tổng Chi trong tháng.
   - Thanh tiến trình **Ngân sách tháng** tự động đổi màu cảnh báo: Xanh (`< 80%`), Cam (`80% – 99%`), Đỏ (`>= 100%`).
   - Danh sách 5 giao dịch gần nhất.
2. **Sổ giao dịch (`TransactionListFragment`):**
   - Xem lịch sử giao dịch gom nhóm theo từng ngày.
   - Chuyển đổi tháng trước/sau, lọc theo loại (*Tất cả / Chi tiêu / Thu nhập*), tìm kiếm tức thì theo ghi chú hoặc tên danh mục.
   - Vuốt sang trái (`Swipe to delete`) để xóa nhanh kèm nút **Hoàn tác (`Snackbar`)**.
3. **Thêm / Sửa / Xóa giao dịch (`AddEditTransactionActivity`):**
   - Tự động phân cách hàng nghìn khi nhập số tiền (`MoneyTextWatcher`).
   - Chọn danh mục trực quan theo dạng lưới 4 cột.
   - Tự động kiểm tra và bắn thông báo cảnh báo nếu khoản chi vượt 80% hoặc 100% hạn mức tháng.
4. **Thống kê & Biểu đồ (`StatisticsFragment`):**
   - **Biểu đồ tròn (`PieChart`):** Cơ cấu chi tiêu / thu nhập theo danh mục kèm bảng xếp hạng tỷ lệ %.
   - **Biểu đồ cột đôi (`BarChart`):** So sánh Tổng Thu và Tổng Chi trong 6 tháng gần nhất.
5. **Cài đặt & Tiện ích (`SettingsFragment` & `CategoryManagerActivity`):**
   - Quản lý danh mục: Thêm, sửa, xóa danh mục với bộ chọn 16 biểu tượng vector và 15 màu sắc.
   - Nhắc nhở ghi chép chi tiêu hằng ngày (`AlarmManager` + `BroadcastReceiver`).
   - Xuất lịch sử giao dịch ra file `.csv` (hỗ trợ tiếng Việt UTF-8 BOM mở trực tiếp bằng Excel).
   - Tạo dữ liệu mẫu tự động trong 90 ngày gần nhất để phục vụ demo.

---

## Công nghệ sử dụng
- **Ngôn ngữ:** Java (JDK 17)
- **Giao diện:** XML, Material Design 3
- **Cơ sở dữ liệu:** SQLite (`SQLiteOpenHelper`, mô hình `DAO`) + `SharedPreferences`
- **Thư viện biểu đồ:** [MPAndroidChart v3.1.0](https://github.com/PhilJay/MPAndroidChart)
- **Android SDK:** `minSdk 26` (Android 8.0) – `targetSdk 34` (Android 14)

---

## Cấu trúc thư mục
```text
app/src/main/java/com/example/expensemanager
├── adapter/      # RecyclerView Adapters (Transaction, Category, Stat, Icon/Color Picker)
├── database/     # SQLiteOpenHelper (DatabaseHelper) & các lớp DAO (CategoryDAO, TransactionDAO, BudgetDAO)
├── model/        # Các lớp thực thể (Category, Transaction, Budget, CategoryStat)
├── ui/           # Activities & Fragments (MainActivity, AddEditTransactionActivity, CategoryManagerActivity, 4 Fragments)
└── utils/        # Tiện ích định dạng tiền tệ, ngày tháng, thông báo, xuất CSV, tạo dữ liệu mẫu
```

---

## Hướng dẫn cài đặt & Chạy ứng dụng
1. Clone repository:
   ```bash
   git clone https://github.com/thinhdang1104/FinMate.git
   ```
2. Mở **Android Studio** -> **Open** -> Chọn thư mục dự án.
3. Chờ Gradle đồng bộ hoàn tất và nhấn **Run (`▶`)** trên thiết bị thật hoặc máy ảo Android (API 26+).
