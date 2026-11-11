package com.example.expensemanager.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.R;
import com.example.expensemanager.adapter.TransactionAdapter;
import com.example.expensemanager.database.TransactionDAO;
import com.example.expensemanager.model.Transaction;
import com.example.expensemanager.ui.AddEditTransactionActivity;
import com.example.expensemanager.utils.CurrencyUtils;
import com.example.expensemanager.utils.DateUtils;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.List;

/**
 * Tab Sổ giao dịch: chọn tháng, tìm kiếm, lọc Thu/Chi, vuốt để xóa (có hoàn tác).
 */
public class TransactionListFragment extends Fragment {

    private final Calendar currentMonth = Calendar.getInstance();
    private int filterType = -1;      // -1 tất cả, 0 chi, 1 thu
    private String keyword = "";

    private TextView tvMonth, tvSumIncome, tvSumExpense, tvEmptyMessage;
    private View layoutEmpty;
    private RecyclerView rv;
    private TransactionAdapter adapter;
    private TransactionDAO dao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transactions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        dao = new TransactionDAO(requireContext());

        tvMonth = v.findViewById(R.id.tv_month);
        tvSumIncome = v.findViewById(R.id.tv_sum_income);
        tvSumExpense = v.findViewById(R.id.tv_sum_expense);
        layoutEmpty = v.findViewById(R.id.layout_empty);
        tvEmptyMessage = v.findViewById(R.id.tv_empty_message);

        v.findViewById(R.id.btn_prev_month).setOnClickListener(x -> changeMonth(-1));
        v.findViewById(R.id.btn_next_month).setOnClickListener(x -> changeMonth(1));

        EditText etSearch = v.findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                keyword = s.toString();
                load();
            }
        });

        ChipGroup chips = v.findViewById(R.id.chip_group_filter);
        chips.setOnCheckedStateChangeListener((group, ids) -> {
            int id = ids.isEmpty() ? R.id.chip_all : ids.get(0);
            if (id == R.id.chip_expense) filterType = Transaction.TYPE_EXPENSE;
            else if (id == R.id.chip_income) filterType = Transaction.TYPE_INCOME;
            else filterType = -1;
            load();
        });

        rv = v.findViewById(R.id.rv_transactions);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TransactionAdapter(true, t -> {
            Intent i = new Intent(requireContext(), AddEditTransactionActivity.class);
            i.putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_ID, t.getId());
            startActivity(i);
        });
        rv.setAdapter(adapter);
        attachSwipeToDelete();
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    private void changeMonth(int delta) {
        currentMonth.add(Calendar.MONTH, delta);
        load();
    }

    private void load() {
        tvMonth.setText(DateUtils.monthTitle(currentMonth));
        List<Transaction> list = dao.getByRange(DateUtils.startOfMonth(currentMonth),
                DateUtils.endOfMonth(currentMonth), filterType, keyword);
        adapter.setData(list);

        double income = 0, expense = 0;
        for (Transaction t : list) {
            if (t.isIncome()) income += t.getAmount();
            else expense += t.getAmount();
        }
        tvSumIncome.setText("+" + CurrencyUtils.format(income));
        tvSumExpense.setText("-" + CurrencyUtils.format(expense));

        boolean empty = list.isEmpty();
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rv.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            tvEmptyMessage.setText(keyword.trim().isEmpty()
                    ? "Chưa có giao dịch nào trong " + DateUtils.monthTitle(currentMonth).toLowerCase(DateUtils.VI)
                    : "Không tìm thấy giao dịch phù hợp với \"" + keyword.trim() + "\"");
        }
    }

    /** Vuốt sang trái để xóa, hiển thị Snackbar cho phép hoàn tác. */
    private void attachSwipeToDelete() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView r, @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction) {
                int pos = vh.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                Transaction removed = adapter.getItem(pos);
                dao.delete(removed.getId());
                load();
                Snackbar.make(requireView(), "Đã xóa " + CurrencyUtils.format(removed.getAmount()), Snackbar.LENGTH_LONG)
                        .setAction("Hoàn tác", x -> {
                            dao.insert(removed);
                            load();
                        })
                        .show();
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(rv);
    }
}
