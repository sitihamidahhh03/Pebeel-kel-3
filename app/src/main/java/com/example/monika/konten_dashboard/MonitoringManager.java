package com.example.monika.konten_dashboard;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.monika.R;
import java.util.Random;

public class MonitoringManager {

    private Activity activity;
    private Handler handler;
    private Runnable runnable;
    private ProgressBar progressBar;
    private TextView tvStatus, tvPercentage, tvPesanSaran;
    private View cardSaran;

    public MonitoringManager(Activity activity) {
        this.activity = activity;
        // Hubungkan ke ID yang ada di activity_dashboard.xml
        this.progressBar = activity.findViewById(R.id.progressBar);
        this.tvStatus = activity.findViewById(R.id.tvStatus);
        this.tvPercentage = activity.findViewById(R.id.tvPercentage);
        
        // Hubungkan ke ID yang ada di komponen_saran_dashboard.xml
        this.tvPesanSaran = activity.findViewById(R.id.tvPesanSaran);
        this.cardSaran = activity.findViewById(R.id.cardSaran);

        this.handler = new Handler(Looper.getMainLooper());
        startMonitoring();
    }

    private void startMonitoring() {
        runnable = new Runnable() {
            @Override
            public void run() {
                // 1. Buat angka acak 0-100 (Dummy Sensor)
                int randomValue = new Random().nextInt(101);

                // 2. Update Tampilan & Pesan Saran
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

        String status;
        String saran;
        int color;

        if (value <= 30) {
            status = "Kering/Kritis";
            saran = "Tanah terlalu kering (" + value + "%). Tanaman berisiko layu permanen. Tindakan: Segera nyalakan penyiraman.";
            color = Color.parseColor("#FF5252"); // Merah
        } else if (value <= 60) {
            status = "Optimal/Sedang";
            saran = "Kondisi ideal untuk kebanyakan tanaman (" + value + "%). Akar bisa menyerap air dan oksigen dengan seimbang.";
            color = Color.parseColor("#8BAE66"); // Hijau
        } else if (value <= 80) {
            status = "Basah";
            saran = "Tanah sangat lembap (" + value + "%). Cocok untuk tanaman yang suka air, namun waspadai jamur jika bertahan terlalu lama.";
            color = Color.parseColor("#448AFF"); // Biru
        } else {
            status = "Jenuh/Banjir";
            saran = "Tanah tergenang air (" + value + "%). Berisiko menyebabkan pembusukan akar. Tindakan: Hentikan penyiraman dan perbaiki drainase.";
            color = Color.parseColor("#795548"); // Cokelat
        }

        tvStatus.setText(status);
        tvStatus.setTextColor(color);
        progressBar.setProgressTintList(ColorStateList.valueOf(color));

        // Update teks saran secara dinamis
        if (tvPesanSaran != null) {
            tvPesanSaran.setText(saran);
        }
    }

    public void stopMonitoring() {
        if (handler != null) handler.removeCallbacks(runnable);
    }
}