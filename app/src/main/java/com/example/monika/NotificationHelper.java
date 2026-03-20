package com.example.monika; // PENTING: Ganti dengan nama package aslimu

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID = "notifikasi_kelembaban";
    private static final String CHANNEL_NAME = "Peringatan Kelembaban";

    // Menambahkan Anotasi ini untuk 'membungkam' peringatan merah dari Android Studio
    @SuppressLint("MissingPermission")
    public static void tampilkanNotifikasi(Context context, String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Menghapus pengecekan versi karena proyekmu minimal API 26 (menghilangkan error SDK_INT)
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );
        notificationManager.createNotificationChannel(channel);

        // Membangun tampilan notifikasi
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.sym_def_app_icon) // Logo Android
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Mengecek izin untuk Android 13+ (Menghilangkan error POST_NOTIFICATIONS)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            // Memunculkan notifikasi ke layar
            notificationManager.notify(1, builder.build());
        }
    }
}