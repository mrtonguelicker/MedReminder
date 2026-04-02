package com.example.medreminder.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medreminder.R;
import com.example.medreminder.activities.AddMedicationActivity;
import com.example.medreminder.adapters.MedicationAdapter;
import com.example.medreminder.models.Medication;
import com.example.medreminder.models.SharedPreferencesHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MedicationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private MedicationAdapter adapter;
    private List<Medication> medicationList;
    private SharedPreferencesHelper sharedPreferencesHelper;
    private TextView tvTotal;
    private FloatingActionButton fabAdd;

    public MedicationsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_medications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rv_medications);
        tvTotal = view.findViewById(R.id.tv_total);
        fabAdd = view.findViewById(R.id.fab_add);

        sharedPreferencesHelper = new SharedPreferencesHelper(requireContext());

        medicationList = new ArrayList<>(sharedPreferencesHelper.getAllMedications());

        adapter = new MedicationAdapter(medicationList, new MedicationAdapter.OnMedicationActionListener() {
            @Override
            public void onEditClick(int position) {
                Medication medication = medicationList.get(position);

                Intent intent = new Intent(requireContext(), AddMedicationActivity.class);
                intent.putExtra("medication_id", medication.getId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(int position) {
                Medication medication = medicationList.get(position);

                sharedPreferencesHelper.deleteMedicaiton(medication.getId());
                medicationList.remove(position);
                adapter.notifyItemRemoved(position);

                updateTotalCount();
                Toast.makeText(requireContext(), "Medication deleted", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddMedicationActivity.class);
            startActivity(intent);
        });

        updateTotalCount();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (sharedPreferencesHelper != null && medicationList != null && adapter != null) {
            medicationList.clear();
            medicationList.addAll(sharedPreferencesHelper.getAllMedications());
            adapter.notifyDataSetChanged();
            updateTotalCount();
        }
    }

    private void updateTotalCount() {
        if (tvTotal != null && medicationList != null) {
            tvTotal.setText("Total: " + medicationList.size());
        }
    }
}