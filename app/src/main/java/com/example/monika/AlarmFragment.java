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
                    Intent data = result.getData();
                    String time = data.getStringExtra("time");
                    String label = data.getStringExtra("label");
                    int index = data.getIntExtra("index", -1);

                    if (index == -1) {
                        tambahAlarm(time, label);
                    } else {
                        updateAlarm(index, time, label);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // MENGGUNAKAN LAYOUT FRAGMENT (BUKAN ACTIVITY)
        View view = inflater.inflate(R.layout.fragment_alarm, container, false);

        btnAdd = view.findViewById(R.id.btnAdd);
        containerAlarm = view.findViewById(R.id.containerAlarm);

        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), AddAlarm.class);
                alarmLauncher.launch(intent);
            });
        }

        return view;
    }

    private void tambahAlarm(String time, String label) {
        if (getContext() == null) return;

        LinearLayout item = new LinearLayout(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 24);
        item.setLayoutParams(params);

        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setBackgroundResource(R.drawable.bg_card_alarm);
        item.setPadding(32, 32, 32, 32);

        LinearLayout textContainer = new LinearLayout(getContext());
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView tvTime = new TextView(getContext());
        tvTime.setText(time);
        tvTime.setTextSize(18);
        tvTime.setTextColor(getResources().getColor(android.R.color.white));

        TextView tvLabel = new TextView(getContext());
        tvLabel.setText(label);
        tvLabel.setTextColor(getResources().getColor(android.R.color.white));

        textContainer.addView(tvTime);
        textContainer.addView(tvLabel);

        ImageView ivDelete = new ImageView(getContext());
        ivDelete.setImageResource(android.R.drawable.ic_menu_delete);
        ivDelete.setPadding(16, 16, 16, 16);
        ivDelete.setImageTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));

        ivDelete.setOnClickListener(v -> {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_konfirmasi_hapus, null);
            androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setView(dialogView)
                    .create();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            }

            TextView tvMessage = dialogView.findViewById(R.id.tvMessageDialog);
            if (tvMessage != null) tvMessage.setText("Apakah Anda yakin ingin menghapus alarm jam " + time + "?");

            dialogView.findViewById(R.id.btnNo).setOnClickListener(v1 -> dialog.dismiss());
            dialogView.findViewById(R.id.btnYes).setOnClickListener(v1 -> {
                AlarmManagerHelper.hapusAlarm(getContext(), time);
                containerAlarm.removeView(item);
                dialog.dismiss();
            });
            dialog.show();
        });

        item.addView(textContainer);
        item.addView(ivDelete);

        item.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddAlarm.class);
            intent.putExtra("time", tvTime.getText().toString());
            intent.putExtra("label", tvLabel.getText().toString());
            intent.putExtra("index", containerAlarm.indexOfChild(item));
            alarmLauncher.launch(intent);
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
