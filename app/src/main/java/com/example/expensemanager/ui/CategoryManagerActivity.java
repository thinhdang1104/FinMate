package com.example.expensemanager.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.R;
import com.example.expensemanager.adapter.CategoryManageAdapter;
import com.example.expensemanager.adapter.ColorPickerAdapter;
import com.example.expensemanager.adapter.IconPickerAdapter;
import com.example.expensemanager.database.CategoryDAO;
import com.example.expensemanager.database.TransactionDAO;
import com.example.expensemanager.model.Category;
import com.example.expensemanager.utils.IconUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

/**
 * Màn hình Quản lý danh mục: xem theo loại, thêm, sửa, xóa.
 */
public class CategoryManagerActivity extends AppCompatActivity {

    private CategoryDAO categoryDAO;
    private TransactionDAO transactionDAO;
    private CategoryManageAdapter adapter;
    private TextView tvEmpty;
    private int currentType = Category.TYPE_EXPENSE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_manager);

        categoryDAO = new CategoryDAO(this);
        transactionDAO = new TransactionDAO(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvEmpty = findViewById(R.id.tv_empty);

        TabLayout tabs = findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab().setText("Chi tiêu"));
        tabs.addTab(tabs.newTab().setText("Thu nhập"));
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentType = tab.getPosition() == 0 ? Category.TYPE_EXPENSE : Category.TYPE_INCOME;
                load();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        RecyclerView rv = findViewById(R.id.rv_categories);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoryManageAdapter(new CategoryManageAdapter.Listener() {
            @Override
            public void onEdit(Category category) {
                showCategoryDialog(category);
            }

            @Override
            public void onDelete(Category category) {
                confirmDelete(category);
            }
        });
        rv.setAdapter(adapter);

        ExtendedFloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> showCategoryDialog(null));

        load();
    }

    private void load() {
        List<Category> list = categoryDAO.getByType(currentType);
        adapter.setData(list);
        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showCategoryDialog(Category existing) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_category, null);
        TextInputLayout tilName = view.findViewById(R.id.til_name);
        TextInputEditText etName = view.findViewById(R.id.et_name);
        RecyclerView rvIcons = view.findViewById(R.id.rv_icons);
        RecyclerView rvColors = view.findViewById(R.id.rv_colors);

        String initialIcon = existing != null ? existing.getIcon() : "ic_other";
        String initialColor = existing != null ? existing.getColor() : IconUtils.PALETTE[0];
        if (existing != null) etName.setText(existing.getName());

        IconPickerAdapter iconAdapter = new IconPickerAdapter(IconUtils.getAllIconNames(), initialIcon,
                IconUtils.parseColor(initialColor, Color.GRAY));
        rvIcons.setLayoutManager(new GridLayoutManager(this, 6));
        rvIcons.setAdapter(iconAdapter);

        ColorPickerAdapter colorAdapter = new ColorPickerAdapter(IconUtils.PALETTE, initialColor,
                hex -> iconAdapter.setColor(IconUtils.parseColor(hex, Color.GRAY)));
        rvColors.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvColors.setAdapter(colorAdapter);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(existing == null ? "Thêm danh mục "
                        + (currentType == Category.TYPE_EXPENSE ? "chi tiêu" : "thu nhập") : "Sửa danh mục")
                .setView(view)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = etName.getText() == null ? "" : etName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                tilName.setError("Tên danh mục không được để trống");
                return;
            }
            if (existing != null) {
                existing.setName(name);
                existing.setIcon(iconAdapter.getSelected());
                existing.setColor(colorAdapter.getSelected());
                categoryDAO.update(existing);
            } else {
                categoryDAO.insert(new Category(name, currentType, iconAdapter.getSelected(),
                        colorAdapter.getSelected()));
            }
            Toast.makeText(this, "Đã lưu danh mục", Toast.LENGTH_SHORT).show();
            load();
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void confirmDelete(Category category) {
        int used = transactionDAO.countByCategory(category.getId());
        if (used > 0) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Không thể xóa")
                    .setMessage("Danh mục \"" + category.getName() + "\" đang được sử dụng bởi " + used
                            + " giao dịch. Hãy xóa hoặc chuyển các giao dịch đó sang danh mục khác trước.")
                    .setPositiveButton("Đã hiểu", null)
                    .show();
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle("Xóa danh mục?")
                .setMessage("Danh mục \"" + category.getName() + "\" sẽ bị xóa.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (d, w) -> {
                    categoryDAO.delete(category.getId());
                    load();
                })
                .show();
    }
}
