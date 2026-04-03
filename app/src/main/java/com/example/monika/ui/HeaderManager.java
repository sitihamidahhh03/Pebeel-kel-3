package com.example.monika.ui;

import android.app.Activity;
import android.content.Context; // Tambahan untuk SharedPreferences
import android.content.Intent;  // Tambahan untuk pindah halaman
import android.content.SharedPreferences; // Tambahan untuk ambil data email
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.monika.AktivitasProfil; // Import halaman tujuan
import com.example.monika.R; // Sesuaikan dengan package

public class HeaderManager {

    private Activity activity;
    private ImageView ivProfile;
    private TextView tvLogoApp;
    private ImageView ivBack;
    private View btnKembali;

    // Constructor: Menghubungkan Manager ini dengan Activity
    public HeaderManager(Activity activity) {
        this.activity = activity;
        initHeader();
    }

    private void initHeader() {
        // 1. Menghubungkan variabel dengan ID yang ada di layout_header.xml
        ivProfile = activity.findViewById(R.id.ivProfile);
        tvLogoApp = activity.findViewById(R.id.tvLogoApp);
        btnKembali = activity.findViewById(R.id.btnKembali);

        // 2. Fungsi Klik pada Icon Profil
        if (ivProfile != null) {
            ivProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Menampilkan pesan sementara
                    Toast.makeText(activity, "Membuka Profil Pengguna...", Toast.LENGTH_SHORT).show();

                    // --- BAGIAN PINDAH HALAMAN (TAMBAHAN) ---
                    // Ambil email yang tersimpan di memori (SyamPref)
                    SharedPreferences pref = activity.getSharedPreferences("SyamPref", Context.MODE_PRIVATE);
                    String emailTersimpan = pref.getString("email", "User@email.com");

                    // Perintah pindah ke AktivitasProfil
                    Intent intent = new Intent(activity, AktivitasProfil.class);
                    intent.putExtra("EMAIL_USER", emailTersimpan); // Kirim email agar tampil di profil
                    activity.startActivity(intent);
                    // ----------------------------------------
                }
            });
        }


    }

    public void showBackButton(boolean show) {
        if (btnKembali != null) {
            btnKembali.setVisibility(show ? View.VISIBLE : View.GONE);
            btnKembali.setOnClickListener(v -> activity.finish());
        }
    }

    public void showProfileIcon(boolean show) {if (ivProfile != null) {
        ivProfile.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    }

    // Fungsi tambahan jika ingin mengubah judul secara dinamis dari Java
    public void setHeaderTitle(String title) {
        if (tvLogoApp != null) {
            tvLogoApp.setText(title);
        }
    }
}