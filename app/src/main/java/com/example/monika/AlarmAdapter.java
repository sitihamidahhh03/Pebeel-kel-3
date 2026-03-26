package com.example.monika;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    private Context context;
    private List<AlarmModel> alarmList;
    private OnAlarmDeleteListener deleteListener;

    public interface OnAlarmDeleteListener {
        void onDelete(AlarmModel alarm, int position);
    }

    public AlarmAdapter(Context context, List<AlarmModel> alarmList) {
        this.context = context;
        this.alarmList = alarmList;
    }

    public void setOnDeleteListener(OnAlarmDeleteListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        AlarmModel alarm = alarmList.get(position);

        // Set waktu
        holder.tvTime.setText(alarm.getTime());

        // Set label
        if (alarm.getLabel() != null && !alarm.getLabel().isEmpty()) {
            holder.tvLabel.setText(alarm.getLabel());
        } else {
            holder.tvLabel.setText("(tanpa label)");
        }

        // Set status
        if (alarm.isActive()) {
            holder.tvStatus.setText("Aktif");
        } else {
            holder.tvStatus.setText("Nonaktif");
        }

        // Tombol hapus
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(alarm, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    public void addAlarm(AlarmModel alarm) {
        alarmList.add(alarm);
        notifyItemInserted(alarmList.size() - 1);
    }

    public void removeAlarm(int position) {
        alarmList.remove(position);
        notifyItemRemoved(position);
    }

    public List<AlarmModel> getAlarmList() {
        return alarmList;
    }

    public static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvLabel, tvStatus;
        Button btnDelete;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.alarmTime);
            tvLabel = itemView.findViewById(R.id.alarmLabel);
            tvStatus = itemView.findViewById(R.id.alarmStatus);
            btnDelete = itemView.findViewById(R.id.btnDeleteAlarm);
        }
    }
}