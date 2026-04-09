// AlarmReceiver.java
package com.example.monika;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "syram_notifications"; // Gunakan channel yang sama dengan monitoring
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {
        String alarmLabel = intent.getStringExtra("alarm_label");
        if (alarmLabel == null) {
            alarmLabel = "Pengingat Tanaman";
        }

        String message = "Waktunya " + alarmLabel + " - pastikan kelembaban tanah cukup!";

        // 1. Simpan ke riwayat notifikasi aplikasi (Instagram style)
        NotificationRepository.addNotification(context, "Alarm Pengingat", message, "ALARM");

        // 2. Buat Notification Channel (untuk Android Oreo ke atas)
        createNotificationChannel(context);

        // 3. Munculkan notifikasi di layar HP
        showNotification(context, alarmLabel, message);
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "SYRAM Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void showNotification(Context context, String alarmLabel, String message) {
        // Intent untuk masuk ke halaman Notifikasi saat notif di-klik
        Intent notificationIntent = new Intent(context, NotificationActivity.class);
        notificationIntent.putExtra("OPEN_STATUS", "ALARM");
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 
                0, 
                notificationIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Alarm Pengingat")
                .setContentText(message)
                // --- TAMBAHKAN PANAH (BigTextStyle) agar teks bisa dibaca lengkap ---
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                // -------------------------------------------------------------------
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent) // Menghubungkan ke halaman notifikasi
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }
}
