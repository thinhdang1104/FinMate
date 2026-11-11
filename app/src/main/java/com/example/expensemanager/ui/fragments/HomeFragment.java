package com.example.expensemanager.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.R;
import com.example.expensemanager.adapter.TransactionAdapter;
import com.example.expensemanager.database.BudgetDAO;
import com.example.expensemanager.database.TransactionDAO;
import com.example.expensemanager.model.Transaction;
import com.example.expensemanager.ui.AddEditTransactionActivity;
import com.example.expensemanager.ui.MainActivity;
import com.example.expensemanager.utils.AppPrefs;
import com.example.expensemanager.utils.CurrencyUtils;
import com.example.expensemanager.utils.DateUtils;
import com.example.expensemanager.utils.DialogUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Tab Tổng quan: số dư, thu/chi tháng, ngân sách và giao dịch gần đây.
 */
public class HomeFragment extends Fragment {

    private TextView tvGreeting, tvMonth, tvBalance, tvIncome, tvExpense;
    private TextView tvBudgetTitle, tvBudgetRemain, tvBudgetSpent, tvBudgetPercent, tvBudgetEmpty;
    private View layoutBudgetContent, layoutEmpty;
    private LinearProgressIndicator progressBudget;
    private MaterialButton btnSetBudget;
    private TransactionAdapter adapter;

    private TransactionDAO transactionDAO;
    private BudgetDAO budgetDAO;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        transactionDAO = new TransactionDAO(requireContext());
        budgetDAO = new BudgetDAO(requireContext());

        tvGreeting = v.findViewById(R.id.tv_greeting);
        tvMonth = v.findViewById(R.id.tv_month);
        tvBalance = v.findViewById(R.id.tv_balance);
        tvIncome = v.findViewById(R.id.tv_month_income);
        tvExpense = v.findViewById(R.id.tv_month_expense);
        tvBudgetTitle = v.findViewById(R.id.tv_budget_title);
        tvBudgetRemain = v.findViewById(R.id.tv_budget_remain);
        tvBudgetSpent = v.findViewById(R.id.tv_budget_spent);
        tvBudgetPercent = v.findViewById(R.id.tv_budget_percent);
        tvBudgetEmpty = v.findViewById(R.id.tv_budget_empty);
        layoutBudgetContent = v.findViewById(R.id.layout_budget_content);
        progressBudget = v.findViewById(R.id.progress_budget);
        btnSetBudget = v.findViewById(R.id.btn_set_budget);
        layoutEmpty = v.findViewById(R.id.layout_empty);

        RecyclerView rv = v.findViewById(R.id.rv_recent);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setNestedScrollingEnabled(false);
        adapter = new TransactionAdapter(false, this::openEdit);
        rv.setAdapter(adapter);

        v.findViewById(R.id.tv_see_all).setOnClickListener(x -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateTo(R.id.nav_transactions);
            }
        });
        v.findViewById(R.id.btn_empty_add).setOnClickListener(x ->
                startActivity(new Intent(requireContext(), AddEditTransactionActivity.class)));
        btnSetBudget.setOnClickListener(x -> {
            Calendar now = Calendar.getInstance();
            DialogUtils.showBudgetDialog(requireContext(), DateUtils.monthKey(now),
                    DateUtils.monthTitle(now), this::loadData);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void openEdit(Transaction t) {
        Intent i = new Intent(requireContext(), AddEditTransactionActivity.class);
        i.putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_ID, t.getId());
        startActivity(i);
    }

    private void loadData() {
        Calendar now = Calendar.getInstance();
        long start = DateUtils.startOfMonth(now);
        long end = DateUtils.endOfMonth(now);

        tvGreeting.setText("Xin chào, " + AppPrefs.getUserName(requireContext()));
        tvMonth.setText("Tổng quan · " + DateUtils.monthTitle(now));

        double balance = transactionDAO.getTotalBalance();
        double income = transactionDAO.sumByRange(start, end, Transaction.TYPE_INCOME);
        double expense = transactionDAO.sumByRange(start, end, Transaction.TYPE_EXPENSE);
        tvBalance.setText(CurrencyUtils.format(balance));
        tvIncome.setText(CurrencyUtils.format(income));
        tvExpense.setText(CurrencyUtils.format(expense));

        bindBudget(now, expense);

        List<Transaction> recent = transactionDAO.getRecent(5);
        adapter.setData(recent);
        layoutEmpty.setVisibility(recent.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void bindBudget(Calendar now, double spent) {
        tvBudgetTitle.setText("Ngân sách " + DateUtils.monthTitle(now).toLowerCase(DateUtils.VI));
        double limit = budgetDAO.getLimit(DateUtils.monthKey(now));

        if (limit <= 0) {
            layoutBudgetContent.setVisibility(View.GONE);
            tvBudgetEmpty.setVisibility(View.VISIBLE);
            btnSetBudget.setText("Thiết lập");
            return;
        }
        layoutBudgetContent.setVisibility(View.VISIBLE);
        tvBudgetEmpty.setVisibility(View.GONE);
        btnSetBudget.setText("Sửa");

        double percent = spent * 100 / limit;
        int colorRes;
        if (percent >= 100) colorRes = R.color.expense;
        else if (percent >= 80) colorRes = R.color.warning;
        else colorRes = R.color.income;
        int color = ContextCompat.getColor(requireContext(), colorRes);

        progressBudget.setIndicatorColor(color);
        progressBudget.setProgressCompat((int) Math.min(100, Math.round(percent)), true);
        tvBudgetPercent.setText(String.format(Locale.US, "%d%%", Math.round(percent)));
        tvBudgetPercent.setTextColor(color);
        tvBudgetSpent.setText("Đã chi " + CurrencyUtils.format(spent) + " / " + CurrencyUtils.format(limit));

        if (spent > limit) {
            tvBudgetRemain.setText("Vượt " + CurrencyUtils.format(spent - limit));
        } else {
            tvBudgetRemain.setText("Còn lại " + CurrencyUtils.format(limit - spent));
        }
        tvBudgetRemain.setTextColor(color);
    }
}
