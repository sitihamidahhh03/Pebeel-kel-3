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
    private TextView tvStatus, tvPercentage, tvPesanSaran, tvModeStatus;
    private ImageView ivIconPeringatan;
    private View cardSaran;
    private WateringManager wateringManager;
    private static final String CHANNEL_ID = "syram_notifications";
    private DatabaseReference monitoringRef;
    private ValueEventListener monitoringListener;
    
    private DatabaseReference modeRef;
    private ValueEventListener modeListener;
    private String currentMode = "Manual";
    private int lastSoilValue = 0;

    public MonitoringManager(View rootView, WateringManager wateringManager) {
        this.activity = (Activity) rootView.getContext();
        this.wateringManager = wateringManager;
        
        this.progressBar = rootView.findViewById(R.id.progressBar);
        this.tvStatus = rootView.findViewById(R.id.tvStatus);
        this.tvModeStatus = rootView.findViewById(R.id.tvModeStatus);
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

    private void startMonitoring() {
        String dbUrl = "https://syram-iot-default-rtdb.asia-southeast1.firebasedatabase.app/";
        // DIKEMBALIKAN KE ASLINYA (KAPITAL)
        monitoringRef = FirebaseDatabase.getInstance(dbUrl).getReference("Sensor/Kelembapan");
        modeRef = FirebaseDatabase.getInstance(dbUrl).getReference("Kontrol/Mode");

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
                        lastSoilValue = value;
                        updateDisplay(value);
                    } catch (Exception e) {
                        Log.e("FIREBASE_READ", "Error parsing: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        monitoringRef.addValueEventListener(monitoringListener);

        modeListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentMode = snapshot.getValue(String.class);
                    updateDisplay(lastSoilValue);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        modeRef.addValueEventListener(modeListener);
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
        } else if (value <= 80) {
            status = "Optimal";
            saran = "Kondisi ideal untuk cabai (" + value + "%). Pertumbuhan maksimal karena kebutuhan air terpenuhi.";
            color = Color.parseColor("#8BAE66"); 
        } else if (value <= 90) {
            status = "Basah";
            saran = "Tanah cukup basah (" + value + "%). Pantau terus agar tidak terjadi genangan air berkepanjangan.";
            color = Color.parseColor("#448AFF"); 
        } else {
            status = "Optimal"; // Mengikuti logika asli user
            saran = "Tanah tergenang (" + value + "%). Berisiko menyebabkan pembusukan akar. Tindakan: Hentikan penyiraman.";
            color = Color.parseColor("#795548"); 
        }

        if (tvModeStatus != null) {
            tvModeStatus.setText("Mode: " + currentMode);
        }

        tvStatus.setText(status);
        tvStatus.setTextColor(color);
        progressBar.setProgressTintList(ColorStateList.valueOf(color));

        if (tvPesanSaran != null) tvPesanSaran.setText(saran);
        if (ivIconPeringatan != null) ivIconPeringatan.setImageTintList(ColorStateList.valueOf(color));

        if (wateringManager != null) {
            wateringManager.checkAutoWatering(value);
        }
        
        // Update Kondisi ke Firebase sesuai path asli
        monitoringRef.getParent().child("kondisi").setValue(status);
    }

    public void stopMonitoring() {
        if (monitoringRef != null && monitoringListener != null) monitoringRef.removeEventListener(monitoringListener);
        if (modeRef != null && modeListener != null) modeRef.removeEventListener(modeListener);
    }
}
