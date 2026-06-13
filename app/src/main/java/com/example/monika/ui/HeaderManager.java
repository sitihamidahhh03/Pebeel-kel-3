package com.example.monika.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.monika.AktivitasProfil;
import com.example.monika.DatabaseHelper;
import com.example.monika.R;

import java.io.File;

public class HeaderManager {

    private Activity activity;
    private ImageView ivProfile;
    private View cvProfile;
    private TextView tvLogoApp;
    private View btnKembali;
    private DatabaseHelper dbHelper;

    public HeaderManager(Activity activity) {
        this.activity = activity;
        this.dbHelper = new DatabaseHelper(activity);
        initHeader();
    }

    private void initHeader() {
        ivProfile = activity.findViewById(R.id.ivProfile);
        cvProfile = activity.findViewById(R.id.cvProfile);
        tvLogoApp = activity.findViewById(R.id.tvLogoApp);
        btnKembali = activity.findViewById(R.id.btnKembali);

        loadProfilePhoto();

        // Listener untuk akses profil (set pada ImageView dan CardView agar stabil)
        View.OnClickListener profileListener = v -> {
            SharedPreferences pref = activity.getSharedPreferences("SyamPref", Context.MODE_PRIVATE);
            String emailTersimpan = pref.getString("email", "");

            Intent intent = new Intent(activity, AktivitasProfil.class);
            intent.putExtra("EMAIL_USER", emailTersimpan);
            activity.startActivity(intent);
        };

        if (ivProfile != null) ivProfile.setOnClickListener(profileListener);
        if (cvProfile != null) cvProfile.setOnClickListener(profileListener);
    }

    public void loadProfilePhoto() {
        if (ivProfile == null) return;

        SharedPreferences pref = activity.getSharedPreferences("SyamPref", Context.MODE_PRIVATE);
        String email = pref.getString("email", "");
        
        if (!email.isEmpty()) {
            // 1. Coba ambil dari Database Lokal (SQLite) dulu agar cepat
            String photoPath = dbHelper.getUserPhoto(email);
            
            // 2. Jika di SQLite kosong, coba cari di SharedPreferences
            if (photoPath == null || photoPath.isEmpty()) {
                photoPath = pref.getString("profile_path_" + email, null);
            }

            if (photoPath != null && !photoPath.isEmpty()) {
                File imgFile = new File(photoPath);
                if (imgFile.exists()) {
                    try {
                        // Optimalkan pemuatan gambar agar tidak lambat/berat
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 2; // Perkecil resolusi untuk ikon kecil di header
                        Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
                        
                        if (bitmap != null) {
                            ivProfile.setImageBitmap(bitmap);
                            ivProfile.setImageTintList(null); // Hapus warna filter default
                            ivProfile.setScaleType(ImageView.ScaleType.CENTER_CROP); // Agar foto memenuhi lingkaran
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        // Fallback: Tampilkan ikon default jika gagal
        ivProfile.setImageResource(R.drawable.ic_account_circle);
    }

    public void showBackButton(boolean show) {
        if (btnKembali != null) {
            btnKembali.setVisibility(show ? View.VISIBLE : View.GONE);
            btnKembali.setOnClickListener(v -> activity.finish());
        }
    }

    public void showProfileIcon(boolean show) {
        if (cvProfile != null) {
            cvProfile.setVisibility(show ? View.VISIBLE : View.GONE);
        } else if (ivProfile != null) {
            ivProfile.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    public void setHeaderTitle(String title) {
        if (tvLogoApp != null) {
            tvLogoApp.setText(title);
        }
    }
}
