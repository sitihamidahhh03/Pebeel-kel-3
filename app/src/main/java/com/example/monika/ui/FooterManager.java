package com.example.monika.ui;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import androidx.viewpager2.widget.ViewPager2;
import com.example.monika.R;

public class FooterManager {

    private Activity activity;
    private View indicatorHome, indicatorChart, indicatorAlarm, indicatorBell;
    private ImageView menuHome, menuChart, menuAlarm, menuBell;
    private View containerHome, containerChart, containerAlarm, containerBell;
    private ViewPager2 viewPager;

    private final int COLOR_ACTIVE = Color.WHITE;
    private final int COLOR_INACTIVE = Color.parseColor("#7A966B");

    public FooterManager(Activity activity) {
        this.activity = activity;
        initFooter();
    }

    public void setPager(ViewPager2 viewPager) {
        this.viewPager = viewPager;
    }

    private void initFooter() {
        indicatorHome = activity.findViewById(R.id.indicator_home);
        indicatorChart = activity.findViewById(R.id.indicator_chart);
        indicatorAlarm = activity.findViewById(R.id.indicator_alarm);
        indicatorBell = activity.findViewById(R.id.indicator_bell);

        menuHome = activity.findViewById(R.id.menu_home);
        menuChart = activity.findViewById(R.id.menu_chart);
        menuAlarm = activity.findViewById(R.id.menu_alarm);
        menuBell = activity.findViewById(R.id.menu_bell);

        containerHome = activity.findViewById(R.id.container_home);
        containerChart = activity.findViewById(R.id.container_chart);
        containerAlarm = activity.findViewById(R.id.container_alarm);
        containerBell = activity.findViewById(R.id.container_bell);

        if (containerHome != null) {
            containerHome.setOnClickListener(v -> {
                if (viewPager != null) viewPager.setCurrentItem(0, true);
            });
        }

        if (containerChart != null) {
            containerChart.setOnClickListener(v -> {
                if (viewPager != null) viewPager.setCurrentItem(1, true);
            });
        }

        if (containerAlarm != null) {
            containerAlarm.setOnClickListener(v -> {
                if (viewPager != null) viewPager.setCurrentItem(2, true);
            });
        }

        if (containerBell != null) {
            containerBell.setOnClickListener(v -> {
                if (viewPager != null) viewPager.setCurrentItem(3, true);
            });
        }
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
