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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class GrafikFragment extends Fragment {

    private BarChart barChart;
    private PieChart pieChart;
    private TextView tvGrafikTitle, tvLabelRange, tvDetailTitle, tvNoData;
    private RecyclerView rvDetailData;
    private DetailGrafikAdapter detailAdapter;
    private String currentMode = "WEEK";
    private final String[] hari = {"Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"};
    private final String[] bulan = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des"};
    private String[] dynamicYears;
    private CustomMarkerView markerView;
    private int selectedIndex = -1;
    private final String dbUrl = "https://syram-iot-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grafik, container, false);

        barChart = view.findViewById(R.id.barChart);
        pieChart = view.findViewById(R.id.pieChart);
        tvGrafikTitle = view.findViewById(R.id.tvGrafikTitle);
        tvLabelRange = view.findViewById(R.id.tvLabelRange);
        tvDetailTitle = view.findViewById(R.id.tvDetailTitle);
        tvNoData = view.findViewById(R.id.tvNoData);
        rvDetailData = view.findViewById(R.id.rvDetailData);
        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggleGroup);
        SwipeRefreshLayout swipeRefresh = view.findViewById(R.id.swipeRefresh);

        detailAdapter = new DetailGrafikAdapter();
        rvDetailData.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDetailData.setAdapter(detailAdapter);

        generateDynamicYears();
        setupChartDefaults();

        if (toggleGroup != null) {
            toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (isChecked) {
                    if (checkedId == R.id.btnWeek) currentMode = "WEEK";
                    else if (checkedId == R.id.btnMonth) currentMode = "MONTH";
                    else if (checkedId == R.id.btnYear) currentMode = "YEAR";
                    updateUIForMode();
                    loadGrafikData();
                }
            });
        }

        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(() -> {
                loadGrafikData();
                swipeRefresh.setRefreshing(false);
            });
        }

        loadGrafikData();
        return view;
    }

    private void updateUIForMode() {
        if (currentMode.equals("WEEK")) {
            tvGrafikTitle.setText("Grafik Minggu Ini");
            tvLabelRange.setText("Minggu Ini");
        } else if (currentMode.equals("MONTH")) {
            tvGrafikTitle.setText("Grafik Bulan Ini");
            tvLabelRange.setText("Bulan Ini");
        } else {
            tvGrafikTitle.setText("Grafik Tahun Ini");
            tvLabelRange.setText("Tahun Ini");
        }
        selectedIndex = -1;
        tvDetailTitle.setText("Detail Data Terkumpul");
        detailAdapter.setData(new ArrayList<>());
        tvNoData.setVisibility(View.GONE);
    }

    private void setupChartDefaults() {
        if (barChart == null) return;
        markerView = new CustomMarkerView(getContext(), R.layout.custom_marker_view);
        markerView.setChartView(barChart);
        barChart.setMarker(markerView);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        
        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                selectedIndex = (int) e.getX();
                showDetailData(selectedIndex);
            }
            @Override
            public void onNothingSelected() {
                selectedIndex = -1;
                detailAdapter.setData(new ArrayList<>());
                tvNoData.setVisibility(View.GONE);
            }
        });

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisLeft().setAxisMaximum(100f);
    }

    private void showDetailData(int index) {
        if (getContext() == null) return;
        String label = (currentMode.equals("WEEK")) ? hari[index] : (currentMode.equals("MONTH") ? bulan[index] : dynamicYears[index]);
        tvDetailTitle.setText("Detail Data: " + label);
        tvNoData.setText("Memuat data...");
        tvNoData.setVisibility(View.VISIBLE);
        rvDetailData.setVisibility(View.GONE);

        // AMBIL DATA REAL-TIME DARI FIREBASE (Jauh lebih pasti)
        FirebaseDatabase.getInstance(dbUrl).getReference("Grafik/Details")
                .child(String.valueOf(index))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<DetailEntry> details = new ArrayList<>();
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                String hour = child.getKey();
                                Object val = child.getValue();
                                if (val instanceof Number) {
                                    details.add(new DetailEntry(hour, ((Number) val).floatValue()));
                                }
                            }
                        }

                        if (!details.isEmpty()) {
                            detailAdapter.setData(details);
                            tvNoData.setVisibility(View.GONE);
                            rvDetailData.setVisibility(View.VISIBLE);
                        } else {
                            tvNoData.setText("Belum ada riwayat per jam untuk " + label);
                            tvNoData.setVisibility(View.VISIBLE);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvNoData.setText("Gagal mengambil data.");
                    }
                });
    }

    private void generateDynamicYears() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        dynamicYears = new String[]{String.valueOf(currentYear - 1), String.valueOf(currentYear)};
    }

    private void loadGrafikData() {
        if (barChart == null || pieChart == null || getContext() == null) return;
        float[] realData = currentMode.equals("YEAR") ? MonitoringRepository.getYearlyData(getContext(), dynamicYears) :
                (currentMode.equals("MONTH") ? MonitoringRepository.getMonthlyData(getContext()) : MonitoringRepository.getWeeklyData(getContext()));
        String[] labels = currentMode.equals("YEAR") ? dynamicYears : (currentMode.equals("MONTH") ? bulan : hari);
        if (markerView != null) markerView.setLabels(labels);

        ArrayList<BarEntry> entries = new ArrayList<>();
        float total = 0; int count = 0;
        for (int i = 0; i < realData.length; i++) {
            entries.add(new BarEntry(i, realData[i]));
            if (realData[i] > 0) { total += realData[i]; count++; }
        }
        float avg = count > 0 ? total / count : 0;

        BarDataSet dataSet = new BarDataSet(entries, "Kelembaban");
        dataSet.setColor(Color.parseColor("#628141"));
        dataSet.setHighLightColor(Color.parseColor("#8BAE66"));
        dataSet.setDrawValues(true);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) { return String.format(Locale.GERMAN, "%.1f", value); }
        });

        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.setData(new BarData(dataSet));
        barChart.animateY(1000);
        barChart.invalidate();
        updatePieChart(avg);
    }

    private void updatePieChart(float avg) {
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(avg));
        pieEntries.add(new PieEntry(100 - avg));
        PieDataSet pds = new PieDataSet(pieEntries, "");
        pds.setColors(new int[]{Color.parseColor("#628141"), Color.parseColor("#E0E0E0")});
        pds.setDrawValues(false);
        pieChart.setData(new PieData(pds));
        pieChart.setCenterText(Math.round(avg) + "%");
        pieChart.setHoleRadius(75f);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.invalidate();
    }
}
