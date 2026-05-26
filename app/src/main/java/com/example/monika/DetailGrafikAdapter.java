package com.example.monika;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DetailGrafikAdapter extends RecyclerView.Adapter<DetailGrafikAdapter.ViewHolder> {

    private List<DetailEntry> detailList = new ArrayList<>();

    public void setData(List<DetailEntry> data) {
        this.detailList = data != null ? data : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_detail_grafik, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DetailEntry entry = detailList.get(position);
        holder.tvTime.setText(entry.label);
        holder.tvValue.setText(String.format(Locale.GERMAN, "%.1f%%", entry.value));
    }

    @Override
    public int getItemCount() {
        return detailList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvValue;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTimeLabel);
            tvValue = itemView.findViewById(R.id.tvValueLabel);
        }
    }
}
