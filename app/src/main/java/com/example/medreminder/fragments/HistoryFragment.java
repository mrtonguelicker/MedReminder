package com.example.medreminder.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medreminder.R;
import com.example.medreminder.adapters.CalendarAdapter;
import com.example.medreminder.models.CalendarDay;
import com.example.medreminder.models.DoseLog;
import com.example.medreminder.models.Medication;
import com.example.medreminder.models.SharedPreferencesHelper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class HistoryFragment extends Fragment {

    private RecyclerView rvCalendar;
    private TextView tvAdherence, tvTotal, tvMonthYear;
    private ImageButton btnPrev, btnNext;
    private Button btnExport;

    private View cardDayDetail;
    private TextView tvDetailTitle;
    private LinearLayout layoutDetailLogs;

    private View layoutStats;

    private SharedPreferencesHelper prefsManager;

    private int currentYear;
    private int currentMonth;

    public HistoryFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvCalendar = view.findViewById(R.id.rv_calendar);
        tvAdherence = view.findViewById(R.id.tv_adherence_rate);
        tvTotal = view.findViewById(R.id.tv_total_doses);
        tvMonthYear = view.findViewById(R.id.tv_month_year);

        btnPrev = view.findViewById(R.id.btn_prev_month);
        btnNext = view.findViewById(R.id.btn_next_month);
        btnExport = view.findViewById(R.id.btn_export);

        cardDayDetail = view.findViewById(R.id.card_day_detail);
        tvDetailTitle = view.findViewById(R.id.tv_detail_title);
        layoutDetailLogs = view.findViewById(R.id.layout_detail_logs);
        layoutStats = view.findViewById(R.id.layout_stats);

        prefsManager = new SharedPreferencesHelper(getContext());

        rvCalendar.setLayoutManager(new GridLayoutManager(getContext(), 7));

        Calendar calendar = Calendar.getInstance();
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH);

        btnPrev.setOnClickListener(v -> {
            currentMonth--;
            if (currentMonth < 0) {
                currentMonth = 11;
                currentYear--;
            }
            cardDayDetail.setVisibility(View.GONE);
            loadCalendar();
        });

        btnNext.setOnClickListener(v -> {
            currentMonth++;
            if (currentMonth > 11) {
                currentMonth = 0;
                currentYear++;
            }
            cardDayDetail.setVisibility(View.GONE);
            loadCalendar();
        });

        btnExport.setOnClickListener(v -> exportLogs());

        loadCalendar();
    }

    private void exportLogs() {
        List<DoseLog> logs = prefsManager.getAllDoseLogs();

        if (logs.isEmpty()) {
            Toast.makeText(getContext(), "No data to export", Toast.LENGTH_SHORT).show();
            return;
        }

        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsDir, "med_history.csv");

        try {
            FileWriter writer = new FileWriter(file);
            writer.append("Medication Name,Status,Date,Timestamp\n");

            for (DoseLog log : logs) {
                writer.append(log.getMedicationName()).append(",");
                writer.append(log.getStatus()).append(",");
                writer.append(log.getDate()).append(",");
                writer.append(String.valueOf(log.getTimestamp())).append("\n");
            }

            writer.flush();
            writer.close();

            Toast.makeText(getContext(), "Exported to Downloads/med_history.csv", Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Export failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCalendar() {
        List<DoseLog> allLogs = prefsManager.getAllDoseLogs();
        List<CalendarDay> days = generateCalendarDays(currentYear, currentMonth, allLogs);

        CalendarAdapter adapter = new CalendarAdapter(days, (day, selected) -> {
            if (selected) {
                showDayDetail(day.date);
            } else {
                cardDayDetail.setVisibility(View.GONE);
            }
        });

        rvCalendar.setAdapter(adapter);

        Calendar cal = Calendar.getInstance();
        cal.set(currentYear, currentMonth, 1);

        String monthName = new SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                .format(cal.getTime());

        tvMonthYear.setText(monthName);

        // Filter logs for this month only
        List<DoseLog> monthLogs = getLogsForMonth(allLogs, currentYear, currentMonth);
        calculateStats(monthLogs);
    }

    private List<DoseLog> getLogsForMonth(List<DoseLog> allLogs, int year, int month) {
        List<DoseLog> filtered = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        for (DoseLog log : allLogs) {
            cal.setTimeInMillis(log.getTimestamp());
            if (cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month) {
                filtered.add(log);
            }
        }
        return filtered;
    }

    private List<CalendarDay> generateCalendarDays(int year, int month, List<DoseLog> logs) {
        List<CalendarDay> days = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);

        // Add empty padding for days before the 1st
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // Sunday = 0
        for (int i = 0; i < firstDayOfWeek; i++) {
            days.add(new CalendarDay(0, "", "none"));
        }

        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (int i = 1; i <= daysInMonth; i++) {
            calendar.set(Calendar.DAY_OF_MONTH, i);
            String dateStr = sdf.format(calendar.getTime());
            String status = getStatusForDate(dateStr, logs);
            days.add(new CalendarDay(i, dateStr, status));
        }

        return days;
    }

    private String getStatusForDate(String date, List<DoseLog> logs) {
        int taken = 0;
        int missed = 0;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (DoseLog log : logs) {
            String logDate = sdf.format(new Date(log.getTimestamp()));
            if (logDate.equals(date)) {
                if (log.getStatus().equalsIgnoreCase("taken")) taken++;
                else missed++;
            }
        }

        if (taken > 0 && missed == 0) return "taken";
        if (taken > 0) return "partial";
        if (missed > 0) return "missed";

        return "none";
    }

    private void showDayDetail(String date) {
        List<DoseLog> logs = prefsManager.getAllDoseLogs();
        List<DoseLog> filtered = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (DoseLog log : logs) {
            String logDate = sdf.format(new Date(log.getTimestamp()));
            if (logDate.equals(date)) filtered.add(log);
        }

        if (filtered.isEmpty()) {
            cardDayDetail.setVisibility(View.GONE);
            Toast.makeText(getContext(), "No logs for this day", Toast.LENGTH_SHORT).show();
            return;
        }

        // Format title: "Thursday, February 26"
        try {
            Date dateObj = sdf.parse(date);
            if (dateObj != null) {
                String title = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(dateObj);
                tvDetailTitle.setText(title);
            }
        } catch (Exception e) {
            tvDetailTitle.setText(date);
        }

        // Build medication lookup for scheduled times
        Map<String, Medication> medMap = new HashMap<>();
        for (Medication med : prefsManager.getAllMedications()) {
            medMap.put(med.getId(), med);
        }

        // Populate log entries
        layoutDetailLogs.removeAllViews();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        for (DoseLog log : filtered) {
            View logView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_day_detail_log, layoutDetailLogs, false);

            TextView tvMedName = logView.findViewById(R.id.tv_log_med_name);
            TextView tvScheduledTime = logView.findViewById(R.id.tv_log_scheduled_time);
            TextView tvStatus = logView.findViewById(R.id.tv_log_status);

            tvMedName.setText(log.getMedicationName());

            // Show scheduled time from medication data
            Medication med = medMap.get(log.getMedicationId());
            if (med != null) {
                tvScheduledTime.setText(String.format("%02d:%02d", med.getTimeHour(), med.getTimeMinute()));
            } else {
                tvScheduledTime.setText("");
            }

            // Status chip
            String status = log.getStatus();
            String takenTime = timeFormat.format(new Date(log.getTimestamp()));

            if ("taken".equalsIgnoreCase(status)) {
                tvStatus.setText("Taken " + takenTime);
                tvStatus.setBackgroundResource(R.drawable.chip_taken);
            } else if ("missed".equalsIgnoreCase(status)) {
                tvStatus.setText("Missed");
                tvStatus.setBackgroundResource(R.drawable.chip_missed);
            } else if ("snoozed".equalsIgnoreCase(status)) {
                tvStatus.setText("Snoozed " + takenTime);
                tvStatus.setBackgroundResource(R.drawable.chip_snoozed);
            } else {
                tvStatus.setText(status);
                tvStatus.setBackgroundResource(R.drawable.chip_taken);
            }

            layoutDetailLogs.addView(logView);
        }

        cardDayDetail.setVisibility(View.VISIBLE);
    }

    private void calculateStats(List<DoseLog> logs) {
        int totalCount = logs.size();

        if (totalCount == 0) {
            // Animate out
            if (layoutStats.getVisibility() == View.VISIBLE) {
                layoutStats.animate()
                        .alpha(0f)
                        .translationY(-layoutStats.getHeight())
                        .setDuration(250)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                layoutStats.setVisibility(View.GONE);
                            }
                        })
                        .start();
            } else {
                layoutStats.setVisibility(View.GONE);
            }
            return;
        }

        int takenCount = 0;
        for (DoseLog log : logs) {
            if (log.getStatus().equalsIgnoreCase("taken")) takenCount++;
        }

        int percentage = (takenCount * 100 / totalCount);
        tvAdherence.setText(percentage + "%");
        tvTotal.setText(String.valueOf(totalCount));

        // Animate in
        if (layoutStats.getVisibility() != View.VISIBLE) {
            layoutStats.setAlpha(0f);
            layoutStats.setTranslationY(-40f);
            layoutStats.setVisibility(View.VISIBLE);
            layoutStats.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .setListener(null)
                    .start();
        }
    }
}
