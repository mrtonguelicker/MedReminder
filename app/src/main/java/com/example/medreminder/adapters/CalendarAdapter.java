package com.example.medreminder.adapters;

import android.util.TypedValue;
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
        void onDayClick(CalendarDay day, boolean selected);
    }

    private final List<CalendarDay> days;
    private final OnDayClickListener listener;
    private int selectedPosition = -1;

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

        if (day.dayNumber == 0) {
            holder.tvDay.setText("");
            holder.tvDay.setBackgroundResource(0);
            holder.itemView.setOnClickListener(null);
            return;
        }

        holder.tvDay.setText(String.valueOf(day.dayNumber));

        boolean isSelected = position == selectedPosition;
        boolean hasColor = false;

        // Pick the right drawable
        switch (day.status) {
            case "taken":
                holder.tvDay.setBackgroundResource(isSelected
                        ? R.drawable.calendar_day_selected_taken
                        : R.drawable.calendar_day_taken);
                hasColor = true;
                break;
            case "partial":
                holder.tvDay.setBackgroundResource(isSelected
                        ? R.drawable.calendar_day_selected_partial
                        : R.drawable.calendar_day_partial);
                hasColor = true;
                break;
            case "missed":
                holder.tvDay.setBackgroundResource(isSelected
                        ? R.drawable.calendar_day_selected_missed
                        : R.drawable.calendar_day_missed);
                hasColor = true;
                break;
            default:
                holder.tvDay.setBackgroundResource(isSelected
                        ? R.drawable.calendar_day_selected
                        : R.drawable.calendar_day_default);
                break;
        }

        // White text on colored backgrounds, theme text color on default
        if (hasColor) {
            holder.tvDay.setTextColor(0xFFFFFFFF);
        } else {
            TypedValue tv = new TypedValue();
            holder.itemView.getContext().getTheme()
                    .resolveAttribute(android.R.attr.textColorPrimary, tv, true);
            int color = holder.itemView.getContext().getColor(tv.resourceId);
            holder.tvDay.setTextColor(color);
        }

        holder.itemView.setOnClickListener(v -> {
            int prev = selectedPosition;
            int pos = holder.getAdapterPosition();

            if (pos == selectedPosition) {
                // Toggle off — deselect
                selectedPosition = -1;
                notifyItemChanged(pos);
                if (listener != null) listener.onDayClick(day, false);
            } else {
                // Select new
                selectedPosition = pos;
                if (prev >= 0) notifyItemChanged(prev);
                notifyItemChanged(pos);
                if (listener != null) listener.onDayClick(day, true);
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
