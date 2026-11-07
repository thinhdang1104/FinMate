package com.example.expensemanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.R;
import com.example.expensemanager.model.Category;
import com.example.expensemanager.utils.IconUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Danh sách danh mục trong màn hình Quản lý danh mục (sửa / xóa).
 */
public class CategoryManageAdapter extends RecyclerView.Adapter<CategoryManageAdapter.ViewHolder> {

    public interface Listener {
        void onEdit(Category category);

        void onDelete(Category category);
    }

    private final List<Category> items = new ArrayList<>();
    private final Listener listener;

    public CategoryManageAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setData(List<Category> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Category c = items.get(position);
        h.tvName.setText(c.getName());
        IconUtils.bindCategoryIcon(h.iconContainer, h.ivIcon, c.getIcon(), c.getColor());
        h.itemView.setOnClickListener(v -> listener.onEdit(c));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(c));
        h.btnDelete.setContentDescription("Xóa danh mục " + c.getName());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View iconContainer;
        final ImageView ivIcon;
        final TextView tvName;
        final ImageButton btnDelete;

        ViewHolder(View v) {
            super(v);
            iconContainer = v.findViewById(R.id.icon_container);
            ivIcon = v.findViewById(R.id.iv_icon);
            tvName = v.findViewById(R.id.tv_name);
            btnDelete = v.findViewById(R.id.btn_delete);
        }
    }
}
