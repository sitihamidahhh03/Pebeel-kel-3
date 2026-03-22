package com.example.monika;

import android.os.Bundle;
import android.widget.TextView; // Wajib import ini
import androidx.appcompat.app.AppCompatActivity;

// 1. IMPORT Manager dari folder ui
import com.example.monika.ui.FooterManager;
import com.example.monika.ui.HeaderManager;
import com.example.monika.konten_dashboard.ClockManager; // Import ClockManager yang baru dibuat

public class DashboardActivity extends AppCompatActivity {

    private ClockManager clockManager; // Simpan di variabel agar bisa dihentikan nanti

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Pastikan layout yang dipanggil benar
        setContentView(R.layout.activity_dashboard);

        // 2. AKTIFKAN HEADER
        new HeaderManager(this);

        // 3. AKTIFKAN FOOTER
        FooterManager footer = new FooterManager(this);
        // Set Indikator HOME aktif saat pertama kali buka
        footer.setActiveMenu(R.id.indicator_home);

        // 4. AKTIFKAN CLOCK (JAM & TANGGAL OTOMATIS)
        // Pastikan ID tvTime dan tvDate sudah ada di activity_dashboard.xml
        TextView tvTime = findViewById(R.id.tvTime);
        TextView tvDate = findViewById(R.id.tvDate);

        // Hidupkan mesin jam
        clockManager = new ClockManager(tvTime, tvDate);
    }

    // PENTING: Matikan mesin jam saat Activity dihancurkan/ditutup
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clockManager != null) {
            clockManager.stopClock();
        }
    }
}