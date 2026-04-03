package com.example.monika;

import android.app.Activity;
import android.app.TimePickerDialog;
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

        // 🔥 DEFAULT WAKTU SEKARANG
        Calendar calendar = Calendar.getInstance();
        selectedHour = calendar.get(Calendar.HOUR_OF_DAY);
        selectedMinute = calendar.get(Calendar.MINUTE);
        updateTimeDisplay();

        // 🔥 CEK MODE EDIT
        Intent intent = getIntent();
        if (intent.hasExtra("index")) { // Gunakan 'index' sebagai penanda mode EDIT
            // MODE EDIT
            if (tvTitle != null) tvTitle.setText("Edit Alarm");
            btnSave.setText("Perbarui Alarm");

            // Ambil data yang dikirim dari ReadAlarm
            String time = intent.getStringExtra("time");
            String label = intent.getStringExtra("label");

            if (time != null) {
                tvTime.setText(time);
                // Parsing waktu ke jam & menit agar TimePicker mulai dari jam tersebut
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
            // MODE TAMBAH
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

        Intent resultIntent = new Intent();
        resultIntent.putExtra("time", time);
        resultIntent.putExtra("label", label);

        // 🔥 KIRIM INDEX JIKA EDIT
        if (getIntent().hasExtra("index")) {
            resultIntent.putExtra("index",
                    getIntent().getIntExtra("index", -1));
        }

        setResult(Activity.RESULT_OK, resultIntent);

        Toast.makeText(this, "Alarm disimpan: " + time, Toast.LENGTH_SHORT).show();

        finish();
    }
}
