package com.example.monika;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.monika.ui.FooterManager;
import com.example.monika.ui.HeaderManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Random;

public class GrafikActivity extends AppCompatActivity {

    BarChart barChart;
    com.github.mikephil.charting.charts.PieChart pieChart;
    SwipeRefreshLayout swipeRefresh;

    String[] hari = {"Senin","Selasa","Rabu","Kamis","Jumat","Sabtu","Minggu"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grafik);

        // 1. Inisialisasi Header & Footer
        HeaderManager header = new HeaderManager(this);
        header.setHeaderTitle("Grafik Kelembaban");

        FooterManager footer = new FooterManager(this);
        footer.setActiveMenu(R.id.indicator_chart);

        // 2. Inisialisasi Chart & Layout
        barChart = findViewById(R.id.barChart);
        pieChart = findViewById(R.id.pieChart);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        loadDummyData();

        swipeRefresh.setOnRefreshListener(() -> {
            new Handler().postDelayed(() -> {
                loadDummyData(); // refresh data
                swipeRefresh.setRefreshing(false);
            }, 1000);
        });
    }

    private void loadDummyData() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        Random random = new Random();

        float total = 0;
        for (int i = 0; i < 7; i++) {
            float value = 20 + random.nextInt(61); // 20 - 80 %
            entries.add(new BarEntry(i, value));
            total += value;
        }
        float avg = total / 7;

        BarDataSet dataSet = new BarDataSet(entries, "Kelembaban");

        dataSet.setColor(Color.parseColor("#628141"));
        dataSet.setValueTextColor(Color.parseColor("#1B211A"));
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        barChart.setData(data);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(hari));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.parseColor("#1B211A"));

        barChart.getAxisLeft().setTextColor(Color.parseColor("#1B211A"));
        barChart.getAxisRight().setEnabled(false);

        barChart.setBackgroundColor(Color.parseColor("#FFFFFF"));


        Description desc = new Description();
        desc.setText("");
        barChart.setDescription(desc);

        barChart.animateY(800);
        barChart.invalidate(); // refresh chart

        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(avg));
        pieEntries.add(new PieEntry(100 - avg));

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        pieDataSet.setColors(
                Color.parseColor("#628141"),
                Color.parseColor("#E0E0E0")
        );
        pieDataSet.setDrawValues(false);

        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(70f);
        pieChart.setTransparentCircleRadius(75f);

        pieChart.setDrawEntryLabels(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.getDescription().setEnabled(false);

        pieChart.setCenterText(Math.round(avg) + "%");
        pieChart.setCenterTextSize(18f);
        pieChart.setCenterTextColor(Color.parseColor("#1B211A"));

        pieChart.animateY(800);
        pieChart.invalidate();
    }
}
