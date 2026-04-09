package com.example.monika.konten_dashboard;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.core.app.NotificationCompat;
import com.example.monika.DashboardActivity;
import com.example.monika.MonitoringRepository;
import com.example.monika.NotificationActivity;
import com.example.monika.NotificationRepository;
import com.example.monika.R;
import java.util.Random;

public class MonitoringManager {

    private Activity activity;
    private Handler handler;
    private Runnable runnable;
    private ProgressBar progressBar;
    private TextView tvStatus, tvPercentage, tvPesanSaran;
    private ImageView ivIconPeringatan;
    private View cardSaran;
    private static final String CHANNEL_ID = "syram_notifications";

    public MonitoringManager(Activity activity) {
        this.activity = activity;
        this.progressBar = activity.findViewById(R.id.progressBar);
        this.tvStatus = activity.findViewById(R.id.tvStatus);
        this.tvPercentage = activity.findViewById(R.id.tvPercentage);
        this.tvPesanSaran = activity.findViewById(R.id.tvPesanSaran);
        this.ivIconPeringatan = activity.findViewById(R.id.ivIconPeringatan);
        this.cardSaran = activity.findViewById(R.id.cardSaran);

        createNotificationChannel();
        this.handler = new Handler(Looper.getMainLooper());
        startMonitoring();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "SYRAM Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = activity.getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void sendSystemNotification(String title, String message, String status) {
        Intent intent = new Intent(activity, DashboardActivity.class);
        intent.putExtra("OPEN_STATUS", status);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pi = PendingIntent.getActivity(activity, status.hashCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(activity, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi)
                .setAutoCancel(true);

        NotificationManager nm = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(101, builder.build());

        NotificationRepository.addNotification(activity, title, message, status);
    }

    private void startMonitoring() {
        runnable = new Runnable() {
            @Override
            public void run() {
                int randomValue = new Random().nextInt(101);
                updateDisplay(randomValue);
                handler.postDelayed(this, 10000);
            }
        };
        handler.post(runnable);
    }

    private void updateDisplay(int value) {
        if (progressBar == null) return;

        tvPercentage.setText(value + "%");
        progressBar.setProgress(value);

        // --- SIMPAN DATA KE REPOSITORY AGAR GRAFIK SINKRON ---
        MonitoringRepository.updateTodayValue(activity, value);

        String status;
        String saran;
        int color;
        
        if (value < 60) {
            status = "Kering";
            saran = "Tanah mulai kering (" + value + "%). Cabai butuh kelembaban 60-80%. Tindakan: Segera nyalakan penyiraman.";
            color = Color.parseColor("#FF5252"); 
            sendSystemNotification("SYRAM: Kelembaban Rendah!", saran, "KERING");
        } else if (value <= 80) {
            status = "Optimal";
            saran = "Kondisi ideal untuk cabai (" + value + "%). Pertumbuhan maksimal karena kebutuhan air terpenuhi.";
            color = Color.parseColor("#8BAE66"); 
        } else if (value <= 90) {
            status = "Basah";
            saran = "Tanah cukup basah (" + value + "%). Pantau terus agar tidak terjadi genangan air berkepanjangan.";
            color = Color.parseColor("#448AFF"); 
            sendSystemNotification("SYRAM: Tanah Basah", saran, "TINGGI");
        } else {
            status = "Banjir";
            saran = "Tanah tergenang (" + value + "%). Berisiko menyebabkan pembusukan akar. Tindakan: Hentikan penyiraman.";
            color = Color.parseColor("#795548"); 
            sendSystemNotification("SYRAM: PERINGATAN BANJIR!", saran, "BANJIR");
        }

        tvStatus.setText(status);
        tvStatus.setTextColor(color);
        progressBar.setProgressTintList(ColorStateList.valueOf(color));

        if (tvPesanSaran != null) tvPesanSaran.setText(saran);
        if (ivIconPeringatan != null) ivIconPeringatan.setImageTintList(ColorStateList.valueOf(color));
    }

    public void stopMonitoring() {
        if (handler != null) handler.removeCallbacks(runnable);
    }
}
