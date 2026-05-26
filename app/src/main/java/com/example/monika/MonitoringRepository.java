package com.example.monika;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonitoringRepository {
    private static final String PREF_NAME = "MoNiKa_Monitoring_Data";
    private static final String KEY_WEEKLY = "weekly_data";
    private static final String KEY_MONTHLY = "monthly_data";
    private static final String KEY_YEARLY = "yearly_data";
    private static final String KEY_DETAILS = "hourly_details"; 
    private static final String KEY_LAST_AGGREGATE = "last_aggregate_time";
    private static final String DB_URL = "https://syram-iot-default-rtdb.asia-southeast1.firebasedatabase.app/";

    public static void updateTodayValue(Context context, int value) {
        DatabaseReference db = FirebaseDatabase.getInstance(DB_URL).getReference();
        db.child("Sensor/RawData").push().setValue(value);

        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long lastAgg = pref.getLong(KEY_LAST_AGGREGATE, 0);
        long now = System.currentTimeMillis();

        if (lastAgg == 0) {
            pref.edit().putLong(KEY_LAST_AGGREGATE, now).apply();
            return;
        }

        if (now - lastAgg >= 3600000) {
            aggregateAndClean(context, db, now);
        }
    }

    private static void aggregateAndClean(Context context, DatabaseReference db, long now) {
        db.child("Sensor/RawData").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                long count = 0;
                double sum = 0;
                for (DataSnapshot child : snapshot.getChildren()) {
                    try {
                        Object val = child.getValue();
                        if (val instanceof Number) {
                            sum += ((Number) val).doubleValue();
                            count++;
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }

                if (count > 0) {
                    int averageValue = (int) (sum / count);
                    String hourLabel = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":00";
                    updateFirebaseGrafik(db, averageValue, hourLabel);
                    updateLocalCache(context, averageValue, hourLabel);
                    db.child("Sensor/RawData").removeValue();
                    context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                            .edit().putLong(KEY_LAST_AGGREGATE, now).apply();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private static void updateFirebaseGrafik(DatabaseReference db, int value, String hour) {
        int dayIndex = getTodayIndex();
        db.child("Grafik/Weekly").child(String.valueOf(dayIndex)).setValue(value);
        db.child("Grafik/Details").child(String.valueOf(dayIndex)).child(hour).setValue(value);
    }

    private static void updateLocalCache(Context context, int value, String hour) {
        float[] weekly = getWeeklyData(context);
        weekly[getTodayIndex()] = value;
        saveWeeklyData(context, weekly);

        Map<String, List<DetailEntry>> details = getHourlyDetails(context);
        String dayKey = String.valueOf(getTodayIndex());
        List<DetailEntry> dayDetails = details.getOrDefault(dayKey, new ArrayList<>());
        dayDetails.add(new DetailEntry(hour, value));
        details.put(dayKey, dayDetails);
        saveHourlyDetails(context, details);
    }

    public static void startSyncingWithFirebase(Context context) {
        DatabaseReference db = FirebaseDatabase.getInstance(DB_URL).getReference("Grafik");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                
                // 1. Sinkronisasi Grafik Mingguan
                DataSnapshot weeklySnap = snapshot.child("Weekly");
                float[] weekly = new float[7];
                for (int i = 0; i < 7; i++) {
                    if (weeklySnap.child(String.valueOf(i)).exists()) {
                        Object val = weeklySnap.child(String.valueOf(i)).getValue();
                        if (val instanceof Number) weekly[i] = ((Number) val).floatValue();
                    }
                }
                saveWeeklyData(context, weekly);

                // 2. Sinkronisasi Detail Per Jam (PERBAIKAN KRUSIAL)
                DataSnapshot detailsSnap = snapshot.child("Details");
                Map<String, List<DetailEntry>> allDetails = new HashMap<>();
                for (DataSnapshot daySnap : detailsSnap.getChildren()) {
                    String dayKey = daySnap.getKey();
                    List<DetailEntry> dayDetails = new ArrayList<>();
                    for (DataSnapshot hourSnap : daySnap.getChildren()) {
                        String hour = hourSnap.getKey();
                        Object val = hourSnap.getValue();
                        if (val instanceof Number) {
                            dayDetails.add(new DetailEntry(hour, ((Number) val).floatValue()));
                        }
                    }
                    allDetails.put(dayKey, dayDetails);
                }
                saveHourlyDetails(context, allDetails);
                Log.d("SYNC", "Data detail tersinkron: " + allDetails.size() + " hari");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public static Map<String, List<DetailEntry>> getHourlyDetails(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = pref.getString(KEY_DETAILS, null);
        if (json == null) return new HashMap<>();
        try {
            return new Gson().fromJson(json, new TypeToken<Map<String, List<DetailEntry>>>(){}.getType());
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private static void saveHourlyDetails(Context context, Map<String, List<DetailEntry>> details) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_DETAILS, new Gson().toJson(details)).apply();
    }

    public static float[] getWeeklyData(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = pref.getString(KEY_WEEKLY, null);
        if (json == null) return new float[7];
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
        // mapping: Sen=0, Sel=1, ..., Min=6
        int[] mapping = {6, 0, 1, 2, 3, 4, 5}; 
        return mapping[day - 1];
    }

    private static int getCurrentMonthIndex() {
        return Calendar.getInstance().get(Calendar.MONTH);
    }

    private static int getCurrentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }
}
