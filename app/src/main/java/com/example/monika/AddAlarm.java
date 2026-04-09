package com.example.monika;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import java.util.Locale;

public class AddAlarm extends AppCompatActivity {

    private TextView tvTime;
    private EditText etLabel;
    private TextView tvTitle;
    private Button btnSave;
    private Button btnCancel;
    private Button btnBack;

    private int selectedHour = 0;
    private int selectedMinute = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        tvTitle = findViewById(R.id.tv_title_page);
        tvTime = findViewById(R.id.tv_time);
        etLabel = findViewById(R.id.et_label);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        btnBack = findViewById(R.id.btn_back);

        Calendar calendar = Calendar.getInstance();
        selectedHour = calendar.get(Calendar.HOUR_OF_DAY);
        selectedMinute = calendar.get(Calendar.MINUTE);
        updateTimeDisplay();

        Intent intent = getIntent();
        if (intent.hasExtra("index")) {
            if (tvTitle != null) tvTitle.setText("Edit Alarm");
            btnSave.setText("Perbarui Alarm");

            String time = intent.getStringExtra("time");
            String label = intent.getStringExtra("label");

            if (time != null) {
                tvTime.setText(time);
                try {
                    String[] parts = time.split(":");
                    selectedHour = Integer.parseInt(parts[0]);
                    selectedMinute = Integer.parseInt(parts[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            etLabel.setText(label);
        } else {
            if (tvTitle != null) tvTitle.setText("Tambah Alarm");
            btnSave.setText("Simpan Alarm");
        }

        tvTime.setOnClickListener(v -> showTimePicker());
        btnSave.setOnClickListener(v -> saveAlarm());
        btnCancel.setOnClickListener(v -> finish());
        btnBack.setOnClickListener(v -> finish());
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedHour = hourOfDay;
                    selectedMinute = minute;
                    updateTimeDisplay();
                },
                selectedHour,
                selectedMinute,
                true
        );
        timePickerDialog.show();
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
            etLabel.requestFocus();
            return;
        }

        // --- SETUP ALARM KE SISTEM ANDROID ---
        setSystemAlarm(time, label);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("time", time);
        resultIntent.putExtra("label", label);

        if (getIntent().hasExtra("index")) {
            resultIntent.putExtra("index", getIntent().getIntExtra("index", -1));
        }

        setResult(Activity.RESULT_OK, resultIntent);
        Toast.makeText(this, "Alarm disetel: " + time, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void setSystemAlarm(String time, String label) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("alarm_label", label);
        
        // ID unik berdasarkan waktu untuk identifikasi saat hapus
        int alarmId = time.hashCode();
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 
                alarmId, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
        calendar.set(Calendar.MINUTE, selectedMinute);
        calendar.set(Calendar.SECOND, 0);

        // Jika waktu sudah lewat hari ini, set untuk besok
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        if (alarmManager != null) {
            // Set Alarm Tepat Waktu
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }
    }
}
