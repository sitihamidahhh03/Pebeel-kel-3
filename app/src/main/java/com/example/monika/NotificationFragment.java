package com.example.monika;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.List;

public class NotificationFragment extends Fragment {

    private LinearLayout historyContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Menggunakan layout fragment_notification yang sudah dibersihkan
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        historyContainer = view.findViewById(R.id.container_riwayat_notif);
        displayNotificationHistory();

        return view;
    }

    private void displayNotificationHistory() {
        if (getContext() == null || historyContainer == null) return;
        
        historyContainer.removeAllViews();
        List<NotificationModel> history = NotificationRepository.getNotifications(getContext());
        
        if (history.isEmpty()) {
            TextView tvEmpty = new TextView(getContext());
            tvEmpty.setText("Belum ada riwayat aktivitas terbaru.");
            tvEmpty.setPadding(40, 20, 20, 20);
            tvEmpty.setTextColor(android.graphics.Color.GRAY);
            historyContainer.addView(tvEmpty);
            return;
        }

        for (NotificationModel notif : history) {
            View cardView;
            if ("ALARM".equals(notif.getStatus())) {
                cardView = getLayoutInflater().inflate(R.layout.notification_alarm, historyContainer, false);
            } else {
                cardView = getLayoutInflater().inflate(R.layout.notification_saran, historyContainer, false);
            }

            TextView tvTitle = cardView.findViewById(R.id.tv_notif_title);
            TextView tvMsg = cardView.findViewById(R.id.tv_notif_msg);
            TextView tvTime = cardView.findViewById(R.id.tv_notif_time);
            ImageView ivIcon = cardView.findViewById(R.id.iv_notif_icon);

            if (tvTitle != null) tvTitle.setText(notif.getTitle());
            if (tvMsg != null) tvMsg.setText(notif.getMessage());
            if (tvTime != null) tvTime.setText(notif.getTime());

            if ("BANJIR".equals(notif.getStatus()) && ivIcon != null) {
                ivIcon.setImageTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.RED));
            }

            historyContainer.addView(cardView);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        displayNotificationHistory();
    }
}
