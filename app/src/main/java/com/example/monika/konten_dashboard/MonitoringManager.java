package com.example.monika.konten_dashboard;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.monika.R;
import java.util.Random;

public class MonitoringManager {

    private Activity activity;
    private Handler handler;
    private Runnable runnable;
    private ProgressBar progressBar;
    private TextView tvStatus, tvPercentage;

    public MonitoringManager(Activity activity) {
        this.activity = activity;
        // Hubungkan ke ID yang ada di activity_dashboard.xml
        this.progressBar = activity.findViewById(R.id.progressBar);
        this.tvStatus = activity.findViewById(R.id.tvStatus);
        this.tvPercentage = activity.findViewById(R.id.tvPercentage);

        this.handler = new Handler(Looper.getMainLooper());
        startMonitoring();
    }

    private void startMonitoring() {
        runnable = new Runnable() {
            @Override
            public void run() {
                // 1. Buat angka acak 0-100
                int randomValue = new Random().nextInt(101);

                // 2. Update Tampilan & Warna
                updateDisplay(randomValue);

                // 3. Ulangi tiap 10 detik
                handler.postDelayed(this, 10000);
            }
        };
        handler.post(runnable);
    }

    private void updateDisplay(int value) {
        if (progressBar == null) return;

        tvPercentage.setText(value + "%");
        progressBar.setProgress(value);

        // LOGIKA WARNA
        int color;
        String status;

        if (value <= 49) {
            status = "Kering";
            color = Color.parseColor("#FF5252"); // Merah
        } else if (value <= 80) {
            status = "Normal";
            color = Color.parseColor("#8BAE66"); // Hijau
        } else {
            status = "Lembap";
            color = Color.parseColor("#448AFF"); // Biru
        }

        tvStatus.setText(status);
        tvStatus.setTextColor(color);
        // Ganti warna Progress Bar secara otomatis
        progressBar.setProgressTintList(ColorStateList.valueOf(color));
    }

    public void stopMonitoring() {
        if (handler != null) handler.removeCallbacks(runnable);
    }
}