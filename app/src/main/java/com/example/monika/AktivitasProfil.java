package com.example.monika;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.monika.ui.HeaderManager;

public class AktivitasProfil extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tampilan_profil);

        HeaderManager header = new HeaderManager(this);
        header.setHeaderTitle("Profil");
        header.showBackButton(true);
        header.showProfileIcon(false);
        ImageView btnKembali = findViewById(R.id.btnKembali);
        TextView tvEmailUser = findViewById(R.id.tvEmailUser);
        Button btnLogout = findViewById(R.id.btnLogout);

        // Ambil email dari intent (dikirim dari Login atau Saran)
        String email = getIntent().getStringExtra("EMAIL_USER");
        if (email != null) {
            tvEmailUser.setText(email);
        }

        if (btnKembali != null) {
            btnKembali.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 1. Pindah ke Dashboard secara eksplisit
                    Intent intent = new Intent(AktivitasProfil.this, DashboardActivity.class);

                    // 2. Flag agar tidak menumpuk halaman di RAM
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    // 3. Pindah halaman
                    startActivity(intent);
                    finish();
                }
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Logika Logout: Kembali ke halaman Login dan bersihkan tumpukan aktivitas
                    Intent intent = new Intent(AktivitasProfil.this, AktivitasLogin.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }
}
