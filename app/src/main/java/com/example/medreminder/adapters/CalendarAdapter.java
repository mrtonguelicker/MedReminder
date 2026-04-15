package com.example.medreminder.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medreminder.R;
import com.example.medreminder.models.CalendarDay;

import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {

    public interface OnDayClickListener {
        void onDayClick(CalendarDay day);
    }

    private final List<CalendarDay> days;
    private final OnDayClickListener listener;

    public CalendarAdapter(List<CalendarDay> days, OnDayClickListener listener) {
        this.days = days;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {

        CalendarDay day = days.get(position);

        holder.tvDay.setText(String.valueOf(day.dayNumber));

        if (day.dayNumber == 0) {
            holder.tvDay.setText("");
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            return;
        }

        switch (day.status) {
            case "taken":
                holder.itemView.setBackgroundColor(Color.parseColor("#4CAF50"));
                break;

            case "partial":
                holder.itemView.setBackgroundColor(Color.parseColor("#FFC107"));
                break;

            case "missed":
                holder.itemView.setBackgroundColor(Color.parseColor("#F44336"));
                break;

            default:
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && day.dayNumber != 0) {
                listener.onDayClick(day);
            }
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {

        TextView tvDay;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
        }
    }
}
