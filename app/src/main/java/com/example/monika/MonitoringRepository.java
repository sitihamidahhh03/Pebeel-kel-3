package com.example.monika;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MonitoringRepository {
    private static final String PREF_NAME = "MoNiKa_Monitoring_Data";
    private static final String KEY_WEEKLY = "weekly_data";
    private static final String KEY_MONTHLY = "monthly_data";
    private static final String KEY_YEARLY = "yearly_data";

    // Simpan nilai terbaru untuk hari ini
    public static void updateTodayValue(Context context, int value) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        
        // Update Weekly Data (0-6: Sen-Min)
        float[] weekly = getWeeklyData(context);
        weekly[getTodayIndex()] = value;
        saveWeeklyData(context, weekly);

        // Update Monthly Data (0-11: Jan-Des)
        float[] monthly = getMonthlyData(context);
        monthly[getCurrentMonthIndex()] = value;
        saveMonthlyData(context, monthly);

        // Update Yearly Data (Kita simpan dalam Map Tahun -> Nilai)
        saveYearlyData(context, String.valueOf(getCurrentYear()), value);
    }

    public static float[] getWeeklyData(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = pref.getString(KEY_WEEKLY, null);
        if (json == null) return new float[]{0, 0, 0, 0, 0, 0, 0};
        return new Gson().fromJson(json, float[].class);
    }

    private static void saveWeeklyData(Context context, float[] data) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_WEEKLY, new Gson().toJson(data)).apply();
    }

    public static float[] getMonthlyData(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = pref.getString(KEY_MONTHLY, null);
        if (json == null) return new float[12];
        return new Gson().fromJson(json, float[].class);
    }

    private static void saveMonthlyData(Context context, float[] data) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_MONTHLY, new Gson().toJson(data)).apply();
    }

    public static float[] getYearlyData(Context context, String[] years) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = pref.getString(KEY_YEARLY, null);
        Map<String, Float> dataMap;
        if (json == null) dataMap = new HashMap<>();
        else dataMap = new Gson().fromJson(json, new TypeToken<Map<String, Float>>(){}.getType());

        float[] result = new float[years.length];
        for (int i = 0; i < years.length; i++) {
            result[i] = dataMap.getOrDefault(years[i], 0f);
        }
        return result;
    }

    private static void saveYearlyData(Context context, String year, int value) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = pref.getString(KEY_YEARLY, null);
        Map<String, Float> dataMap;
        if (json == null) dataMap = new HashMap<>();
        else dataMap = new Gson().fromJson(json, new TypeToken<Map<String, Float>>(){}.getType());

        dataMap.put(year, (float) value);
        pref.edit().putString(KEY_YEARLY, new Gson().toJson(dataMap)).apply();
    }

    private static int getTodayIndex() {
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_WEEK);
        // Calendar.MONDAY = 2, ..., SUNDAY = 1
        int[] mapping = {6, 0, 1, 2, 3, 4, 5}; // Indeks 0 (Minggu) jadi 6, Indeks 1 (Senin) jadi 0
        return mapping[day - 1];
    }

    private static int getCurrentMonthIndex() {
        return Calendar.getInstance().get(Calendar.MONTH);
    }

    private static int getCurrentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }
}
