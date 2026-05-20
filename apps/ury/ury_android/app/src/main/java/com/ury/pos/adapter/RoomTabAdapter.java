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
import com.ury.pos.model.Room;

public class RoomTabAdapter extends ListAdapter<Room, RoomTabAdapter.VH> {

    public interface OnRoomClickListener {
        void onClick(Room room);
    }

    private final OnRoomClickListener listener;
    private String selectedRoom;

    public RoomTabAdapter(OnRoomClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.item_category, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Room room = getItem(position);
        holder.tvLabel.setText(room.name);
        holder.itemView.setSelected(room.name.equals(selectedRoom));
        holder.itemView.setOnClickListener(v -> {
            selectedRoom = room.name;
            notifyDataSetChanged();
            listener.onClick(room);
        });
    }

    @Override
    public void submitList(java.util.List<Room> list) {
        if (list != null && !list.isEmpty() && selectedRoom == null) {
            selectedRoom = list.get(0).name;
        }
        super.submitList(list);
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvLabel;

        VH(View v) {
            super(v);
            tvLabel = v.findViewById(R.id.tv_category);
        }
    }

    private static final DiffUtil.ItemCallback<Room> DIFF =
            new DiffUtil.ItemCallback<Room>() {
                @Override
                public boolean areItemsTheSame(@NonNull Room a, @NonNull Room b) {
                    return a.name != null && a.name.equals(b.name);
                }

                @Override
                public boolean areContentsTheSame(@NonNull Room a, @NonNull Room b) {
                    return a.name.equals(b.name);
                }
            };
}
