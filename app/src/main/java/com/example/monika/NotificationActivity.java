package com.example.monika;

import com.example.monika.ui.HeaderManager;
import com.example.monika.ui.FooterManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getWindow() != null) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.syam_green_dark));
        }

        setContentView(R.layout.activity_notification);

        HeaderManager headerManager = new HeaderManager(this);
        headerManager.setHeaderTitle(getString(R.string.title_notification));

        FooterManager footerManager = new FooterManager(this);
        footerManager.setActiveMenu(R.id.indicator_bell);

        LinearLayout historyContainer = findViewById(R.id.container_riwayat_notif);
        if (historyContainer != null) {
            displayNotificationHistory(historyContainer);
        }

        setupCardDropdowns();

        String statusToOpen = getIntent().getStringExtra("OPEN_STATUS");
        if (statusToOpen != null) {
            autoOpenCard(statusToOpen);
        }
    }

    private void displayNotificationHistory(LinearLayout container) {
        container.removeAllViews();
        List<NotificationModel> history = NotificationRepository.getNotifications(this);
        
        if (history.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Belum ada riwayat aktivitas terbaru.");
            tvEmpty.setPadding(40, 20, 20, 20);
            tvEmpty.setTextColor(android.graphics.Color.GRAY);
            container.addView(tvEmpty);
            return;
        }

        for (NotificationModel notif : history) {
            View cardView;
            if ("ALARM".equals(notif.getStatus())) {
                cardView = getLayoutInflater().inflate(R.layout.notification_alarm, container, false);
            } else {
                cardView = getLayoutInflater().inflate(R.layout.notification_saran, container, false);
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

            container.addView(cardView);
        }
    }

    private void autoOpenCard(String status) {
        View contentRendah = findViewById(R.id.content_rendah);
        View contentTinggi = findViewById(R.id.content_tinggi);

        switch (status) {
            case "KERING":
                if (contentRendah != null) contentRendah.setVisibility(View.VISIBLE);
                break;
            case "TINGGI":
            case "BANJIR":
                if (contentTinggi != null) contentTinggi.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setupCardDropdowns() {
        View headerRendah = findViewById(R.id.header_rendah);
        View contentRendah = findViewById(R.id.content_rendah);
        if (headerRendah != null && contentRendah != null) {
            headerRendah.setOnClickListener(v -> contentRendah.setVisibility(contentRendah.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE));
        }

        View headerTinggi = findViewById(R.id.header_tinggi);
        View contentTinggi = findViewById(R.id.content_tinggi);
        if (headerTinggi != null && contentTinggi != null) {
            headerTinggi.setOnClickListener(v -> contentTinggi.setVisibility(contentTinggi.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE));
        }
    }
}
