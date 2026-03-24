// AlarmReceiver.java
package com.example.monika;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "plant_care_channel";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {
        String alarmLabel = intent.getStringExtra("alarm_label");
        int alarmId = intent.getIntExtra("alarm_id", 0);

        if (alarmLabel == null) {
            alarmLabel = "Pengingat Tanaman";
        }

        createNotificationChannel(context);
        showNotification(context, alarmLabel);
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Pengingat Tanaman",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifikasi untuk mengingatkan menyiram tanaman");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void showNotification(Context context, String alarmLabel) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_edit)
                .setContentTitle("Pengingat Tanaman")
                .setContentText("Waktunya " + alarmLabel + " - pastikan kelembaban tanah cukup!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }
}