package com.example.monika;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.monika.AlarmAdapter;
import com.example.monika.konten_dashboard.ClockManager;
import com.example.monika.konten_dashboard.MonitoringManager;
import com.example.monika.konten_dashboard.WateringManager;
import com.example.monika.AddAlarm;
import com.example.monika.AlarmModel;
import com.example.monika.ui.FooterManager;
import com.example.monika.ui.HeaderManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private ClockManager clockManager;
    private MonitoringManager monitoringManager;
    private AlarmAdapter alarmAdapter;
    private List<AlarmModel> alarmList;

    private static final int REQUEST_ADD_ALARM = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 1. Header & Footer
        new HeaderManager(this);
        FooterManager footer = new FooterManager(this);
        footer.setActiveMenu(R.id.indicator_home);

        // 2. Jam & Tanggal
        TextView tvTime = findViewById(R.id.tvTime);
        TextView tvDate = findViewById(R.id.tvDate);
        clockManager = new ClockManager(tvTime, tvDate);

        // 3. Monitoring (10 Detik)
        monitoringManager = new MonitoringManager(this);

        // 4. Switch Siram
        SwitchCompat switchSiram = findViewById(R.id.switchSiram);
        if (switchSiram != null) {
            new WateringManager(this, switchSiram);
        }

        // 5. Setup Alarm List
        setupAlarmList();

        // 6. Setup Tombol Tambah Alarm
        setupAddAlarmButton();
    }

    private void setupAlarmList() {
        // Inisialisasi list alarm
        alarmList = new ArrayList<>();

        // Setup RecyclerView
        RecyclerView rvAlarm = findViewById(R.id.rv_alarm_list);
        if (rvAlarm != null) {
            rvAlarm.setLayoutManager(new LinearLayoutManager(this));

            alarmAdapter = new AlarmAdapter(this, alarmList, (alarm, isChecked) -> {
                // Handle toggle alarm aktif/nonaktif
                if (isChecked) {
                    Toast.makeText(this, "Alarm " + alarm.getTime() + " diaktifkan", Toast.LENGTH_SHORT).show();
                    // TODO: Jadwalkan alarm
                } else {
                    Toast.makeText(this, "Alarm " + alarm.getTime() + " dinonaktifkan", Toast.LENGTH_SHORT).show();
                    // TODO: Batalkan alarm
                }
            });

            rvAlarm.setAdapter(alarmAdapter);
        }

        // Load data alarm yang sudah tersimpan (jika ada)
        loadSavedAlarms();
    }

    private void setupAddAlarmButton() {
        FloatingActionButton fabAddAlarm = findViewById(R.id.fab_add_alarm);
        if (fabAddAlarm != null) {
            fabAddAlarm.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, AddAlarm.class);
                startActivityForResult(intent, REQUEST_ADD_ALARM);
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ADD_ALARM && resultCode == RESULT_OK && data != null) {
            String time = data.getStringExtra("time");
            String label = data.getStringExtra("label");

            // Buat alarm baru
            AlarmModel newAlarm = new AlarmModel(time, label);

            // Tambahkan ke list
            alarmList.add(newAlarm);
            alarmAdapter.notifyItemInserted(alarmList.size() - 1);

            // Scroll ke posisi terakhir
            RecyclerView rvAlarm = findViewById(R.id.rv_alarm_list);
            if (rvAlarm != null) {
                rvAlarm.scrollToPosition(alarmList.size() - 1);
            }

            // Simpan ke penyimpanan
            saveAlarmToStorage(newAlarm);

            // Tampilkan konfirmasi
            String message = label.isEmpty() ?
                    "Alarm " + time + " ditambahkan" :
                    "Alarm " + time + " (" + label + ") ditambahkan";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSavedAlarms() {
        // TODO: Load alarm dari SharedPreferences atau Database
        // Contoh data untuk testing (bisa dihapus nanti)
        // alarmList.add(new AlarmModel("07:00", "Bangun pagi"));
        // alarmList.add(new AlarmModel("12:00", "Sholat Dzuhur"));
        // alarmList.add(new AlarmModel("17:00", "Siram tanaman"));
    }

    private void saveAlarmToStorage(AlarmModel alarm) {
        // TODO: Simpan ke SharedPreferences atau Database
        // Sementara hanya print log
        android.util.Log.d("DashboardActivity", "Alarm saved: " + alarm.getTime() + " - " + alarm.getLabel());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clockManager != null) {
            clockManager.stopClock();
        }
        if (monitoringManager != null) {
            monitoringManager.stopMonitoring();
        }
    }
}