package com.example.monika;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddAlarm extends AppCompatActivity {

    private TextView tvTime;
    private EditText etLabel;
    private int selectedHour = 0;
    private int selectedMinute = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        // 1. Inisialisasi Views
        TextView tvTitle = findViewById(R.id.tv_title_page);
        tvTime = findViewById(R.id.tv_time);
        etLabel = findViewById(R.id.et_label);
        Button btnSave = findViewById(R.id.btn_save);
        Button btnCancel = findViewById(R.id.btn_cancel);
        Button btnBack = findViewById(R.id.btn_back);

        // 2. Set waktu default
        Calendar calendar = Calendar.getInstance();
        selectedHour = calendar.get(Calendar.HOUR_OF_DAY);
        selectedMinute = calendar.get(Calendar.MINUTE);
        updateTimeDisplay();

        // 3. Logika untuk Edit (Jika dipanggil dari ReadAlarm)
        Intent intent = getIntent();
        if (intent.hasExtra("index")) {
            if (tvTitle != null) tvTitle.setText(R.string.title_edit_alarm);
            if (btnSave != null) btnSave.setText(R.string.btn_update_alarm);

            String time = intent.getStringExtra("time");
            String label = intent.getStringExtra("label");
            if (time != null) {
                tvTime.setText(time);
                try {
                    String[] parts = time.split(":");
                    selectedHour = Integer.parseInt(parts[0]);
                    selectedMinute = Integer.parseInt(parts[1]);
                } catch (Exception ignored) {}
            }
            if (etLabel != null) etLabel.setText(label);
        }

        // 4. Pasang Listener
        tvTime.setOnClickListener(v -> showTimePicker());
        if (btnSave != null) btnSave.setOnClickListener(v -> saveAlarm());
        if (btnCancel != null) btnCancel.setOnClickListener(v -> finish());
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    private void showTimePicker() {
        // Menggunakan MaterialTimePicker untuk menghindari masalah tombol tidak terlihat
        // Komponen ini mengikuti tema Material aplikasi Anda secara otomatis
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(selectedHour)
                .setMinute(selectedMinute)
                .setTitleText("Pilih Waktu Alarm")
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            selectedHour = picker.getHour();
            selectedMinute = picker.getMinute();
            updateTimeDisplay();
        });

        picker.show(getSupportFragmentManager(), "MATERIAL_TIME_PICKER");
    }

    private void updateTimeDisplay() {
        String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
        tvTime.setText(formattedTime);
    }

    private void saveAlarm() {
        String time = tvTime.getText().toString().trim();
        String label = etLabel.getText().toString().trim();

        if (label.isEmpty()) {
            etLabel.setError("Label harus diisi");
            return;
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return;
            }
        }

        setSystemAlarm(label);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("time", time);
        resultIntent.putExtra("label", label);
        if (getIntent().hasExtra("index")) {
            resultIntent.putExtra("index", getIntent().getIntExtra("index", -1));
        }
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private void setSystemAlarm(String label) {
        try {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("alarm_label", label);

            int requestCode = selectedHour * 100 + selectedMinute;

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
            calendar.set(Calendar.MINUTE, selectedMinute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            if (alarmManager != null) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        } catch (Exception ignored) {}
    }
}
