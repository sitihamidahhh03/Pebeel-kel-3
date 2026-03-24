package com.example.monika.ui;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import com.example.monika.R;

public class FooterManager {

    private Activity activity;
    private View indicatorHome, indicatorChart, indicatorAlarm, indicatorBell;
    private ImageView menuHome, menuChart, menuAlarm, menuBell;

    // Warna saat Aktif (di atas kapsul hijau) dan Tidak Aktif
    private final int COLOR_ACTIVE = Color.WHITE;
    private final int COLOR_INACTIVE = Color.parseColor("#7A966B"); // Hijau pudar

    public FooterManager(Activity activity) {
        this.activity = activity;
        initFooter();
    }

    private void initFooter() {
        // 1. Hubungkan ke XML (Indikator Kapsul)
        indicatorHome = activity.findViewById(R.id.indicator_home);
        indicatorChart = activity.findViewById(R.id.indicator_chart);
        indicatorAlarm = activity.findViewById(R.id.indicator_alarm);
        indicatorBell = activity.findViewById(R.id.indicator_bell);

        // 2. Hubungkan ke XML (Icon Menu)
        menuHome = activity.findViewById(R.id.menu_home);
        menuChart = activity.findViewById(R.id.menu_chart);
        menuAlarm = activity.findViewById(R.id.menu_alarm);
        menuBell = activity.findViewById(R.id.menu_bell);

        // 3. Logika Klik (Hanya ganti status di satu halaman)
        if (menuHome != null) menuHome.setOnClickListener(v -> setActiveMenu(R.id.indicator_home));
        if (menuChart != null) menuChart.setOnClickListener(v -> setActiveMenu(R.id.indicator_chart));
        if (menuAlarm != null) menuAlarm.setOnClickListener(v -> setActiveMenu(R.id.indicator_alarm));
        if (menuBell != null) menuBell.setOnClickListener(v -> setActiveMenu(R.id.indicator_bell));
    }

    public void setActiveMenu(int activeIndicatorID) {
        // Reset SEMUA Indikator jadi GONE dan Icon jadi warna pudar
        resetAll();

        // Aktifkan yang dipilih
        if (activeIndicatorID == R.id.indicator_home) {
            updateUI(indicatorHome, menuHome);
        } else if (activeIndicatorID == R.id.indicator_chart) {
            updateUI(indicatorChart, menuChart);
        } else if (activeIndicatorID == R.id.indicator_alarm) {
            updateUI(indicatorAlarm, menuAlarm);
        } else if (activeIndicatorID == R.id.indicator_bell) {
            updateUI(indicatorBell, menuBell);
        }
    }

    private void updateUI(View indicator, ImageView icon) {
        if (indicator != null && icon != null) {
            indicator.setVisibility(View.VISIBLE);
            indicator.setAlpha(0f);
            indicator.animate().alpha(1f).setDuration(200).start(); // Animasi halus
            icon.setColorFilter(COLOR_ACTIVE); // Icon jadi Putih
        }
    }

    private void resetAll() {
        View[] indicators = {indicatorHome, indicatorChart, indicatorAlarm, indicatorBell};
        ImageView[] icons = {menuHome, menuChart, menuAlarm, menuBell};

        for (View v : indicators) if (v != null) v.setVisibility(View.GONE);
        for (ImageView img : icons) if (img != null) img.setColorFilter(COLOR_INACTIVE);
    }
}