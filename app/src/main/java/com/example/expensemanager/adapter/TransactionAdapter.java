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
import com.example.expensemanager.model.Transaction;
import com.example.expensemanager.utils.CurrencyUtils;
import com.example.expensemanager.utils.DateUtils;
import com.example.expensemanager.utils.IconUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter hiển thị danh sách giao dịch.
 * Khi {@code groupByDay = true}, tự chèn tiêu đề ngày khi ngày thay đổi.
 */
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    public interface OnTransactionClickListener {
        void onClick(Transaction transaction);
    }

    private final List<Transaction> items = new ArrayList<>();
    private final boolean groupByDay;
    private final OnTransactionClickListener listener;

    public TransactionAdapter(boolean groupByDay, OnTransactionClickListener listener) {
        this.groupByDay = groupByDay;
        this.listener = listener;
    }

    public void setData(List<Transaction> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    public Transaction getItem(int position) {
        return items.get(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Transaction t = items.get(position);

        // Tiêu đề nhóm theo ngày
        boolean showHeader = groupByDay && (position == 0
                || !DateUtils.isSameDay(items.get(position - 1).getDate(), t.getDate()));
        h.tvHeader.setVisibility(showHeader ? View.VISIBLE : View.GONE);
        if (showHeader) h.tvHeader.setText(DateUtils.formatDayHeader(t.getDate()));

        h.tvCategory.setText(t.getCategoryName() != null ? t.getCategoryName() : "Không rõ");

        String note = t.getNote() == null ? "" : t.getNote().trim();
        String sub = groupByDay ? note : DateUtils.formatShortDay(t.getDate()) + (note.isEmpty() ? "" : " · " + note);
        h.tvNote.setText(sub);
        h.tvNote.setVisibility(sub.isEmpty() ? View.GONE : View.VISIBLE);

        h.tvAmount.setText(CurrencyUtils.formatSigned(t.getAmount(), t.getType()));
        h.tvAmount.setTextColor(ContextCompat.getColor(h.itemView.getContext(),
                t.isIncome() ? R.color.income : R.color.expense));

        IconUtils.bindCategoryIcon(h.iconContainer, h.ivIcon, t.getCategoryIcon(), t.getCategoryColor());

        h.row.setOnClickListener(v -> {
            if (listener != null) listener.onClick(t);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvHeader, tvCategory, tvNote, tvAmount;
        final View row, iconContainer;
        final ImageView ivIcon;

        ViewHolder(View v) {
            super(v);
            tvHeader = v.findViewById(R.id.tv_date_header);
            row = v.findViewById(R.id.row);
            iconContainer = v.findViewById(R.id.icon_container);
            ivIcon = v.findViewById(R.id.iv_icon);
            tvCategory = v.findViewById(R.id.tv_category);
            tvNote = v.findViewById(R.id.tv_note);
            tvAmount = v.findViewById(R.id.tv_amount);
        }
    }
}
