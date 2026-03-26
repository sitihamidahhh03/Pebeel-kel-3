package com.example.monika.konten_dashboard;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class MonitoringManager {

    private Activity activity;
    private Handler handler;
    private Runnable runnable;
    private boolean isRunning;

    public MonitoringManager(Activity activity) {
        this.activity = activity;
        this.handler = new Handler(Looper.getMainLooper());
        startMonitoring();
    }

    public void startMonitoring() {
        if (isRunning) return;
        isRunning = true;

        runnable = new Runnable() {
            @Override
            public void run() {
                // Lakukan monitoring setiap 10 detik
                performMonitoring();
                if (isRunning) {
                    handler.postDelayed(this, 10000); // 10 detik
                }
            }
        };
        handler.post(runnable);
    }

    private void performMonitoring() {
        // TODO: Implementasi monitoring tanaman
        // Contoh: cek kelembaban tanah, suhu, dll
        // Toast.makeText(activity, "Monitoring berjalan...", Toast.LENGTH_SHORT).show();
    }

    public void stopMonitoring() {
        isRunning = false;
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
}