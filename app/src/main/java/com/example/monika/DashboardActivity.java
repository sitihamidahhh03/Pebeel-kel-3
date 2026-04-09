package com.example.monika;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import com.example.monika.konten_dashboard.ClockManager;
import com.example.monika.konten_dashboard.MonitoringManager;
import com.example.monika.konten_dashboard.WateringManager;
import com.example.monika.ui.FooterManager;
import com.example.monika.ui.HeaderManager;

public class DashboardActivity extends AppCompatActivity {

    private MonitoringManager monitoringManager;
    private ClockManager clockManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 1. Inisialisasi Header & Footer
        HeaderManager header = new HeaderManager(this);
        header.setHeaderTitle(getString(R.string.title_dashboard));

        FooterManager footer = new FooterManager(this);
        footer.setActiveMenu(R.id.indicator_home);

        // 2. Inisialisasi Manager Konten
        TextView tvTime = findViewById(R.id.tvTime);
        TextView tvDate = findViewById(R.id.tvDate);
        clockManager = new ClockManager(tvTime, tvDate);
        
        monitoringManager = new MonitoringManager(this);
        
        SwitchCompat switchSiram = findViewById(R.id.switchSiram);
        new WateringManager(this, switchSiram);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (monitoringManager != null) monitoringManager.stopMonitoring();
        if (clockManager != null) clockManager.stopClock();
    }
}
