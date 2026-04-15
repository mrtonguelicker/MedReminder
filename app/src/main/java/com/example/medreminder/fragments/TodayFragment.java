package com.example.medreminder.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medreminder.R;
import com.example.medreminder.adapters.TodayAdapter;
import com.example.medreminder.models.DoseLog;
import com.example.medreminder.models.Medication;
import com.example.medreminder.models.SharedPreferencesHelper;
import com.example.medreminder.services.AlarmScheduler;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodayFragment extends Fragment implements TodayAdapter.OnDoseActionListener {

    private RecyclerView recyclerView;
    private TodayAdapter todayAdapter;
    private List<Medication> medicationList;
    private List<String> statusList;
    private SharedPreferencesHelper prefsHelper;
    private TextView tvStreak;
    private View cardRefillWarning;
    private TextView tvRefillMessage;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_today, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rv_today);
        tvStreak = view.findViewById(R.id.tv_streak);
        cardRefillWarning = view.findViewById(R.id.card_refill_warning);
        tvRefillMessage = view.findViewById(R.id.tv_refill_message);

        prefsHelper = new SharedPreferencesHelper(requireContext());

        medicationList = new ArrayList<>();
        statusList = new ArrayList<>();

        todayAdapter = new TodayAdapter(medicationList, statusList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(todayAdapter);

        loadTodayData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTodayData();
    }

    private void loadTodayData() {
        medicationList.clear();
        statusList.clear();

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        List<Medication> allMeds = prefsHelper.getAllMedications();
        for (Medication med : allMeds) {
            if (med.isActive()) {
                medicationList.add(med);
                DoseLog log = prefsHelper.getTodayDoseLog(med.getId());
                if (log != null) {
                    String status = log.getStatus();
                    String time = timeFormat.format(new Date(log.getTimestamp()));
                    if ("taken".equals(status)) {
                        statusList.add("Taken at " + time);
                    } else if ("snoozed".equals(status)) {
                        statusList.add("Snoozed at " + time);
                    } else if ("missed".equals(status)) {
                        statusList.add("Missed");
                    } else {
                        statusList.add(null);
                    }
                } else {
                    statusList.add(null);
                }
            }
        }

        todayAdapter.notifyDataSetChanged();
        updateStreak();
        checkRefills(allMeds);
    }

    private void updateStreak() {
        List<Medication> activeMeds = new ArrayList<>();
        for (Medication m : prefsHelper.getAllMedications()) {
            if (m.isActive()) activeMeds.add(m);
        }
        if (activeMeds.isEmpty()) {
            tvStreak.setText("0 days");
            return;
        }

        List<DoseLog> allLogs = prefsHelper.getAllDoseLogs();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1); // start from yesterday
        int streak = 0;

        while (true) {
            String dateStr = sdf.format(cal.getTime());
            boolean allTaken = true;
            for (Medication med : activeMeds) {
                boolean foundTaken = false;
                for (DoseLog log : allLogs) {
                    if (log.getDate().equals(dateStr)
                            && log.getMedicationId().equals(med.getId())
                            && "taken".equals(log.getStatus())) {
                        foundTaken = true;
                        break;
                    }
                }
                if (!foundTaken) {
                    allTaken = false;
                    break;
                }
            }
            if (!allTaken) break;
            streak++;
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }

        // Check if all meds taken today too
        String today = sdf.format(new Date());
        boolean allTakenToday = true;
        for (Medication med : activeMeds) {
            boolean found = false;
            for (DoseLog log : allLogs) {
                if (log.getDate().equals(today)
                        && log.getMedicationId().equals(med.getId())
                        && "taken".equals(log.getStatus())) {
                    found = true;
                    break;
                }
            }
            if (!found) { allTakenToday = false; break; }
        }
        if (allTakenToday) streak++;

        tvStreak.setText(streak + (streak == 1 ? " day" : " days"));
    }

    private void checkRefills(List<Medication> meds) {
        List<String> lowMeds = new ArrayList<>();
        for (Medication med : meds) {
            if (med.needsRefill()) {
                lowMeds.add(med.getName() + ": " + med.getPillCount() + " pill remaining");
            }
        }
        if (!lowMeds.isEmpty()) {
            cardRefillWarning.setVisibility(View.VISIBLE);
            tvRefillMessage.setText(String.join("\n", lowMeds));
        } else {
            cardRefillWarning.setVisibility(View.GONE);
        }
    }

    @Override
    public void onTake(int position) {
        Medication med = medicationList.get(position);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DoseLog log = new DoseLog(med.getId(), med.getName(), today, "taken", System.currentTimeMillis());
        prefsHelper.saveDoseLog(log);
        med.setPillCount(med.getPillCount() - 1);
        prefsHelper.saveMedication(med);
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        statusList.set(position, "Taken at " + time);
        todayAdapter.notifyItemChanged(position);
        checkRefills(medicationList);
    }

    @Override
    public void onSnooze(int position) {
        Medication med = medicationList.get(position);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DoseLog log = new DoseLog(med.getId(), med.getName(), today, "snoozed", System.currentTimeMillis());
        prefsHelper.saveDoseLog(log);
        AlarmScheduler.scheduleSnoozeAlarm(requireContext(), med, 1);
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        statusList.set(position, "Snoozed at " + time);
        todayAdapter.notifyItemChanged(position);
    }

    @Override
    public void onMiss(int position) {
        Medication med = medicationList.get(position);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DoseLog log = new DoseLog(med.getId(), med.getName(), today, "missed", System.currentTimeMillis());
        prefsHelper.saveDoseLog(log);
        statusList.set(position, "Missed");
        todayAdapter.notifyItemChanged(position);
    }
}
