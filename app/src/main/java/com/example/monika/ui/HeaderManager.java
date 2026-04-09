package com.example.monika.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.monika.AktivitasProfil;
import com.example.monika.DatabaseHelper;
import com.example.monika.R;

public class HeaderManager {

    private Activity activity;
    private ImageView ivProfile;
    private View cvProfile; // Tambahkan referensi untuk CardView profil
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
        cvProfile = activity.findViewById(R.id.cvProfile); // Hubungkan ke CardView
        tvLogoApp = activity.findViewById(R.id.tvLogoApp);
        btnKembali = activity.findViewById(R.id.btnKembali);

        loadProfilePhoto();

        if (ivProfile != null) {
            ivProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences pref = activity.getSharedPreferences("SyamPref", Context.MODE_PRIVATE);
                    String emailTersimpan = pref.getString("email", "");

                    Intent intent = new Intent(activity, AktivitasProfil.class);
                    intent.putExtra("EMAIL_USER", emailTersimpan);
                    activity.startActivity(intent);
                }
            });
        }
    }

    public void loadProfilePhoto() {
        if (ivProfile != null) {
            SharedPreferences pref = activity.getSharedPreferences("SyamPref", Context.MODE_PRIVATE);
            String email = pref.getString("email", "");
            
            if (!email.isEmpty()) {
                String photoPath = dbHelper.getUserPhoto(email);
                if (photoPath != null && !photoPath.isEmpty()) {
                    ivProfile.setImageURI(Uri.parse(photoPath));
                    ivProfile.setImageTintList(null);
                } else {
                    ivProfile.setImageResource(R.drawable.ic_account_circle);
                }
            }
        }
    }

    public void showBackButton(boolean show) {
        if (btnKembali != null) {
            btnKembali.setVisibility(show ? View.VISIBLE : View.GONE);
            btnKembali.setOnClickListener(v -> activity.finish());
        }
    }

    public void showProfileIcon(boolean show) {
        // Sembunyikan CardView-nya agar bulatan putih tidak tersisa
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
