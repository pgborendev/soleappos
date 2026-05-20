package com.ury.pos.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ury.pos.R;
import com.ury.pos.model.Table;

public class TableAdapter extends ListAdapter<Table, TableAdapter.VH> {

    public interface OnTableClickListener {
        void onClick(Table table);
    }

    private final OnTableClickListener listener;

    public TableAdapter(OnTableClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.item_table, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvName, tvSeats, tvStatus;

        VH(View v) {
            super(v);
            tvName   = v.findViewById(R.id.tv_table_name);
            tvSeats  = v.findViewById(R.id.tv_seats);
            tvStatus = v.findViewById(R.id.tv_table_status);
        }

        void bind(Table table, OnTableClickListener listener) {
            tvName.setText(table.name);
            tvSeats.setText(table.noOfSeats > 0 ? table.noOfSeats + " seats" : "");
            tvStatus.setText(table.isOccupied() ? "Occupied" : "Available");
            itemView.setBackgroundResource(table.isOccupied()
                    ? R.drawable.bg_table_occupied
                    : R.drawable.bg_table_available);
            itemView.setOnClickListener(v -> listener.onClick(table));
        }
    }

    private static final DiffUtil.ItemCallback<Table> DIFF =
            new DiffUtil.ItemCallback<Table>() {
                @Override
                public boolean areItemsTheSame(@NonNull Table a, @NonNull Table b) {
                    return a.name != null && a.name.equals(b.name);
                }

                @Override
                public boolean areContentsTheSame(@NonNull Table a, @NonNull Table b) {
                    return a.name.equals(b.name) && a.occupied == b.occupied;
                }
            };
}
