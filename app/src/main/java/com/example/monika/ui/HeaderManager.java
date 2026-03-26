package com.example.monika.ui;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.monika.R; // Sesuaikan dengan package

public class HeaderManager {

    private Activity activity;
    private ImageView ivProfile;
    private TextView tvLogoApp;

    // Constructor: Menghubungkan Manager ini dengan Activity
    public HeaderManager(Activity activity) {
        this.activity = activity;
        initHeader();
    }

    private void initHeader() {
        // 1. Menghubungkan variabel dengan ID yang ada di layout_header.xml
        ivProfile = activity.findViewById(R.id.ivProfile);

        // 2. Fungsi Klik pada Icon Profil
        if (ivProfile != null) {
            ivProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Menampilkan pesan sementara
                    Toast.makeText(activity, "Membuka Profil Pengguna...", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // 3. Fungsi Klik pada Logo/Nama Aplikasi (Opsional)
        if (tvLogoApp != null) {
            tvLogoApp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Klik logo untuk kembali ke paling atas atau refresh
                    Toast.makeText(activity, "Dashboard Pertanian Cabai", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Fungsi tambahan jika ingin mengubah judul secara dinamis dari Java
    public void setHeaderTitle(String title) {
        if (tvLogoApp != null) {
            tvLogoApp.setText(title);
        }
    }
}
