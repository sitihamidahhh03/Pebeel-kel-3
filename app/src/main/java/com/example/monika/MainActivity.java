package com.example.monika;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        // KITA PAKSA ALAMATNYA DI SINI (Jangan sampai ada typo ya)
        String url = "https://syram-iot-default-rtdb.asia-southeast1.firebasedatabase.app/";
        com.google.firebase.database.FirebaseDatabase database = com.google.firebase.database.FirebaseDatabase.getInstance(url);

        com.google.firebase.database.DatabaseReference myRef = database.getReference("tes_monitoring_cabai");

        // Kirim data sederhana
        myRef.setValue("Aplikasi Syram AKHIRNYA KONEK!");
    }
}