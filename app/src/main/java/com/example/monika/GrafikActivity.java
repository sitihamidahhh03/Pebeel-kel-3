package com.example.monika;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.monika.ui.FooterManager;
import com.example.monika.ui.HeaderManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class GrafikActivity extends AppCompatActivity {

    private BarChart barChart;
    private com.github.mikephil.charting.charts.PieChart pieChart;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvGrafikTitle, tvLabelRange;
    private MaterialButtonToggleGroup toggleGroup;

    private Handler autoRefreshHandler;
    private Runnable autoRefreshRunnable;

    private final String[] hari = {"Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"};
    private final String[] bulan = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des"};
    private String[] dynamicYears;

    private String currentMode = "WEEK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grafik);

        // Inisialisasi tahun dinamis
        generateDynamicYears();

        // 1. Inisialisasi Header & Footer
        HeaderManager header = new HeaderManager(this);
        header.setHeaderTitle(getString(R.string.title_grafik));

        FooterManager footer = new FooterManager(this);
        footer.setActiveMenu(R.id.indicator_chart);

        // 2. Inisialisasi Komponen
        barChart = findViewById(R.id.barChart);
        pieChart = findViewById(R.id.pieChart);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        tvGrafikTitle = findViewById(R.id.tvGrafikTitle);
        tvLabelRange = findViewById(R.id.tvLabelRange);
        toggleGroup = findViewById(R.id.toggleGroup);

        // 3. Logika Filter (Toggle Group)
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnWeek) {
                    currentMode = "WEEK";
                    tvGrafikTitle.setText(getString(R.string.label_week));
                    tvLabelRange.setText(getString(R.string.label_week));
                } else if (checkedId == R.id.btnMonth) {
                    currentMode = "MONTH";
                    tvGrafikTitle.setText(getString(R.string.label_month));
                    tvLabelRange.setText(getString(R.string.label_month));
                } else if (checkedId == R.id.btnYear) {
                    currentMode = "YEAR";
                    tvGrafikTitle.setText(getString(R.string.label_year));
                    tvLabelRange.setText(getString(R.string.label_year));
                }
                loadGrafikData();
            }
        });

        loadGrafikData();

        swipeRefresh.setOnRefreshListener(() -> {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                loadGrafikData();
                swipeRefresh.setRefreshing(false);
            }, 1000);
        });

        // Start auto refresh
        startAutoRefresh();
    }

    private void generateDynamicYears() {
        int startYear = 2026;
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        
        if (currentYear < startYear) currentYear = startYear;

        int totalYears = (currentYear - startYear) + 1;
        dynamicYears = new String[totalYears];

        for (int i = 0; i < totalYears; i++) {
            dynamicYears[i] = String.valueOf(startYear + i);
        }
    }

    private void startAutoRefresh() {
        autoRefreshHandler = new Handler(Looper.getMainLooper());
        autoRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadGrafikData();
                autoRefreshHandler.postDelayed(this, 10000);
            }
        };
        autoRefreshHandler.post(autoRefreshRunnable);
    }

    private void loadGrafikData() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        String[] labels;
        float[] realData;

        // AMBIL DATA DARI REPOSITORY AGAR SINKRON DENGAN MONITORING
        if (currentMode.equals("YEAR")) {
            labels = dynamicYears;
            realData = MonitoringRepository.getYearlyData(this, dynamicYears);
        } else if (currentMode.equals("MONTH")) {
            labels = bulan;
            realData = MonitoringRepository.getMonthlyData(this);
        } else {
            labels = hari;
            realData = MonitoringRepository.getWeeklyData(this);
        }

        float total = 0;
        for (int i = 0; i < realData.length; i++) {
            entries.add(new BarEntry(i, realData[i]));
            total += realData[i];
        }
        float avg = total / realData.length;

        // --- SETUP BAR CHART ---
        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(Color.parseColor("#628141"));
        dataSet.setValueTextColor(Color.parseColor("#1B211A"));
        dataSet.setValueTextSize(10f);
        
        BarData data = new BarData(dataSet);
        barChart.setData(data);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        
        if (currentMode.equals("MONTH")) {
            xAxis.setLabelRotationAngle(-45); 
        } else {
            xAxis.setLabelRotationAngle(0);
        }

        barChart.getAxisLeft().setTextColor(Color.parseColor("#1B211A"));
        barChart.getAxisRight().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setExtraOffsets(0, 0, 0, 10);
        
        barChart.invalidate();

        // --- SETUP PIE CHART ---
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(avg));
        pieEntries.add(new PieEntry(100 - avg));

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        pieDataSet.setColors(Color.parseColor("#628141"), Color.parseColor("#E0E0E0"));
        pieDataSet.setDrawValues(false);

        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(70f);
        pieChart.setTransparentCircleRadius(75f);
        
        pieChart.setCenterText(Math.round(avg) + "%");
        pieChart.setCenterTextSize(18f);
        pieChart.setCenterTextColor(Color.parseColor("#1B211A"));
        
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setDrawEntryLabels(false);

        pieChart.invalidate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (autoRefreshHandler != null && autoRefreshRunnable != null) {
            autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
        }
    }
}
