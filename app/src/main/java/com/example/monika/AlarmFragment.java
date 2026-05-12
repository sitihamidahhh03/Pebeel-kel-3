package com.example.monika;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AlarmFragment extends Fragment {
    private ImageView btnAdd;
    private LinearLayout containerAlarm;

    private final ActivityResultLauncher<Intent> alarmLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    String time = result.getData().getStringExtra("time");
                    String label = result.getData().getStringExtra("label");
                    tambahAlarm(time, label);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // PERBAIKAN: Gunakan fragment_alarm, bukan activity_read_alarm agar tidak duplikat
        View view = inflater.inflate(R.layout.fragment_alarm, container, false);
        btnAdd = view.findViewById(R.id.btnAdd);
        containerAlarm = view.findViewById(R.id.containerAlarm);
        
        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> alarmLauncher.launch(new Intent(getContext(), AddAlarm.class)));
        }
        return view;
    }

    private void tambahAlarm(String time, String label) {
        if (getContext() == null) return;
        LinearLayout item = new LinearLayout(getContext());
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setBackgroundResource(R.drawable.bg_card_alarm);
        item.setPadding(40, 40, 40, 40);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, 0, 0, 24);
        item.setLayoutParams(p);

        TextView tv = new TextView(getContext());
        tv.setText(time + " - " + label);
        tv.setTextColor(android.graphics.Color.WHITE);
        tv.setTextSize(16);
        item.addView(tv);
        containerAlarm.addView(item);
    }
}
