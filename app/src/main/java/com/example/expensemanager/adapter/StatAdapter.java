package com.example.expensemanager.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.R;
import com.example.expensemanager.model.CategoryStat;
import com.example.expensemanager.utils.CurrencyUtils;
import com.example.expensemanager.utils.IconUtils;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Bảng xếp hạng danh mục theo tổng tiền (màn hình Thống kê).
 */
public class StatAdapter extends RecyclerView.Adapter<StatAdapter.ViewHolder> {

    private final List<CategoryStat> items = new ArrayList<>();

    public void setData(List<CategoryStat> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_stat, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        CategoryStat s = items.get(position);
        int color = IconUtils.parseColor(s.getColor(), Color.GRAY);
        h.tvName.setText(s.getName());
        h.tvAmount.setText(CurrencyUtils.format(s.getTotal()));
        h.tvPercent.setText(String.format(Locale.US, "%.1f%%", s.getPercent()));
        h.progress.setIndicatorColor(color);
        h.progress.setTrackColor(IconUtils.withAlpha(color, 30));
        h.progress.setProgressCompat(Math.max(1, Math.round(s.getPercent())), false);
        IconUtils.bindCategoryIcon(h.iconContainer, h.ivIcon, s.getIcon(), s.getColor());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View iconContainer;
        final ImageView ivIcon;
        final TextView tvName, tvAmount, tvPercent;
        final LinearProgressIndicator progress;

        ViewHolder(View v) {
            super(v);
            iconContainer = v.findViewById(R.id.icon_container);
            ivIcon = v.findViewById(R.id.iv_icon);
            tvName = v.findViewById(R.id.tv_name);
            tvAmount = v.findViewById(R.id.tv_amount);
            tvPercent = v.findViewById(R.id.tv_percent);
            progress = v.findViewById(R.id.progress);
        }
    }
}
