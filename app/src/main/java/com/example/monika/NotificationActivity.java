package com.example.monika;

import com.example.monika.ui.HeaderManager;
import com.example.monika.ui.FooterManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private HeaderManager headerManager;
    private FooterManager footerManager;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);

        headerManager = new HeaderManager(this);
        headerManager.setHeaderTitle(getString(R.string.title_notification));

        footerManager = new FooterManager(this);
        footerManager.setActiveMenu(R.id.indicator_bell);
        
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // --- KONFIGURASI SWIPE REFRESH ---
        swipeRefresh = findViewById(R.id.swipeRefresh);
        LinearLayout historyContainer = findViewById(R.id.container_riwayat_notif);

        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(() -> {
                // Refresh riwayat notifikasi
                if (historyContainer != null) {
                    displayNotificationHistory(historyContainer);
                }
                // Hentikan animasi loading setelah selesai (PENTING!)
                swipeRefresh.setRefreshing(false);
            });
        }

        // TAMPILKAN RIWAYAT PERTAMA KALI
        if (historyContainer != null) {
            displayNotificationHistory(historyContainer);
        }

        setupCardDropdowns();
        setupActionButtons();

        // LOGIKA PENERIMA NOTIF DARI SYSTEM
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
        View contentMenyiram = findViewById(R.id.content_menyiram);

        if (status.equals("KERING")) {
            if (contentRendah != null) contentRendah.setVisibility(View.VISIBLE);
        } else if (status.equals("TINGGI") || status.equals("BANJIR")) {
            if (contentTinggi != null) contentTinggi.setVisibility(View.VISIBLE);
        } else if (status.equals("ALARM")) {
            if (contentMenyiram != null) contentMenyiram.setVisibility(View.VISIBLE);
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

        View headerMenyiram = findViewById(R.id.header_menyiram);
        View contentMenyiram = findViewById(R.id.content_menyiram);
        if (headerMenyiram != null && contentMenyiram != null) {
            headerMenyiram.setOnClickListener(v -> contentMenyiram.setVisibility(contentMenyiram.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE));
        }
    }

    private void setupActionButtons() {
        if (findViewById(R.id.btn_siram) != null) findViewById(R.id.btn_siram).setOnClickListener(v -> Toast.makeText(this, "Menyiram...", Toast.LENGTH_SHORT).show());
        if (findViewById(R.id.btn_oke) != null) findViewById(R.id.btn_oke).setOnClickListener(v -> Toast.makeText(this, "Oke!", Toast.LENGTH_SHORT).show());
    }
}
