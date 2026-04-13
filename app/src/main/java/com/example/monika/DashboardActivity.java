package com.example.monika;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.monika.ui.FooterManager;
import com.example.monika.ui.HeaderManager;

public class DashboardActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private FooterManager footer;
    private HeaderManager header;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 1. Header & ViewPager Setup
        header = new HeaderManager(this);
        viewPager = findViewById(R.id.viewPager);
        
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // 2. Footer Setup
        footer = new FooterManager(this);
        footer.setPager(viewPager); // Hubungkan footer dengan ViewPager

        // 3. Sinkronisasi ViewPager ke Footer (saat digeser)
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateFooterAndHeader(position);
            }
        });
    }

    private void updateFooterAndHeader(int position) {
        switch (position) {
            case 0:
                header.setHeaderTitle("Dashboard");
                footer.setActiveMenu(R.id.indicator_home);
                break;
            case 1:
                header.setHeaderTitle("Grafik");
                footer.setActiveMenu(R.id.indicator_chart);
                break;
            case 2:
                header.setHeaderTitle("Pengingat");
                footer.setActiveMenu(R.id.indicator_alarm);
                break;
            case 3:
                header.setHeaderTitle("Notifikasi");
                footer.setActiveMenu(R.id.indicator_bell);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (header != null) {
            header.loadProfilePhoto();
        }
    }
}
