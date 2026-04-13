package com.example.monika;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButtonToggleGroup;
import java.util.ArrayList;
import java.util.Calendar;

public class GrafikFragment extends Fragment {

    private BarChart barChart;
    private PieChart pieChart;
    private TextView tvGrafikTitle, tvLabelRange;
    private String currentMode = "WEEK";
    private final String[] hari = {"Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"};
    private final String[] bulan = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des"};
    private String[] dynamicYears;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grafik, container, false);

        barChart = view.findViewById(R.id.barChart);
        pieChart = view.findViewById(R.id.pieChart);
        tvGrafikTitle = view.findViewById(R.id.tvGrafikTitle);
        tvLabelRange = view.findViewById(R.id.tvLabelRange);
        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggleGroup);
        SwipeRefreshLayout swipeRefresh = view.findViewById(R.id.swipeRefresh);

        generateDynamicYears();

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnWeek) {
                    currentMode = "WEEK";
                    tvGrafikTitle.setText("Grafik Minggu Ini");
                    tvLabelRange.setText("Minggu Ini");
                } else if (checkedId == R.id.btnMonth) {
                    currentMode = "MONTH";
                    tvGrafikTitle.setText("Grafik Bulan Ini");
                    tvLabelRange.setText("Bulan Ini");
                } else if (checkedId == R.id.btnYear) {
                    currentMode = "YEAR";
                    tvGrafikTitle.setText("Grafik Tahun Ini");
                    tvLabelRange.setText("Tahun Ini");
                }
                loadGrafikData();
            }
        });

        swipeRefresh.setOnRefreshListener(() -> {
            loadGrafikData();
            swipeRefresh.setRefreshing(false);
        });

        loadGrafikData();
        return view;
    }

    private void generateDynamicYears() {
        int startYear = 2026;
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        if (currentYear < startYear) currentYear = startYear;
        int totalYears = (currentYear - startYear) + 1;
        dynamicYears = new String[totalYears];
        for (int i = 0; i < totalYears; i++) dynamicYears[i] = String.valueOf(startYear + i);
    }

    private void loadGrafikData() {
        if (barChart == null || pieChart == null) return;

        ArrayList<BarEntry> entries = new ArrayList<>();
        String[] labels;
        float[] realData;

        if (currentMode.equals("YEAR")) {
            labels = dynamicYears;
            realData = MonitoringRepository.getYearlyData(getContext(), dynamicYears);
        } else if (currentMode.equals("MONTH")) {
            labels = bulan;
            realData = MonitoringRepository.getMonthlyData(getContext());
        } else {
            labels = hari;
            realData = MonitoringRepository.getWeeklyData(getContext());
        }

        float total = 0;
        for (int i = 0; i < realData.length; i++) {
            entries.add(new BarEntry(i, realData[i]));
            total += realData[i];
        }
        float avg = total / realData.length;

        // Bar Chart Setup
        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(Color.parseColor("#628141"));
        barChart.setData(new BarData(dataSet));
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        barChart.getAxisRight().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.invalidate();

        // Pie Chart Setup
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(avg));
        pieEntries.add(new PieEntry(100 - avg));
        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        pieDataSet.setColors(Color.parseColor("#628141"), Color.parseColor("#E0E0E0"));
        pieChart.setData(new PieData(pieDataSet));
        pieChart.setCenterText(Math.round(avg) + "%");
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();
    }
}
