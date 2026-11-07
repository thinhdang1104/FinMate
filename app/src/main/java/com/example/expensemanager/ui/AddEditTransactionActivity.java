package com.example.expensemanager.ui;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.R;
import com.example.expensemanager.adapter.CategoryAdapter;
import com.example.expensemanager.database.CategoryDAO;
import com.example.expensemanager.database.TransactionDAO;
import com.example.expensemanager.model.Category;
import com.example.expensemanager.model.Transaction;
import com.example.expensemanager.utils.CurrencyUtils;
import com.example.expensemanager.utils.DateUtils;
import com.example.expensemanager.utils.MoneyTextWatcher;
import com.example.expensemanager.utils.NotificationHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Màn hình Thêm mới / Chỉnh sửa giao dịch.
 * Truyền EXTRA_TRANSACTION_ID để chỉnh sửa, không truyền để thêm mới.
 */
public class AddEditTransactionActivity extends AppCompatActivity {

    public static final String EXTRA_TRANSACTION_ID = "extra_transaction_id";

    private MaterialToolbar toolbar;
    private MaterialButtonToggleGroup toggleType;
    private EditText etAmount;
    private TextView tvCurrency, tvDate, tvCategoryError;
    private TextInputEditText etNote;
    private MaterialButton btnSave;

    private CategoryAdapter categoryAdapter;
    private CategoryDAO categoryDAO;
    private TransactionDAO transactionDAO;

    private Transaction editing;           // null = thêm mới
    private int currentType = Transaction.TYPE_EXPENSE;
    private final Calendar selectedDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_transaction);

        categoryDAO = new CategoryDAO(this);
        transactionDAO = new TransactionDAO(this);

        bindViews();
        setupToolbar();

        int id = getIntent().getIntExtra(EXTRA_TRANSACTION_ID, -1);
        if (id > 0) editing = transactionDAO.getById(id);

        int selectedCategoryId = -1;
        if (editing != null) {
            toolbar.setTitle("Sửa giao dịch");
            btnSave.setText("Cập nhật");
            currentType = editing.getType();
            selectedCategoryId = editing.getCategoryId();
            selectedDate.setTimeInMillis(editing.getDate());
            etAmount.setText(CurrencyUtils.formatNumber(Math.round(editing.getAmount())));
            etNote.setText(editing.getNote());
        } else {
            toolbar.setTitle("Thêm giao dịch");
        }

        toggleType.check(currentType == Transaction.TYPE_INCOME ? R.id.btn_income : R.id.btn_expense);
        toggleType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            currentType = checkedId == R.id.btn_income ? Transaction.TYPE_INCOME : Transaction.TYPE_EXPENSE;
            loadCategories(-1);
            applyTypeColor();
        });

        loadCategories(selectedCategoryId);
        applyTypeColor();
        updateDateLabel();

        findViewById(R.id.row_date).setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> save());

        if (editing == null) {
            etAmount.requestFocus();
        }
    }

    private void bindViews() {
        toolbar = findViewById(R.id.toolbar);
        toggleType = findViewById(R.id.toggle_type);
        etAmount = findViewById(R.id.et_amount);
        tvCurrency = findViewById(R.id.tv_currency);
        tvDate = findViewById(R.id.tv_date);
        tvCategoryError = findViewById(R.id.tv_category_error);
        etNote = findViewById(R.id.et_note);
        btnSave = findViewById(R.id.btn_save);

        etAmount.addTextChangedListener(new MoneyTextWatcher(etAmount));

        RecyclerView rv = findViewById(R.id.rv_categories);
        rv.setLayoutManager(new GridLayoutManager(this, 4));
        rv.setNestedScrollingEnabled(false);
        categoryAdapter = new CategoryAdapter(category -> tvCategoryError.setVisibility(View.GONE));
        rv.setAdapter(categoryAdapter);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadCategories(int selectedId) {
        List<Category> list = categoryDAO.getByType(currentType);
        boolean found = false;
        for (Category c : list) {
            if (c.getId() == selectedId) {
                found = true;
                break;
            }
        }
        if (!found) selectedId = list.isEmpty() ? -1 : list.get(0).getId();
        categoryAdapter.setData(list, selectedId);
    }

    /** Đổi màu số tiền và nút Lưu theo loại Thu (xanh) / Chi (đỏ). */
    private void applyTypeColor() {
        int color = ContextCompat.getColor(this,
                currentType == Transaction.TYPE_INCOME ? R.color.income : R.color.expense);
        etAmount.setTextColor(color);
        tvCurrency.setTextColor(color);
        btnSave.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    private void showDatePicker() {
        // MaterialDatePicker làm việc với UTC
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.clear();
        utc.set(selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH));

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Chọn ngày giao dịch")
                .setSelection(utc.getTimeInMillis())
                .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            Calendar picked = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            picked.setTimeInMillis(selection);
            selectedDate.set(picked.get(Calendar.YEAR), picked.get(Calendar.MONTH),
                    picked.get(Calendar.DAY_OF_MONTH));
            updateDateLabel();
        });
        picker.show(getSupportFragmentManager(), "date_picker");
    }

    private void updateDateLabel() {
        tvDate.setText(DateUtils.formatDayHeader(selectedDate.getTimeInMillis()));
    }

    private void save() {
        double amount = CurrencyUtils.parse(etAmount.getText().toString());
        if (amount <= 0) {
            etAmount.setError("Vui lòng nhập số tiền lớn hơn 0");
            etAmount.requestFocus();
            return;
        }
        int categoryId = categoryAdapter.getSelectedId();
        if (categoryId <= 0) {
            tvCategoryError.setVisibility(View.VISIBLE);
            return;
        }
        String note = etNote.getText() == null ? "" : etNote.getText().toString().trim();

        Transaction t = editing != null ? editing : new Transaction();
        t.setAmount(amount);
        t.setType(currentType);
        t.setCategoryId(categoryId);
        t.setNote(TextUtils.isEmpty(note) ? null : note);
        t.setDate(selectedDate.getTimeInMillis());

        if (editing != null) {
            transactionDAO.update(t);
            Toast.makeText(this, "Đã cập nhật giao dịch", Toast.LENGTH_SHORT).show();
        } else {
            transactionDAO.insert(t);
            Toast.makeText(this, "Đã thêm giao dịch", Toast.LENGTH_SHORT).show();
        }

        // Kiểm tra ngân sách sau khi thêm/sửa khoản chi
        if (currentType == Transaction.TYPE_EXPENSE) {
            String warning = NotificationHelper.checkBudget(this, t.getDate());
            if (warning != null) Toast.makeText(this, warning, Toast.LENGTH_LONG).show();
        }
        finish();
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Xóa giao dịch?")
                .setMessage("Giao dịch " + CurrencyUtils.format(editing.getAmount())
                        + " sẽ bị xóa vĩnh viễn.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (d, w) -> {
                    transactionDAO.delete(editing.getId());
                    Toast.makeText(this, "Đã xóa giao dịch", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_edit, menu);
        menu.findItem(R.id.action_delete).setVisible(editing != null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            confirmDelete();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
