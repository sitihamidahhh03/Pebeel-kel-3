package com.example.monika;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AktivitasSaran extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tampilan_saran);

        Button btnAction1 = findViewById(R.id.btnAction1);
        Button btnAction2 = findViewById(R.id.btnAction2);
        ImageView ivSettings = findViewById(R.id.ivSettings);

        if (btnAction1 != null) {
            btnAction1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(AktivitasSaran.this, "Memulai penyiraman tanaman...", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnAction2 != null) {
            btnAction2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(AktivitasSaran.this, "Penyiraman ditunda.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (ivSettings != null) {
            ivSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Ambil email dari SharedPreferences
                    SharedPreferences sharedPref = getSharedPreferences("SyamPref", Context.MODE_PRIVATE);
                    String email = sharedPref.getString("email", "user@email.com");

                    // Pindah ke halaman profil
                    Intent intent = new Intent(AktivitasSaran.this, AktivitasProfil.class);
                    intent.putExtra("EMAIL_USER", email);
                    startActivity(intent);
                }
            });
        }
    }
}
