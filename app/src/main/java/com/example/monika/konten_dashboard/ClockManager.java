package com.example.monika.konten_dashboard;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ClockManager {

    private final TextView tvTime, tvDate;
    private final Handler handler;
    private Runnable runnable;

    public ClockManager(TextView tvTime, TextView tvDate) {
        this.tvTime = tvTime;
        this.tvDate = tvDate;
        this.handler = new Handler(Looper.getMainLooper());
        startClock();
    }

    private void startClock() {
        runnable = new Runnable() {
            @Override
            public void run() {
                // 1. Ambil data waktu sekarang
                Calendar calendar = Calendar.getInstance();

                // 2. Format Jam (Contoh: 13:45 WITA)
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String currentTime = timeFormat.format(calendar.getTime()) + " WITA";

                // 3. Format Tanggal (Contoh: Minggu, 22 Maret 2026)
                // Locale id_ID supaya harinya Bahasa Indonesia (Senin, Selasa, dst)
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("id", "ID"));
                String currentDate = dateFormat.format(calendar.getTime());

                // 4. Update Tampilan ke UI
                if (tvTime != null) tvTime.setText(currentTime);
                if (tvDate != null) tvDate.setText(currentDate);

                // 5. Ulangi setiap 1 detik (1000ms) agar jam terus berjalan
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    // Fungsi untuk mematikan jam (dipanggil di onDestroy Activity)
    public void stopClock() {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
}