package com.example.monika;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.monika.ui.FooterManager;
import com.example.monika.ui.HeaderManager;

/**
 * File ReadAlarm yang sudah diupdate dengan fitur Hapus.
 * Kode tim tetap utuh, hanya memanggil AlarmManagerHelper untuk penghapusan sistem.
 */
public class ReadAlarm extends AppCompatActivity {

    ImageView btnAdd;
    LinearLayout containerAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_alarm);

        // 1. Inisialisasi Header & Footer
        HeaderManager header = new HeaderManager(this);
        header.setHeaderTitle("Pengingat");

        FooterManager footer = new FooterManager(this);
        footer.setActiveMenu(R.id.indicator_alarm);

        btnAdd = findViewById(R.id.btnAdd);
        containerAlarm = findViewById(R.id.containerAlarm);

        btnAdd.setOnClickListener(view -> {
            Intent intent = new Intent(ReadAlarm.this, AddAlarm.class);
            startActivityForResult(intent, 1); // ADD
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {

            String time = data.getStringExtra("time");
            String label = data.getStringExtra("label");

            if (requestCode == 1) {
                // TAMBAH
                tambahAlarm(time, label);

            } else if (requestCode == 2) {
                // EDIT
                int index = data.getIntExtra("index", -1);
                if (index != -1) {
                    updateAlarm(index, time, label);
                }
            }
        }
    }

    private void tambahAlarm(String time, String label) {

        LinearLayout item = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 24);
        item.setLayoutParams(params);

        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setBackgroundResource(R.drawable.bg_card_alarm);
        item.setPadding(32, 32, 32, 32);

        // TEXT CONTAINER
        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));

        TextView tvTime = new TextView(this);
        tvTime.setText(time);
        tvTime.setTextSize(18);
        tvTime.setTextColor(getResources().getColor(android.R.color.white));

        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextColor(getResources().getColor(android.R.color.white));

        textContainer.addView(tvTime);
        textContainer.addView(tvLabel);

        // FITUR HAPUS ALARM (Logika Baru yang Aman)
        ImageView delete = new ImageView(this);
        delete.setImageResource(android.R.drawable.ic_menu_delete);
        delete.setPadding(16, 16, 16, 16);
        delete.setOnClickListener(v -> {
            // Panggil Helper Baru kita untuk mematikan sistem alarm (ID unik berdasarkan waktu)
            AlarmManagerHelper.hapusAlarm(this, time);
            
            // Hapus dari tampilan UI (Kode asli tim)
            containerAlarm.removeView(item);
        });

        item.addView(textContainer);
        item.addView(delete);

        // CLICK UNTUK EDIT (Kode asli tim)
        item.setOnClickListener(v -> {
            Intent intent = new Intent(ReadAlarm.this, AddAlarm.class);
            intent.putExtra("time", tvTime.getText().toString());
            intent.putExtra("label", tvLabel.getText().toString());
            intent.putExtra("index", containerAlarm.indexOfChild(item));
            startActivityForResult(intent, 2); // EDIT
        });

        containerAlarm.addView(item);
    }

    private void updateAlarm(int index, String time, String label) {
        if (index < 0 || index >= containerAlarm.getChildCount()) return;

        LinearLayout item = (LinearLayout) containerAlarm.getChildAt(index);
        LinearLayout textContainer = (LinearLayout) item.getChildAt(0);

        TextView tvTime = (TextView) textContainer.getChildAt(0);
        TextView tvLabel = (TextView) textContainer.getChildAt(1);

        tvTime.setText(time);
        tvLabel.setText(label);
    }
}
