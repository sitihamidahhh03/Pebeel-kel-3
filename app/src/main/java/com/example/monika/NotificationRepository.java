package com.example.monika;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationRepository {
    private static final String PREF_NAME = "MoNiKa_Notifications";
    private static final String KEY_LIST = "notif_list";

    public static void addNotification(Context context, String title, String message, String status) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        
        List<NotificationModel> list = getNotifications(context);
        
        // Ambil waktu sekarang
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        
        // Tambah ke daftar (paling atas)
        list.add(0, new NotificationModel(title, message, currentTime, status));
        
        // Simpan maksimal 20 notifikasi saja agar tidak berat
        if (list.size() > 20) {
            list.remove(list.size() - 1);
        }
        
        pref.edit().putString(KEY_LIST, gson.toJson(list)).apply();
    }

    public static List<NotificationModel> getNotifications(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = pref.getString(KEY_LIST, null);
        if (json == null) return new ArrayList<>();
        
        return new Gson().fromJson(json, new TypeToken<List<NotificationModel>>(){}.getType());
    }
    
    public static void clearNotifications(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().remove(KEY_LIST).apply();
    }
}
