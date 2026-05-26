package com.example.monika.konten_dashboard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.widget.SwitchCompat;
import com.example.monika.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class WateringManager {

    private Context context;
    private SwitchCompat switchSiram;
    private SwitchCompat switchOtomatis;
    private boolean isAutoActive = false;
    private DatabaseReference pompaRef;

    public WateringManager(Context context, SwitchCompat switchSiram, SwitchCompat switchOtomatis) {
        this.context = context;
        this.switchSiram = switchSiram;
        this.switchOtomatis = switchOtomatis;
        
        // DISESUAIKAN: Path lowercase sesuai screenshot Firebase
        String dbUrl = "https://syram-iot-default-rtdb.asia-southeast1.firebasedatabase.app/";
        pompaRef = FirebaseDatabase.getInstance(dbUrl).getReference("Sensor/Pompa");
        
        initSwitch();
    }

    private void initSwitch() {
        if (switchSiram != null) {
            updateSwitchUI(switchSiram, false, "#8BAE66");
            switchSiram.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateSwitchUI(switchSiram, isChecked, "#8BAE66");
                
                String status = isChecked ? "ON" : "OFF";
                pompaRef.setValue(status).addOnSuccessListener(aVoid -> {
                    Log.d("FIREBASE_WRITE", "Berhasil set pompa ke: " + status);
                }).addOnFailureListener(e -> {
                    Log.e("FIREBASE_WRITE", "Gagal update pompa: " + e.getMessage());
                });

                if (isChecked) {
                    Toast.makeText(context, "Penyiraman NYALA 💧", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Penyiraman MATI 🛑", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (switchOtomatis != null) {
            updateSwitchUI(switchOtomatis, false, "#448AFF");
            switchOtomatis.setOnCheckedChangeListener((buttonView, isChecked) -> {
                isAutoActive = isChecked;
                updateSwitchUI(switchOtomatis, isChecked, "#448AFF");
                
                // Update mode di sensor/mode jika diperlukan
                pompaRef.getParent().child("mode").setValue(isChecked ? "Otomatis" : "Manual");
            });
        }
    }

    private void updateSwitchUI(SwitchCompat sw, boolean isActive, String activeColor) {
        int trackColor = isActive ? Color.parseColor(activeColor) : Color.parseColor("#BDBDBD");
        sw.setTrackTintList(ColorStateList.valueOf(trackColor));
        sw.setThumbTintList(ColorStateList.valueOf(Color.WHITE));
    }

    public void checkAutoWatering(int soilValue) {
        if (!isAutoActive || switchSiram == null) return;

        if (soilValue < 60) {
            if (!switchSiram.isChecked()) switchSiram.setChecked(true);
        } else if (soilValue >= 80) {
            if (switchSiram.isChecked()) switchSiram.setChecked(false);
        }
    }
}
