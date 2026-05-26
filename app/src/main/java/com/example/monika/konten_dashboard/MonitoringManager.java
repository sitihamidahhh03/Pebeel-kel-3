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
    
    private DatabaseReference modeRef, pompaRef, kipasRef;
    private ValueEventListener modeListener, pompaListener, kipasListener;
    private String currentMode = "Manual";
    private int lastSoilValue = 0;
    private String lastStatus = "";

    private View layoutStatusManual, dotPompa, dotKipas;
    private TextView tvStatusPompa, tvStatusKipas;

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

        this.layoutStatusManual = rootView.findViewById(R.id.layoutStatusManual);
        this.dotPompa = rootView.findViewById(R.id.dotPompa);
        this.dotKipas = rootView.findViewById(R.id.dotKipas);
        this.tvStatusPompa = rootView.findViewById(R.id.tvStatusPompa);
        this.tvStatusKipas = rootView.findViewById(R.id.tvStatusKipas);

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
        pompaRef = FirebaseDatabase.getInstance(dbUrl).getReference("Kontrol/Pompa");
        kipasRef = FirebaseDatabase.getInstance(dbUrl).getReference("Kontrol/Kipas");

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
                    if (layoutStatusManual != null) {
                        layoutStatusManual.setVisibility("Manual".equalsIgnoreCase(currentMode) ? View.VISIBLE : View.GONE);
                    }
                    updateDisplay(lastSoilValue);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        modeRef.addValueEventListener(modeListener);

        pompaListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isOn = "ON".equalsIgnoreCase(snapshot.getValue(String.class));
                updateIndicator(dotPompa, tvStatusPompa, isOn, "#448AFF");
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        };
        pompaRef.addValueEventListener(pompaListener);

        kipasListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isOn = "ON".equalsIgnoreCase(snapshot.getValue(String.class));
                updateIndicator(dotKipas, tvStatusKipas, isOn, "#FF9800");
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        };
        kipasRef.addValueEventListener(kipasListener);
    }

    private void updateIndicator(View dot, TextView text, boolean isActive, String activeColor) {
        if (dot == null || text == null) return;
        if (isActive) {
            dot.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(activeColor)));
            text.setTextColor(Color.parseColor(activeColor));
            text.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            dot.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0E0E0")));
            text.setTextColor(Color.parseColor("#757575"));
            text.setTypeface(null, android.graphics.Typeface.NORMAL);
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

        // Simpan ke riwayat lokal agar muncul di halaman riwayat Notifikasi
        NotificationRepository.addNotification(activity, title, message, status);
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
            if (!lastStatus.equals("Kering")) {
                sendSystemNotification("SYRAM: Kelembaban Rendah!", saran, "KERING");
            }
        } else if (value <= 80) {
            status = "Optimal";
            saran = "Kondisi ideal untuk cabai (" + value + "%). Pertumbuhan maksimal karena kebutuhan air terpenuhi.";
            color = Color.parseColor("#8BAE66"); 
        } else if (value <= 90) {
            status = "Basah";
            saran = "Tanah cukup basah (" + value + "%). Pantau terus agar tidak terjadi genangan air berkepanjangan.";
            color = Color.parseColor("#448AFF"); 
            if (!lastStatus.equals("Basah")) {
                sendSystemNotification("SYRAM: Tanah Basah", saran, "TINGGI");
            }
        } else {
            status = "Banjir";
            saran = "Tanah tergenang (" + value + "%). Berisiko menyebabkan pembusukan akar. Tindakan: Hentikan penyiraman.";
            color = Color.parseColor("#795548"); 
            if (!lastStatus.equals("Banjir")) {
                sendSystemNotification("SYRAM: PERINGATAN BANJIR!", saran, "BANJIR");
            }
        }

        lastStatus = status;

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
        if (pompaRef != null && pompaListener != null) pompaRef.removeEventListener(pompaListener);
        if (kipasRef != null && kipasListener != null) kipasRef.removeEventListener(kipasListener);
    }
}
