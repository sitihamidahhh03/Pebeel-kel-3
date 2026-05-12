package com.example.monika;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.monika.konten_dashboard.ClockManager;
import com.example.monika.konten_dashboard.MonitoringManager;
import com.example.monika.konten_dashboard.WeatherManager;

public class DashboardFragment extends Fragment {

    private ClockManager clockManager;
    private MonitoringManager monitoringManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // 1. Perkiraan Cuaca Real-time
        new WeatherManager(view);

        // 2. Jam & Tanggal
        TextView tvTime = view.findViewById(R.id.tvTime);
        TextView tvDate = view.findViewById(R.id.tvDate);
        clockManager = new ClockManager(tvTime, tvDate);

        // 3. Monitoring
        // WateringManager dihapus dari sini karena kontrol dipindah ke ControlFragment
        monitoringManager = new MonitoringManager(view, null);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (clockManager != null) clockManager.stopClock();
        if (monitoringManager != null) monitoringManager.stopMonitoring();
    }
}
