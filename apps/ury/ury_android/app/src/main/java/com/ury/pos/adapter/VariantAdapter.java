package com.ury.pos.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ury.pos.R;
import com.ury.pos.model.Variant;
import com.ury.pos.util.CurrencyFormatter;

public class VariantAdapter extends ListAdapter<Variant, VariantAdapter.VH> {

    public interface OnVariantSelectedListener {
        void onSelected(Variant variant);
    }

    private final OnVariantSelectedListener listener;
    private int selectedPos = 0;

    public VariantAdapter(OnVariantSelectedListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.item_variant, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Variant variant = getItem(position);
        holder.rb.setText(variant.name + "  " + CurrencyFormatter.format(variant.price));
        holder.rb.setChecked(position == selectedPos);
        holder.itemView.setOnClickListener(v -> {
            int prev = selectedPos;
            selectedPos = holder.getAdapterPosition();
            notifyItemChanged(prev);
            notifyItemChanged(selectedPos);
            listener.onSelected(variant);
        });
    }

    static class VH extends RecyclerView.ViewHolder {
        final RadioButton rb;

        VH(View v) {
            super(v);
            rb = v.findViewById(R.id.rb_variant);
        }
    }

    private static final DiffUtil.ItemCallback<Variant> DIFF =
            new DiffUtil.ItemCallback<Variant>() {
                @Override
                public boolean areItemsTheSame(@NonNull Variant a, @NonNull Variant b) {
                    return a.id != null && a.id.equals(b.id);
                }

                @Override
                public boolean areContentsTheSame(@NonNull Variant a, @NonNull Variant b) {
                    return a.id.equals(b.id) && a.price == b.price;
                }
            };
}
