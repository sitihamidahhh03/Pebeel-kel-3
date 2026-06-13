package com.example.monika;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;

public class AlarmRepository {
    private static final String PREF_NAME = "MoNiKa_Alarms";
    private static final String KEY_ALARM_LIST = "alarm_list";

    public static void saveAlarms(Context context, List<AlarmModel> alarms) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = new Gson().toJson(alarms);
        pref.edit().putString(KEY_ALARM_LIST, json).apply();
    }

    public static List<AlarmModel> getAlarms(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = pref.getString(KEY_ALARM_LIST, null);
        if (json == null) return new ArrayList<>();
        
        return new Gson().fromJson(json, new TypeToken<List<AlarmModel>>(){}.getType());
    }

    public static void addAlarm(Context context, AlarmModel alarm) {
        List<AlarmModel> list = getAlarms(context);
        list.add(alarm);
        saveAlarms(context, list);
    }

    public static void updateAlarm(Context context, int index, AlarmModel alarm) {
        List<AlarmModel> list = getAlarms(context);
        if (index >= 0 && index < list.size()) {
            list.set(index, alarm);
            saveAlarms(context, list);
        }
    }

    public static void removeAlarm(Context context, int index) {
        List<AlarmModel> list = getAlarms(context);
        if (index >= 0 && index < list.size()) {
            list.remove(index);
            saveAlarms(context, list);
        }
    }
}
