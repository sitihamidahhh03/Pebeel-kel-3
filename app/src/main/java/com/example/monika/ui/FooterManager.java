package com.example.monika.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import com.example.monika.DashboardActivity;
import com.example.monika.NotificationActivity;
import com.example.monika.R;

public class FooterManager {

    private Activity activity;
    private View indicatorHome, indicatorChart, indicatorAlarm, indicatorBell;
    private ImageView menuHome, menuChart, menuAlarm, menuBell;
    // Tambahkan variabel container supaya area kliknya lebih luas (nyaman di jempol)
    private View containerHome, containerChart, containerAlarm, containerBell;

    private final int COLOR_ACTIVE = Color.WHITE;
    private final int COLOR_INACTIVE = Color.parseColor("#7A966B");

    public FooterManager(Activity activity) {
        this.activity = activity;
        initFooter();
    }

    private void initFooter() {
        // 1. Hubungkan ke XML (Indikator & Icon)
        indicatorHome = activity.findViewById(R.id.indicator_home);
        indicatorChart = activity.findViewById(R.id.indicator_chart);
        indicatorAlarm = activity.findViewById(R.id.indicator_alarm);
        indicatorBell = activity.findViewById(R.id.indicator_bell);

        menuHome = activity.findViewById(R.id.menu_home);
        menuChart = activity.findViewById(R.id.menu_chart);
        menuAlarm = activity.findViewById(R.id.menu_alarm);
        menuBell = activity.findViewById(R.id.menu_bell);

        // 2. Hubungkan ke Container (FrameLayout) agar kliknya gampang
        containerHome = activity.findViewById(R.id.container_home);
        containerChart = activity.findViewById(R.id.container_chart);
        containerAlarm = activity.findViewById(R.id.container_alarm);
        containerBell = activity.findViewById(R.id.container_bell);

        // 3. LOGIKA PINDAH HALAMAN (INTENT) - INI YANG TADI KURANG
        if (containerHome != null) {
            containerHome.setOnClickListener(v -> {
                // Jangan pindah kalau kita sudah di Dashboard
                if (!(activity instanceof DashboardActivity)) {
                    Intent intent = new Intent(activity, DashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(0, 0); // Tanpa animasi biar gak kedip
                }
            });
        }

        if (containerBell != null) {
            containerBell.setOnClickListener(v -> {
                // Jangan pindah kalau kita sudah di Notifikasi
                if (!(activity instanceof NotificationActivity)) {
                    Intent intent = new Intent(activity, NotificationActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(0, 0);
                }
            });
        }

        // Catatan: Lakukan hal yang sama untuk menuChart dan menuAlarm jika nanti sudah ada Activity-nya.
    }

    public void setActiveMenu(int activeIndicatorID) {
        resetAll();
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
            indicator.setAlpha(1f);
            icon.setColorFilter(COLOR_ACTIVE);
        }
    }

    private void resetAll() {
        View[] indicators = {indicatorHome, indicatorChart, indicatorAlarm, indicatorBell};
        ImageView[] icons = {menuHome, menuChart, menuAlarm, menuBell};
        for (View v : indicators) if (v != null) v.setVisibility(View.GONE);
        for (ImageView img : icons) if (img != null) img.setColorFilter(COLOR_INACTIVE);
    }
}