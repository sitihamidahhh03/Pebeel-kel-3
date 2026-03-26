package com.example.monika;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Fitur Baru: Helper khusus untuk menghapus/membatalkan alarm.
 * Dibuat terpisah agar tidak merusak kode asli tim.
 */
public class AlarmManagerHelper {

    /**
     * Membatalkan alarm di sistem Android berdasarkan waktu sebagai ID unik.
     * 
     * @param context Context aplikasi
     * @param time String waktu (misal "08:00") yang digunakan sebagai basis ID unik
     */
    public static void hapusAlarm(Context context, String time) {
        if (time == null || time.isEmpty()) return;

        // Gunakan hashcode waktu sebagai ID (harus konsisten dengan cara tim membuat alarm)
        int alarmId = time.hashCode();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        // Intent harus sama dengan yang didaftarkan di manifest dan oleh tim
        Intent intent = new Intent(context, AlarmReceiver.class);

        // Buat PendingIntent yang identik untuk dibatalkan
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            // Batalkan alarm di sistem Android
            alarmManager.cancel(pendingIntent);
            // Batalkan PendingIntent-nya juga
            pendingIntent.cancel();
            
            Toast.makeText(context, "Fitur Hapus: Alarm " + time + " telah dimatikan.", Toast.LENGTH_SHORT).show();
        }
    }
}