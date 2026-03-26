package com.example.monika.konten_dashboard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.widget.Toast;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import com.example.monika.R;

public class WateringManager {

    private Context context;
    private SwitchCompat switchSiram;

    public WateringManager(Context context, SwitchCompat switchSiram) {
        this.context = context;
        this.switchSiram = switchSiram;
        initSwitch();
    }

    private void initSwitch() {
        if (switchSiram == null) return;

        // Set kondisi awal (warna mati)
        updateSwitchUI(false);

        switchSiram.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateSwitchUI(isChecked);

            if (isChecked) {
                Toast.makeText(context, "Pompa Air NYALA 💧", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Pompa Air MATI 🛑", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSwitchUI(boolean isActive) {
        // Warna saat Nyala: Biru Air (#448AFF)
        // Warna saat Mati: Abu-abu/Hijau Pudar (#BDBDBD)
        int trackColor = isActive ? Color.parseColor("#448AFF") : Color.parseColor("#BDBDBD");
        int thumbColor = Color.WHITE;

        switchSiram.setTrackTintList(ColorStateList.valueOf(trackColor));
        switchSiram.setThumbTintList(ColorStateList.valueOf(thumbColor));
    }
}