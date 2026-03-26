package com.example.monika;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.monika.R;
import com.example.monika.AlarmModel;
import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    private Context context;
    private List<AlarmModel> alarmList;
    private OnAlarmToggleListener toggleListener;
    private OnAlarmDeleteListener deleteListener;

    public interface OnAlarmToggleListener {
        void onToggle(AlarmModel alarm, boolean isChecked);
    }

    public interface OnAlarmDeleteListener {
        void onDelete(AlarmModel alarm, int position);
    }

    public AlarmAdapter(Context context, List<AlarmModel> alarmList,
                        OnAlarmToggleListener toggleListener) {
        this.context = context;
        this.alarmList = alarmList;
        this.toggleListener = toggleListener;
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

        holder.tvTime.setText(alarm.getTime());

        // Tampilkan label, jika kosong tampilkan pesan default
        if (alarm.getLabel() != null && !alarm.getLabel().isEmpty()) {
            holder.tvLabel.setText(alarm.getLabel());
            holder.tvLabel.setVisibility(View.VISIBLE);
        } else {
            holder.tvLabel.setText("(tanpa label)");
            holder.tvLabel.setVisibility(View.VISIBLE);
        }

        holder.switchActive.setChecked(alarm.isActive());

        // Set listener untuk switch
        holder.switchActive.setOnCheckedChangeListener(null); // Reset listener dulu
        holder.switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (toggleListener != null) {
                toggleListener.onToggle(alarm, isChecked);
            }
        });

        // Set long click listener untuk hapus (opsional)
        holder.itemView.setOnLongClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(alarm, position);
                return true;
            }
            return false;
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
        notifyItemRangeChanged(position, alarmList.size() - position);
    }

    public void updateAlarmStatus(int position, boolean isActive) {
        alarmList.get(position).setActive(isActive);
        notifyItemChanged(position);
    }

    public List<AlarmModel> getAlarmList() {
        return alarmList;
    }

    public static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvLabel;
        SwitchCompat switchActive;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_alarm_time);
            tvLabel = itemView.findViewById(R.id.tv_alarm_label);
            switchActive = itemView.findViewById(R.id.switch_alarm_active);
        }
    }
}