package com.example.medreminder.adapters;

import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medreminder.R;
import com.example.medreminder.models.DoseLog;
import com.example.medreminder.models.Medication;
import java.util.List;

public class TodayAdapter extends RecyclerView.Adapter<TodayAdapter.TodayViewHolder> {


    public interface OnDoseActionListener {
        void onTake(int position);
        void onSnooze(int postiion);
        void onMiss(int position);
    }

    private final List<Medication> medicationList;
    private final List<String> statusList;
    private final OnDoseActionListener listener;

    public TodayAdapter(List<Medication> medicationList, List<String> statusList, OnDoseActionListener listener) {
        this.medicationList = medicationList;
        this.statusList = statusList;
        this.listener = listener;
    }

    @NonNull
    @Override

    public TodayViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_today_dose, parent, false);
        return new TodayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodayViewHolder holder, int position) {
        Medication medication = medicationList.get(position);
        String status = statusList.get(position);

        holder.tvName.setText(medication.getName());
        holder.tvDosage.setText(medication.getDosage());
        holder.tvTime.setText(String.format("%02d:%02d", medication.getTimeHour(), medication.getTimeMinute()));

        if (status != null) {
            holder.layoutButtons.setVisibility(View.GONE);
            holder.tvStatus.setVisibility(View.VISIBLE);
            switch (status) {
                case "taken":
                    holder.tvStatus.setText("Taken");
                    holder.tvStatus.setTextColor(0xFF4CAF50);
                    break;
                case "missed":
                    holder.tvStatus.setText("Missed");
                    holder.tvStatus.setTextColor(0xFFB71C1C);
                    break;
                case "snoozed":
                    holder.tvStatus.setText("Snoozed");
                    holder.tvStatus.setTextColor(0xFFE65100);
                    break;
            }
        } else {
            holder.layoutButtons.setVisibility(View.VISIBLE);
            holder.tvStatus.setVisibility(View.GONE);
        }

        holder.btnTake.setOnClickListener(v -> {
            if (listener != null) listener.onTake(position);
        });
        holder.btnSnooze.setOnClickListener(v -> {
            if (listener != null) listener.onSnooze(position);
        });
        holder.btnMiss.setOnClickListener(v -> {
            if (listener != null) listener.onMiss(position);
        });
    }

    @Override
    public int getItemCount() {
        return medicationList != null ? medicationList.size() : 0;
    }

    static class TodayViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDosage, tvTime, tvStatus;
        Button btnTake, btnSnooze, btnMiss;
        View layoutButtons;

        public TodayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_med_name);
            tvDosage = itemView.findViewById(R.id.tv_dosage);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnTake = itemView.findViewById(R.id.btn_take);
            btnSnooze = itemView.findViewById(R.id.btn_snooze);
            btnMiss = itemView.findViewById(R.id.btn_miss);
            layoutButtons = itemView.findViewById(R.id.layout_buttons);
        }
    }
}

