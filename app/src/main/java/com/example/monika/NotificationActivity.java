package com.example.monika;

import com.example.monika.ui.HeaderManager;
import com.example.monika.ui.FooterManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class NotificationActivity extends AppCompatActivity {

    private boolean isExpandedGroup = false;
    private boolean isExpandedRendah = true;
    private boolean isExpandedTinggi = true;
    private boolean isExpandedMenyiram = true;
    private HeaderManager headerManager;
    private FooterManager footerManager;

    private static final String CHANNEL_ID = "syram_notifications";
    private static final int NOTIFICATION_ID = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);

        headerManager = new HeaderManager(this);
        headerManager.setHeaderTitle("Notifikasi");

        footerManager = new FooterManager(this);
        // Pastikan R.id.indicator_notification sesuai dengan ID di FooterManager kamu
        footerManager.setActiveMenu(R.id.indicator_bell);
        
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Inisialisasi semua komponen UI
        setupNotificationGroup();
        setupCardDropdowns();
        setupActionButtons();
        
        // Inisialisasi logika notifikasi sistem
        initNotificationLogic();
    }

    // 1. Dropdown Notifikasi Grup (Atas)
    private void setupNotificationGroup() {
        View header = findViewById(R.id.notification_header);
        LinearLayout summary = findViewById(R.id.notification_summary);
        LinearLayout list = findViewById(R.id.notification_list);
        ImageView arrow = findViewById(R.id.arrow_icon);

        if (header != null && summary != null && list != null && arrow != null) {
            header.setOnClickListener(v -> {
                isExpandedGroup = !isExpandedGroup;
                summary.setVisibility(isExpandedGroup ? View.GONE : View.VISIBLE);
                list.setVisibility(isExpandedGroup ? View.VISIBLE : View.GONE);
                arrow.setImageResource(isExpandedGroup ? android.R.drawable.arrow_up_float : android.R.drawable.arrow_down_float);
            });
        }
    }

    // 2. Dropdown Segitiga pada Kartu (Bawah) - FITUR YANG KAMU SUKA
    private void setupCardDropdowns() {
        // Dropdown Kelembaban Rendah
        View headerRendah = findViewById(R.id.header_rendah);
        View contentRendah = findViewById(R.id.content_rendah);
        ImageView arrowRendah = findViewById(R.id.arrow_rendah);
        if (headerRendah != null && contentRendah != null && arrowRendah != null) {
            headerRendah.setOnClickListener(v -> {
                isExpandedRendah = !isExpandedRendah;
                contentRendah.setVisibility(isExpandedRendah ? View.VISIBLE : View.GONE);
                arrowRendah.setImageResource(isExpandedRendah ? android.R.drawable.arrow_up_float : android.R.drawable.arrow_down_float);
            });
        }

        // Dropdown Kelembaban Tinggi
        View headerTinggi = findViewById(R.id.header_tinggi);
        View contentTinggi = findViewById(R.id.content_tinggi);
        ImageView arrowTinggi = findViewById(R.id.arrow_tinggi);
        if (headerTinggi != null && contentTinggi != null && arrowTinggi != null) {
            headerTinggi.setOnClickListener(v -> {
                isExpandedTinggi = !isExpandedTinggi;
                contentTinggi.setVisibility(isExpandedTinggi ? View.VISIBLE : View.GONE);
                arrowTinggi.setImageResource(isExpandedTinggi ? android.R.drawable.arrow_up_float : android.R.drawable.arrow_down_float);
            });
        }

        // Dropdown Detail Menyiram
        View headerMenyiram = findViewById(R.id.header_menyiram);
        View contentMenyiram = findViewById(R.id.content_menyiram);
        ImageView arrowMenyiram = findViewById(R.id.arrow_menyiram);
        if (headerMenyiram != null && contentMenyiram != null && arrowMenyiram != null) {
            headerMenyiram.setOnClickListener(v -> {
                isExpandedMenyiram = !isExpandedMenyiram;
                contentMenyiram.setVisibility(isExpandedMenyiram ? View.VISIBLE : View.GONE);
                arrowMenyiram.setImageResource(isExpandedMenyiram ? android.R.drawable.arrow_up_float : android.R.drawable.arrow_down_float);
            });
        }
    }

    // 3. Tombol-tombol Utama (Semua sudah HIJAU di XML)
    private void setupActionButtons() {
        Button btnSiram = findViewById(R.id.btn_siram);
        Button btnTunda = findViewById(R.id.btn_tunda);
        Button btnOke = findViewById(R.id.btn_oke);

        if (btnSiram != null) btnSiram.setOnClickListener(v -> Toast.makeText(this, "Menyiram Tanaman...", Toast.LENGTH_SHORT).show());
        if (btnTunda != null) btnTunda.setOnClickListener(v -> Toast.makeText(this, "Penyiraman Ditunda.", Toast.LENGTH_SHORT).show());
        if (btnOke != null) btnOke.setOnClickListener(v -> Toast.makeText(this, "Informasi Diterima.", Toast.LENGTH_SHORT).show());
    }

    // 4. Logika Notifikasi Sistem (KEMBALI KE ASLI)
    private void initNotificationLogic() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "SYRAM Notifications", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                showSystemNotification();
            } else {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            showSystemNotification();
        }
    }

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) showSystemNotification();
            });

    private void showSystemNotification() {
        Intent intent = new Intent(this, NotificationActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("SYRAM: Kelembaban Rendah") // TEKS ASLI
                .setContentText("Disarankan segera menyiram tanaman agar tetap sehat.") // TEKS ASLI
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi)
                .setAutoCancel(true);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(NOTIFICATION_ID, builder.build());
    }
}