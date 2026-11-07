package com.example.expensemanager.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.R;

/**
 * Danh sách chọn màu (cuộn ngang) cho danh mục.
 */
public class ColorPickerAdapter extends RecyclerView.Adapter<ColorPickerAdapter.ViewHolder> {

    public interface OnColorSelectedListener {
        void onSelected(String hex);
    }

    private final String[] colors;
    private final OnColorSelectedListener listener;
    private String selected;

    public ColorPickerAdapter(String[] colors, String selected, OnColorSelectedListener listener) {
        this.colors = colors;
        this.selected = selected;
        this.listener = listener;
    }

    public String getSelected() {
        return selected;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_color_pick, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        String hex = colors[position];
        h.circle.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(hex)));
        h.check.setVisibility(hex.equalsIgnoreCase(selected) ? View.VISIBLE : View.GONE);
        h.circle.setContentDescription("Màu " + hex);
        h.circle.setOnClickListener(v -> {
            selected = hex;
            notifyDataSetChanged();
            if (listener != null) listener.onSelected(hex);
        });
    }

    @Override
    public int getItemCount() {
        return colors.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View circle;
        final ImageView check;

        ViewHolder(View v) {
            super(v);
            circle = v.findViewById(R.id.color_circle);
            check = v.findViewById(R.id.iv_check);
        }
    }
}
