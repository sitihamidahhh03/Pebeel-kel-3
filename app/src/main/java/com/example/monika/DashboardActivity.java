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

    private ClockManager clockManager;
    private MonitoringManager monitoringManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 1. Header & Footer
        HeaderManager header = new HeaderManager(this); // Simpan ke variabel header
        header.setHeaderTitle("Dashboard");            // Tambahkan baris ini untuk ubah judul

        FooterManager footer = new FooterManager(this);
        footer.setActiveMenu(R.id.indicator_home);

        // 2. Jam & Tanggal
        TextView tvTime = findViewById(R.id.tvTime);
        TextView tvDate = findViewById(R.id.tvDate);
        clockManager = new ClockManager(tvTime, tvDate);

        // 3. Monitoring (10 Detik)
        monitoringManager = new MonitoringManager(this);

        // 4. Switch Siram
        SwitchCompat switchSiram = findViewById(R.id.switchSiram);
        if (switchSiram != null) {
            new WateringManager(this, switchSiram);
        }
    } // <--- TUTUP ONCREATE DI SINI

    @Override
    protected void onDestroy() { // <--- ONDESTROY BERDIRI SENDIRI DI LUAR
        super.onDestroy();
        if (clockManager != null) {
            clockManager.stopClock();
        }
        if (monitoringManager != null) {
            monitoringManager.stopMonitoring();
        }
    } // <--- TUTUP ONDESTROY
} // <--- TUTUP CLASS UTAMA