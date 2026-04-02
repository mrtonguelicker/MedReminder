package com.example.medreminder.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medreminder.R;
import com.example.medreminder.models.Medication;

import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder> {

    public interface OnMedicationActionListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    private final List<Medication> medicationList;
    private final OnMedicationActionListener listener;

    public MedicationAdapter(List<Medication> medicationList, OnMedicationActionListener listener) {
        this.medicationList = medicationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medication, parent, false);
        return new MedicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicationViewHolder holder, int position) {
        Medication medication = medicationList.get(position);

        holder.tvName.setText(medication.getName());
        holder.tvDosage.setText("Dosage: " + medication.getDosage());
        holder.tvFrequency.setText("Frequency: " + medication.getFrequency());
        holder.tvPillCount.setText("Pills Left: " + medication.getPillCount());

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(position);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return medicationList != null ? medicationList.size() : 0;
    }

    static class MedicationViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDosage, tvFrequency, tvPillCount;
        ImageButton btnEdit, btnDelete;

        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_med_name);
            tvDosage = itemView.findViewById(R.id.tv_dosage);
            tvFrequency = itemView.findViewById(R.id.tv_frequency);
            tvPillCount = itemView.findViewById(R.id.tv_pill_count);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}