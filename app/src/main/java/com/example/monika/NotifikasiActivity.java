package com.example.monika;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class NotifikasiActivity extends AppCompatActivity {

    private Button btnSiramTanaman;
    private Button btnTundaPenyiraman;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifikasi);

        btnSiramTanaman = findViewById(R.id.btn_siram_tanaman);
        btnTundaPenyiraman = findViewById(R.id.btn_tunda_penyiraman);

        btnSiramTanaman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(NotifikasiActivity.this, "Perintah menyiram tanaman dikirim!", Toast.LENGTH_SHORT).show();
                NotificationHelper.tampilkanNotifikasi(NotifikasiActivity.this, "Proses Penyiraman", "Pompa air sedang diaktifkan...");
            }
        });

        btnTundaPenyiraman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(NotifikasiActivity.this, "Penyiraman ditunda. Pompa dimatikan.", Toast.LENGTH_SHORT).show();
            }
        });

        // Simulasi Notifikasi Otomatis
        int simulasiKelembabanTanah = 25;

        if (simulasiKelembabanTanah < 30) {
            NotificationHelper.tampilkanNotifikasi(this, "Kelembaban Rendah", "Tanah kering (" + simulasiKelembabanTanah + "%). Segera siram.");
        } else if (simulasiKelembabanTanah > 80) {
            NotificationHelper.tampilkanNotifikasi(this, "Kelembaban Tinggi", "Tanah sangat basah (" + simulasiKelembabanTanah + "%). Tunda penyiraman.");
        }
    }
}