package com.example.monika;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.monika.R;

public class AddAlarm extends AppCompatActivity {

    private EditText etTime;
    private EditText etLabel;
    private Button btnSave;
    private Button btnCancel;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        // Inisialisasi view
        etTime = findViewById(R.id.et_time);
        etLabel = findViewById(R.id.et_label);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        btnBack = findViewById(R.id.btn_back);

        // Set listeners
        btnSave.setOnClickListener(v -> saveAlarm());
        btnCancel.setOnClickListener(v -> finish());
        btnBack.setOnClickListener(v -> finish());
    }

    private void saveAlarm() {
        String time = etTime.getText().toString().trim();
        String label = etLabel.getText().toString().trim();

        // Validasi input
        if (time.isEmpty()) {
            etTime.setError("Waktu harus diisi");
            etTime.requestFocus();
            return;
        }

        // Validasi format waktu
        if (!isValidTimeFormat(time)) {
            etTime.setError("Format waktu salah. Gunakan HH:MM atau HH.MM (contoh: 15:00)");
            etTime.requestFocus();
            return;
        }

        // Format waktu menjadi standar
        String formattedTime = formatTime(time);

        // Kirim data kembali ke activity sebelumnya
        Intent resultIntent = new Intent();
        resultIntent.putExtra("time", formattedTime);
        resultIntent.putExtra("label", label);
        setResult(Activity.RESULT_OK, resultIntent);

        // Tampilkan toast konfirmasi
        Toast.makeText(this, "Alarm ditambahkan: " + formattedTime, Toast.LENGTH_SHORT).show();

        // Tutup activity
        finish();
    }

    private boolean isValidTimeFormat(String time) {
        // Cek format HH:MM
        if (time.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            return true;
        }
        // Cek format HH.MM
        if (time.matches("^([01]?[0-9]|2[0-3])\\.[0-5][0-9]$")) {
            return true;
        }
        return false;
    }

    private String formatTime(String time) {
        // Ubah . menjadi :
        return time.replace(".", ":");
    }
}