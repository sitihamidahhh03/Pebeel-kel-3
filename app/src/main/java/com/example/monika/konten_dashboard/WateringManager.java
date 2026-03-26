package com.example.monika.konten_dashboard;

import android.app.Activity;
import android.widget.CompoundButton;
import android.widget.Toast;
import androidx.appcompat.widget.SwitchCompat;

public class WateringManager {

    private Activity activity;
    private SwitchCompat switchSiram;

    public WateringManager(Activity activity, SwitchCompat switchSiram) {
        this.activity = activity;
        this.switchSiram = switchSiram;
        setupSwitch();
    }

    private void setupSwitch() {
        if (switchSiram != null) {
            switchSiram.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        // Nyalakan penyiraman
                        startWatering();
                    } else {
                        // Matikan penyiraman
                        stopWatering();
                    }
                }
            });
        }
    }

    private void startWatering() {
        // TODO: Implementasi logika penyiraman
        Toast.makeText(activity, "Penyiraman dimulai", Toast.LENGTH_SHORT).show();
    }

    private void stopWatering() {
        // TODO: Implementasi logika berhenti penyiraman
        Toast.makeText(activity, "Penyiraman dihentikan", Toast.LENGTH_SHORT).show();
    }
}
