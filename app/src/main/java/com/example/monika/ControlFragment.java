package com.example.monika;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ControlFragment extends Fragment {

    private TextView tabManual, tabOtomatis;
    private TextView tvSiramLabel, tvKipasLabel;
    private SwitchMaterial switchSiram, switchKipas;
    private ImageView ivSiram, ivKipas;
    private boolean isManualMode = true;

    // Firebase
    private DatabaseReference pompaRef;
    private DatabaseReference kipasRef;
    private DatabaseReference modeRef;
    private final String dbUrl = "https://syram-iot-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_control, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inisialisasi Views
        tabManual = view.findViewById(R.id.tabManual);
        tabOtomatis = view.findViewById(R.id.tabOtomatis);
        tvSiramLabel = view.findViewById(R.id.tvSiramLabel);
        tvKipasLabel = view.findViewById(R.id.tvKipasLabel);
        switchSiram = view.findViewById(R.id.switchSiramManual);
        switchKipas = view.findViewById(R.id.switchKipasManual);
        ivSiram = view.findViewById(R.id.ivSiram);
        ivKipas = view.findViewById(R.id.ivKipas);

        // Inisialisasi Firebase - DIKEMBALIKAN KE STRUKTUR ASLI (Kapital)
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance(dbUrl);
            pompaRef = database.getReference("Kontrol/Pompa");
            kipasRef = database.getReference("Kontrol/Kipas");
            modeRef = database.getReference("Kontrol/Mode");
            Log.d("FIREBASE_CONTROL", "Firebase initialized successfully with original structure");
        } catch (Exception e) {
            Log.e("FIREBASE_CONTROL", "Error initializing Firebase: " + e.getMessage());
        }

        setupNavigation();
        setupSwitchListeners();

        // Default awal: Mode Manual
        setMode(true);
    }

    private void setupNavigation() {
        if (tabManual != null) tabManual.setOnClickListener(v -> setMode(true));
        if (tabOtomatis != null) tabOtomatis.setOnClickListener(v -> setMode(false));
    }

    private void setupSwitchListeners() {
        if (switchSiram != null) {
            switchSiram.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isManualMode && pompaRef != null) {
                    pompaRef.setValue(isChecked ? "ON" : "OFF");
                }
            });
        }

        if (switchKipas != null) {
            switchKipas.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isManualMode && kipasRef != null) {
                    kipasRef.setValue(isChecked ? "ON" : "OFF");
                }
            });
        }
    }

    private void setMode(boolean manual) {
        this.isManualMode = manual;

        // Update Mode ke Firebase
        if (modeRef != null) {
            modeRef.setValue(manual ? "Manual" : "Otomatis");
        }

        if (!isAdded()) return;

        int activeColor = ContextCompat.getColor(requireContext(), R.color.syam_green_dark);
        int lockedTextColor = Color.argb(120, 62, 78, 53); 

        if (manual) {
            tabManual.setBackgroundResource(R.drawable.bg_button_oval_white);
            tabManual.setTextColor(activeColor);
            tabOtomatis.setBackground(null);
            tabOtomatis.setTextColor(Color.WHITE);

            tvSiramLabel.setTextColor(activeColor);
            tvKipasLabel.setTextColor(activeColor);
            ivSiram.setAlpha(1.0f);
            ivKipas.setAlpha(1.0f);

            tvSiramLabel.setText("Siram Manual");
            tvKipasLabel.setText("Kipas Manual");

            switchSiram.setEnabled(true);
            switchKipas.setEnabled(true);

            // PAKSA MATI saat pindah dari Otomatis ke Manual
            switchSiram.setChecked(false);
            switchKipas.setChecked(false);

            if (pompaRef != null) pompaRef.setValue("OFF");
            if (kipasRef != null) kipasRef.setValue("OFF");

        } else {
            tabOtomatis.setBackgroundResource(R.drawable.bg_button_oval_white);
            tabOtomatis.setTextColor(activeColor);
            tabManual.setBackground(null);
            tabManual.setTextColor(Color.WHITE);

            tvSiramLabel.setTextColor(lockedTextColor);
            tvKipasLabel.setTextColor(lockedTextColor);
            ivSiram.setAlpha(0.6f); 
            ivKipas.setAlpha(0.6f);

            tvSiramLabel.setText("Siram Otomatis");
            tvKipasLabel.setText("Kipas Otomatis");

            switchSiram.setChecked(true);
            switchKipas.setChecked(true);
            switchSiram.setEnabled(false);
            switchKipas.setEnabled(false);

            if (pompaRef != null) pompaRef.setValue("ON");
            if (kipasRef != null) kipasRef.setValue("ON");
        }
    }
}
