package com.example.expensemanager.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.R;
import com.example.expensemanager.utils.IconUtils;

import java.util.List;

/**
 * Lưới chọn biểu tượng cho danh mục.
 */
public class IconPickerAdapter extends RecyclerView.Adapter<IconPickerAdapter.ViewHolder> {

    private final List<String> icons;
    private String selected;
    private int color;

    public IconPickerAdapter(List<String> icons, String selected, int color) {
        this.icons = icons;
        this.selected = selected;
        this.color = color;
    }

    public String getSelected() {
        return selected;
    }

    public void setColor(int color) {
        this.color = color;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_icon_pick, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        String name = icons.get(position);
        boolean isSelected = name.equals(selected);
        h.icon.setImageResource(IconUtils.getIconRes(name));
        h.icon.setImageTintList(ColorStateList.valueOf(isSelected ? 0xFFFFFFFF : color));
        h.container.setBackgroundTintList(ColorStateList.valueOf(
                isSelected ? color : IconUtils.withAlpha(color, 30)));
        h.container.setOnClickListener(v -> {
            selected = name;
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return icons.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View container;
        final ImageView icon;

        ViewHolder(View v) {
            super(v);
            container = v.findViewById(R.id.icon_container);
            icon = v.findViewById(R.id.iv_icon);
        }
    }
}
