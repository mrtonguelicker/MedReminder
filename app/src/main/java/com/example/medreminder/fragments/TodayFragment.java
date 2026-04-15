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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

        List<Medication> allMeds = prefsHelper.getAllMedications();
        for (Medication med : allMeds) {
            if (med.isActive()) {
                medicationList.add(med);
                statusList.add(prefsHelper.getTodayStatus(med.getId()));
            }
        }

        todayAdapter.notifyDataSetChanged();
        updateStreak();
        checkRefills(allMeds);
    }

    private void updateStreak() {
        // TODO: implement streaks
        tvStreak.setText("1 days");
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
        statusList.set(position, "taken");
        todayAdapter.notifyItemChanged(position);
        checkRefills(medicationList);
    }

    @Override
    public void onSnooze(int position) {
        Medication med = medicationList.get(position);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DoseLog log = new DoseLog(med.getId(), med.getName(), today, "snoozed", System.currentTimeMillis());
        prefsHelper.saveDoseLog(log);
        statusList.set(position, "snoozed");
        todayAdapter.notifyItemChanged(position);
        // TODO: schedule snooze alarm
    }

    @Override
    public void onMiss(int position) {
        Medication med = medicationList.get(position);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DoseLog log = new DoseLog(med.getId(), med.getName(), today, "missed", System.currentTimeMillis());
        prefsHelper.saveDoseLog(log);
        statusList.set(position, "missed");
        todayAdapter.notifyItemChanged(position);
    }
}
