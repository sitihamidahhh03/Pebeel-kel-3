package com.example.monika.konten_dashboard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.widget.Toast;
import androidx.appcompat.widget.SwitchCompat;
import com.example.monika.R;

public class WateringManager {

    private Context context;
    private SwitchCompat switchSiram;
    private SwitchCompat switchOtomatis;
    private boolean isAutoActive = false;

    public WateringManager(Context context, SwitchCompat switchSiram, SwitchCompat switchOtomatis) {
        this.context = context;
        this.switchSiram = switchSiram;
        this.switchOtomatis = switchOtomatis;
        initSwitch();
    }

    private void initSwitch() {
        if (switchSiram != null) {
            updateSwitchUI(switchSiram, false, "#8BAE66");
            switchSiram.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateSwitchUI(switchSiram, isChecked, "#8BAE66");
                if (isChecked) {
                    Toast.makeText(context, "Manual: Pompa Air NYALA 💧", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Manual: Pompa Air MATI 🛑", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (switchOtomatis != null) {
            updateSwitchUI(switchOtomatis, false, "#448AFF");
            switchOtomatis.setOnCheckedChangeListener((buttonView, isChecked) -> {
                isAutoActive = isChecked;
                updateSwitchUI(switchOtomatis, isChecked, "#448AFF");
                if (isChecked) {
                    Toast.makeText(context, "Mode OTOMATIS Aktif 🤖", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Mode OTOMATIS Mati ❌", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateSwitchUI(SwitchCompat sw, boolean isActive, String activeColor) {
        int trackColor = isActive ? Color.parseColor(activeColor) : Color.parseColor("#BDBDBD");
        sw.setTrackTintList(ColorStateList.valueOf(trackColor));
        sw.setThumbTintList(ColorStateList.valueOf(Color.WHITE));
    }

    // Fungsi yang dipanggil oleh MonitoringManager untuk kontrol otomatis
    public void checkAutoWatering(int soilValue) {
        if (!isAutoActive || switchSiram == null) return;

        // Logika: Jika kelembaban di bawah 60% (Kering), nyalakan pompa
        if (soilValue < 60) {
            if (!switchSiram.isChecked()) {
                switchSiram.setChecked(true);
                Toast.makeText(context, "Otomatis: Tanah kering, menyiram... 💧", Toast.LENGTH_SHORT).show();
            }
        } 
        // Logika Penyesuaian: Jika kelembaban sudah mencapai status Optimal (mulai dari 60%), matikan pompa
        else if (soilValue >= 60) {
            if (switchSiram.isChecked()) {
                switchSiram.setChecked(false);
                Toast.makeText(context, "Otomatis: Tanah sudah optimal, berhenti menyiram 🛑", Toast.LENGTH_SHORT).show();
            }
        }
    }
}