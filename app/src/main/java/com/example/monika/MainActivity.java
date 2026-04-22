package com.example.monika;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // ==========================================
        // Ini adalah simulasi angka kelembaban dari sensor
        int kelembabanTanah = 25; // Data persentase dari sensor (contoh)

        if (kelembabanTanah < 30) {
            // Memanggil mesin notifikasi yang tadi kamu buat
            NotificationHelper.tampilkanNotifikasi(
                    this,
                    "Peringatan! Tanah Kering",
                    "Kelembaban saat ini " + kelembabanTanah + "%. Segera lakukan penyiraman hortikultura Anda."
            );
        } else if (kelembabanTanah > 80) {
            NotificationHelper.tampilkanNotifikasi(
                    this,
                    "Peringatan! Tanah Terlalu Lembab",
                    "Kelembaban saat ini " + kelembabanTanah + "%. Kurangi volume penyiraman."
            );
        }
        // ==========================================

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}