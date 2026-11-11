package com.example.expensemanager.ui.fragments;

import android.graphics.Color;
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
import com.example.expensemanager.adapter.StatAdapter;
import com.example.expensemanager.database.TransactionDAO;
import com.example.expensemanager.model.CategoryStat;
import com.example.expensemanager.model.Transaction;
import com.example.expensemanager.utils.CurrencyUtils;
import com.example.expensemanager.utils.DateUtils;
import com.example.expensemanager.utils.IconUtils;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Tab Thống kê: biểu đồ tròn cơ cấu theo danh mục và biểu đồ cột Thu - Chi 6 tháng.
 */
public class StatisticsFragment extends Fragment {

    private static final int MONTHS_IN_BAR_CHART = 6;

    private final Calendar currentMonth = Calendar.getInstance();
    private int statType = Transaction.TYPE_EXPENSE;

    private TextView tvMonth, tvTotalLabel, tvTotal, tvStatsEmpty;
    private PieChart pieChart;
    private BarChart barChart;
    private StatAdapter statAdapter;
    private TransactionDAO dao;

    private int colorText, colorTextSecondary, colorOutline;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        dao = new TransactionDAO(requireContext());
        colorText = ContextCompat.getColor(requireContext(), R.color.text_primary);
        colorTextSecondary = ContextCompat.getColor(requireContext(), R.color.text_secondary);
        colorOutline = ContextCompat.getColor(requireContext(), R.color.outline);

        tvMonth = v.findViewById(R.id.tv_month);
        tvTotalLabel = v.findViewById(R.id.tv_total_label);
        tvTotal = v.findViewById(R.id.tv_total);
        tvStatsEmpty = v.findViewById(R.id.tv_stats_empty);
        pieChart = v.findViewById(R.id.pie_chart);
        barChart = v.findViewById(R.id.bar_chart);

        v.findViewById(R.id.btn_prev_month).setOnClickListener(x -> changeMonth(-1));
        v.findViewById(R.id.btn_next_month).setOnClickListener(x -> changeMonth(1));

        MaterialButtonToggleGroup toggle = v.findViewById(R.id.toggle_type);
        toggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            statType = checkedId == R.id.btn_type_income ? Transaction.TYPE_INCOME : Transaction.TYPE_EXPENSE;
            loadPie();
        });

        RecyclerView rv = v.findViewById(R.id.rv_stats);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setNestedScrollingEnabled(false);
        statAdapter = new StatAdapter();
        rv.setAdapter(statAdapter);

        setupPieChart();
        setupBarChart();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAll();
    }

    private void changeMonth(int delta) {
        currentMonth.add(Calendar.MONTH, delta);
        loadAll();
    }

    private void loadAll() {
        tvMonth.setText(DateUtils.monthTitle(currentMonth));
        loadPie();
        loadBar();
    }

    // ------------------------- Biểu đồ tròn -------------------------

    private void setupPieChart() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(60f);
        pieChart.setTransparentCircleRadius(64f);
        pieChart.setDrawEntryLabels(false);
        pieChart.setRotationEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setCenterTextColor(colorText);
        pieChart.setCenterTextSize(13f);
        pieChart.setNoDataText("Chưa có dữ liệu trong tháng này");
        pieChart.setNoDataTextColor(colorTextSecondary);
    }

    private void loadPie() {
        long start = DateUtils.startOfMonth(currentMonth);
        long end = DateUtils.endOfMonth(currentMonth);
        List<CategoryStat> stats = dao.getStatsByCategory(start, end, statType);

        double total = 0;
        for (CategoryStat s : stats) total += s.getTotal();
        boolean expense = statType == Transaction.TYPE_EXPENSE;
        tvTotalLabel.setText(expense ? "Tổng chi tiêu" : "Tổng thu nhập");
        tvTotal.setText(CurrencyUtils.format(total));
        tvTotal.setTextColor(ContextCompat.getColor(requireContext(), expense ? R.color.expense : R.color.income));

        statAdapter.setData(stats);
        tvStatsEmpty.setVisibility(stats.isEmpty() ? View.VISIBLE : View.GONE);
        pieChart.setVisibility(stats.isEmpty() ? View.GONE : View.VISIBLE);

        if (stats.isEmpty()) {
            pieChart.clear();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        for (CategoryStat s : stats) {
            entries.add(new PieEntry((float) s.getTotal(), s.getName()));
            colors.add(IconUtils.parseColor(s.getColor(), Color.GRAY));
        }
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);
        dataSet.setSelectionShift(6f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(11f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Ẩn nhãn của lát quá nhỏ để tránh chồng chữ
                return value < 5f ? "" : String.format(Locale.US, "%.0f%%", value);
            }
        });

        pieChart.setCenterText(stats.get(0).getName() + "\n" + String.format(Locale.US, "%.0f%%",
                stats.get(0).getPercent()));
        pieChart.setData(data);
        pieChart.highlightValues(null);
        pieChart.animateY(600);
        pieChart.invalidate();
    }

    // ------------------------- Biểu đồ cột -------------------------

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setPinchZoom(false);
        barChart.setScaleEnabled(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.setNoDataTextColor(colorTextSecondary);

        XAxis x = barChart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(1f);
        x.setCenterAxisLabels(true);
        x.setDrawGridLines(false);
        x.setTextColor(colorTextSecondary);
        x.setAxisLineColor(colorOutline);

        YAxis left = barChart.getAxisLeft();
        left.setAxisMinimum(0f);
        left.setTextColor(colorTextSecondary);
        left.setGridColor(colorOutline);
        left.setDrawAxisLine(false);
        left.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return CurrencyUtils.formatShort(value);
            }
        });

        Legend legend = barChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextColor(colorText);
    }

    private void loadBar() {
        List<BarEntry> incomeEntries = new ArrayList<>();
        List<BarEntry> expenseEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = MONTHS_IN_BAR_CHART - 1; i >= 0; i--) {
            Calendar m = (Calendar) currentMonth.clone();
            m.add(Calendar.MONTH, -i);
            long start = DateUtils.startOfMonth(m);
            long end = DateUtils.endOfMonth(m);
            int index = MONTHS_IN_BAR_CHART - 1 - i;
            incomeEntries.add(new BarEntry(index, (float) dao.sumByRange(start, end, Transaction.TYPE_INCOME)));
            expenseEntries.add(new BarEntry(index, (float) dao.sumByRange(start, end, Transaction.TYPE_EXPENSE)));
            labels.add("T" + (m.get(Calendar.MONTH) + 1));
        }

        BarDataSet incomeSet = new BarDataSet(incomeEntries, "Thu nhập");
        incomeSet.setColor(ContextCompat.getColor(requireContext(), R.color.income));
        BarDataSet expenseSet = new BarDataSet(expenseEntries, "Chi tiêu");
        expenseSet.setColor(ContextCompat.getColor(requireContext(), R.color.expense));

        float groupSpace = 0.3f;
        float barSpace = 0.05f;
        float barWidth = 0.3f; // (0.3 + 0.05) * 2 + 0.3 = 1.0

        BarData data = new BarData(incomeSet, expenseSet);
        data.setBarWidth(barWidth);
        data.setDrawValues(false);

        barChart.setData(data);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setAxisMinimum(0f);
        barChart.getXAxis().setAxisMaximum(data.getGroupWidth(groupSpace, barSpace) * MONTHS_IN_BAR_CHART);
        barChart.groupBars(0f, groupSpace, barSpace);
        barChart.animateY(600);
        barChart.invalidate();
    }
}
