package com.example.monika;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.monika.ui.FooterManager;
import com.example.monika.ui.HeaderManager;

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

        // FITUR HAPUS ALARM (Kembali pakai Icon Sampah + Custom Dialog)
        ImageView ivDelete = new ImageView(this);
        ivDelete.setImageResource(android.R.drawable.ic_menu_delete);
        ivDelete.setPadding(16, 16, 16, 16);

        // Memberikan warna putih pada ikon
        ivDelete.setImageTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));

        ivDelete.setOnClickListener(v -> {
            // 1. Inflate layout custom dialog
            android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_konfirmasi_hapus, null);
            androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setView(dialogView)
                    .create();

            // 2. Membuat background dialog transparan agar sudut rounded CardView terlihat
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            }

            // 3. Mengatur teks pesan sesuai jam alarm yang dipilih
            TextView tvMessage = dialogView.findViewById(R.id.tvMessageDialog);
            if (tvMessage != null) {
                tvMessage.setText("Apakah Anda yakin ingin menghapus alarm jam " + time + "?");
            }

            // 4. Logika tombol NO (Batal)
            dialogView.findViewById(R.id.btnNo).setOnClickListener(v1 -> dialog.dismiss());

            // 5. Logika tombol YES (Hapus)
            dialogView.findViewById(R.id.btnYes).setOnClickListener(v1 -> {
                AlarmManagerHelper.hapusAlarm(this, time); // Hapus dari sistem
                containerAlarm.removeView(item);           // Hapus dari layar
                dialog.dismiss();
            });

            dialog.show();
        });

        item.addView(textContainer);
        item.addView(ivDelete); // Masukkan kembali ikon sampah ke dalam kartu


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
