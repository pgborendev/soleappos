package com.ury.pos.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ury.pos.R;
import com.ury.pos.model.Addon;
import com.ury.pos.util.CurrencyFormatter;

public class AddonAdapter extends ListAdapter<Addon, AddonAdapter.VH> {

    public interface OnAddonToggleListener {
        void onToggle(Addon addon);
    }

    private final OnAddonToggleListener listener;

    public AddonAdapter(OnAddonToggleListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.item_addon, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Addon addon = getItem(position);
        holder.cb.setText(addon.name + "  +" + CurrencyFormatter.format(addon.price));
        holder.cb.setChecked(false);
        holder.cb.setOnCheckedChangeListener((btn, checked) -> listener.onToggle(addon));
    }

    static class VH extends RecyclerView.ViewHolder {
        final CheckBox cb;

        VH(View v) {
            super(v);
            cb = v.findViewById(R.id.cb_addon);
        }
    }

    private static final DiffUtil.ItemCallback<Addon> DIFF =
            new DiffUtil.ItemCallback<Addon>() {
                @Override
                public boolean areItemsTheSame(@NonNull Addon a, @NonNull Addon b) {
                    return a.id != null && a.id.equals(b.id);
                }

                @Override
                public boolean areContentsTheSame(@NonNull Addon a, @NonNull Addon b) {
                    return a.id.equals(b.id);
                }
            };
}
