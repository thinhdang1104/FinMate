package com.example.expensemanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.R;
import com.example.expensemanager.model.Category;
import com.example.expensemanager.utils.IconUtils;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * Lưới chọn danh mục trong màn hình Thêm/Sửa giao dịch.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    public interface OnCategorySelectedListener {
        void onSelected(Category category);
    }

    private final List<Category> items = new ArrayList<>();
    private final OnCategorySelectedListener listener;
    private int selectedId = -1;

    public CategoryAdapter(OnCategorySelectedListener listener) {
        this.listener = listener;
    }

    public void setData(List<Category> data, int selectedId) {
        items.clear();
        items.addAll(data);
        this.selectedId = selectedId;
        notifyDataSetChanged();
    }

    public int getSelectedId() {
        return selectedId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_grid, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Category c = items.get(position);
        h.tvName.setText(c.getName());
        IconUtils.bindCategoryIcon(h.iconContainer, h.ivIcon, c.getIcon(), c.getColor());

        boolean selected = c.getId() == selectedId;
        h.card.setStrokeColor(ContextCompat.getColor(h.itemView.getContext(),
                selected ? R.color.primary : R.color.outline));
        h.card.setStrokeWidth(dp(h.itemView, selected ? 2 : 1));
        h.card.setCardBackgroundColor(ContextCompat.getColor(h.itemView.getContext(),
                selected ? R.color.primary_container : R.color.surface));
        h.card.setContentDescription(c.getName() + (selected ? ", đang chọn" : ""));

        h.card.setOnClickListener(v -> {
            selectedId = c.getId();
            notifyDataSetChanged();
            if (listener != null) listener.onSelected(c);
        });
    }

    private static int dp(View v, int value) {
        return Math.round(value * v.getResources().getDisplayMetrics().density);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final View iconContainer;
        final ImageView ivIcon;
        final TextView tvName;

        ViewHolder(View v) {
            super(v);
            card = v.findViewById(R.id.card);
            iconContainer = v.findViewById(R.id.icon_container);
            ivIcon = v.findViewById(R.id.iv_icon);
            tvName = v.findViewById(R.id.tv_name);
        }
    }
}
