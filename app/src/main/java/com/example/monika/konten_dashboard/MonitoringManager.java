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
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.example.monika.DashboardActivity;
import com.example.monika.MonitoringRepository;
import com.example.monika.NotificationRepository;
import com.example.monika.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MonitoringManager {

    private Activity activity;
    private ProgressBar progressBar;
    private TextView tvStatus, tvPercentage, tvPesanSaran;
    private ImageView ivIconPeringatan;
    private View cardSaran;
    private WateringManager wateringManager;
    private static final String CHANNEL_ID = "syram_notifications";
    private DatabaseReference monitoringRef;
    private ValueEventListener monitoringListener;

    public MonitoringManager(View rootView, WateringManager wateringManager) {
        this.activity = (Activity) rootView.getContext();
        this.wateringManager = wateringManager;
        
        this.progressBar = rootView.findViewById(R.id.progressBar);
        this.tvStatus = rootView.findViewById(R.id.tvStatus);
        this.tvPercentage = rootView.findViewById(R.id.tvPercentage);
        this.tvPesanSaran = rootView.findViewById(R.id.tvPesanSaran);
        this.ivIconPeringatan = rootView.findViewById(R.id.ivIconPeringatan);
        this.cardSaran = rootView.findViewById(R.id.cardSaran);

        createNotificationChannel();
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
        String dbUrl = "https://syram-iot-default-rtdb.asia-southeast1.firebasedatabase.app/";
        // Tahap 2 & 3: Read Real-time dari node Sensor/Kelembapan
        monitoringRef = FirebaseDatabase.getInstance(dbUrl).getReference("Sensor/Kelembapan");

        monitoringListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        Object val = snapshot.getValue();
                        int value = 0;
                        if (val instanceof Long) {
                            value = ((Long) val).intValue();
                        } else if (val instanceof Double) {
                            value = ((Double) val).intValue();
                        } else if (val instanceof String) {
                            value = Integer.parseInt((String) val);
                        }
                        updateDisplay(value);
                    } catch (Exception e) {
                        Log.e("FIREBASE_READ", "Error parsing: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FIREBASE_READ", "Gagal baca: " + error.getMessage());
            }
        };
        monitoringRef.addValueEventListener(monitoringListener);
    }

    private void updateDisplay(int value) {
        if (progressBar == null) return;

        tvPercentage.setText(value + "%");
        progressBar.setProgress(value);

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

        if (wateringManager != null) {
            wateringManager.checkAutoWatering(value);
        }
    }

    public void stopMonitoring() {
        if (monitoringRef != null && monitoringListener != null) {
            monitoringRef.removeEventListener(monitoringListener);
        }
    }
}
