package com.example.weighttracker_ayala;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WeightAdapter extends RecyclerView.Adapter<WeightAdapter.ViewHolder> {

    public interface OnEntryActionListener {
        void onDelete(long entryId);
        void onEdit(WeightEntry entry);
    }

    private List<WeightEntry> entries;
    private final OnEntryActionListener listener;

    public WeightAdapter(List<WeightEntry> entries, OnEntryActionListener listener) {
        this.entries = new ArrayList<>(entries);
        this.listener = listener;
    }

    // replaces the current list and refreshes the grid
    public void setEntries(List<WeightEntry> entries) {
        this.entries = new ArrayList<>(entries);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weight_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeightEntry entry = entries.get(position);

        holder.tvDate.setText(entry.getDate());
        holder.tvWeight.setText(String.format(Locale.US, "%.1f", entry.getWeight()));
        holder.tvNotes.setText(entry.getNotes().isEmpty() ? "—" : entry.getNotes());

        holder.btnDelete.setOnClickListener(v -> listener.onDelete(entry.getId()));
        // tap a row to open the edit screen for that entry
        holder.itemView.setOnClickListener(v -> listener.onEdit(entry));
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvDate;
        final TextView tvWeight;
        final TextView tvNotes;
        final MaterialButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvWeight = itemView.findViewById(R.id.tvWeight);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            btnDelete = itemView.findViewById(R.id.btnDeleteEntry);
        }
    }
}
