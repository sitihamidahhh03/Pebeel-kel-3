package com.example.monika.konten_dashboard;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ClockManager {

    private TextView tvTime;
    private TextView tvDate;
    private Handler handler;
    private Runnable runnable;
    private boolean isRunning;

    public ClockManager(TextView tvTime, TextView tvDate) {
        this.tvTime = tvTime;
        this.tvDate = tvDate;
        this.handler = new Handler(Looper.getMainLooper());
        startClock();
    }

    public void startClock() {
        if (isRunning) return;
        isRunning = true;

        runnable = new Runnable() {
            @Override
            public void run() {
                updateTime();
                if (isRunning) {
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(runnable);
    }

    private void updateTime() {
        Date now = new Date();

        // Format waktu
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        if (tvTime != null) {
            tvTime.setText(timeFormat.format(now));
        }

        // Format tanggal
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("id", "ID"));
        if (tvDate != null) {
            tvDate.setText(dateFormat.format(now));
        }
    }

    public void stopClock() {
        isRunning = false;
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
}