package com.example.medreminder.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medreminder.R;
import com.example.medreminder.adapters.CalendarAdapter;
import com.example.medreminder.models.CalendarDay;
import com.example.medreminder.models.DoseLog;
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
            loadCalendar();
        });

        btnNext.setOnClickListener(v -> {
            currentMonth++;
            if (currentMonth > 11) {
                currentMonth = 0;
                currentYear++;
            }
            loadCalendar();
        });

        // 🔥 EXPORT BUTTON
        btnExport.setOnClickListener(v -> exportLogs());

        loadCalendar();
    }

    // =========================
    // EXPORT FUNCTION
    // =========================
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

    // =========================
    // LOAD CALENDAR
    // =========================
    private void loadCalendar() {

        List<DoseLog> logs = prefsManager.getAllDoseLogs();

        List<CalendarDay> days = generateCalendarDays(currentYear, currentMonth, logs);

        CalendarAdapter adapter = new CalendarAdapter(days, day -> {
            showLogsForDay(day.date);
        });

        rvCalendar.setAdapter(adapter);

        Calendar cal = Calendar.getInstance();
        cal.set(currentYear, currentMonth, 1);

        String monthName = new SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                .format(cal.getTime());

        tvMonthYear.setText(monthName);

        calculateStats(logs);
    }

    private List<CalendarDay> generateCalendarDays(int year, int month, List<DoseLog> logs) {

        List<CalendarDay> days = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);

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

    private void showLogsForDay(String date) {

        List<DoseLog> logs = prefsManager.getAllDoseLogs();
        List<DoseLog> filtered = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (DoseLog log : logs) {
            String logDate = sdf.format(new Date(log.getTimestamp()));
            if (logDate.equals(date)) filtered.add(log);
        }

        if (filtered.isEmpty()) {
            Toast.makeText(getContext(), "No logs for this day", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder message = new StringBuilder();

        for (DoseLog log : filtered) {
            message.append(log.getMedicationName())
                    .append(" - ")
                    .append(log.getStatus())
                    .append("\n");
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Logs for " + date)
                .setMessage(message.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void calculateStats(List<DoseLog> logs) {

        int takenCount = 0;
        int totalCount = logs.size();

        for (DoseLog log : logs) {
            if (log.getStatus().equalsIgnoreCase("taken")) takenCount++;
        }

        int percentage = totalCount == 0 ? 0 : (takenCount * 100 / totalCount);

        tvAdherence.setText(percentage + "%");
        tvTotal.setText(String.valueOf(totalCount));
    }
}
