package com.example.medreminder.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medreminder.R;
import com.example.medreminder.models.Medication;

import java.util.List;

public class TodayAdapter extends RecyclerView.Adapter<TodayAdapter.TodayViewHolder> {

    public interface OnDoseActionListener {
        void onTake(int position);
        void onSnooze(int position);
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
    public TodayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
        holder.tvTime.setText(String.format("%02d:%02d",
                medication.getTimeHour(),
                medication.getTimeMinute()));

        if (status != null) {
            holder.layoutButtons.setVisibility(View.GONE);
            holder.layoutStatus.setVisibility(View.VISIBLE);
            holder.tvStatus.setText(status);

            if (status.startsWith("Taken")) {
                holder.layoutStatus.setBackgroundResource(R.drawable.status_bar_bg);
                holder.tvStatusIcon.setText("\u2713");
            } else if (status.startsWith("Missed")) {
                holder.layoutStatus.setBackgroundResource(R.drawable.status_bar_bg_missed);
                holder.tvStatusIcon.setText("\u2717");
            } else if (status.startsWith("Snoozed")) {
                holder.layoutStatus.setBackgroundResource(R.drawable.status_bar_bg_snoozed);
                holder.tvStatusIcon.setText("\u23F0");
            }
        } else {
            holder.layoutButtons.setVisibility(View.VISIBLE);
            holder.layoutStatus.setVisibility(View.GONE);
        }

        holder.btnTake.setOnClickListener(v -> {
            if (listener != null) listener.onTake(position);
        });

        holder.btnMiss.setOnClickListener(v -> {
            if (listener != null) listener.onMiss(position);
        });

        holder.btnSnooze.setOnClickListener(v -> {
            if (listener != null) listener.onSnooze(position);
        });
    }

    @Override
    public int getItemCount() {
        return medicationList != null ? medicationList.size() : 0;
    }

    static class TodayViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvDosage, tvTime, tvStatus, tvStatusIcon;
        Button btnTake, btnSnooze, btnMiss;
        View layoutButtons, layoutStatus;

        public TodayViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tv_med_name);
            tvDosage = itemView.findViewById(R.id.tv_dosage);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvStatusIcon = itemView.findViewById(R.id.tv_status_icon);

            btnTake = itemView.findViewById(R.id.btn_take);
            btnSnooze = itemView.findViewById(R.id.btn_snooze);
            btnMiss = itemView.findViewById(R.id.btn_miss);

            layoutButtons = itemView.findViewById(R.id.layout_buttons);
            layoutStatus = itemView.findViewById(R.id.layout_status);
        }
    }
}
