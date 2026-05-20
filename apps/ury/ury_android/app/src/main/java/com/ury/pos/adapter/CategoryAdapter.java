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

public class CategoryAdapter extends ListAdapter<String, CategoryAdapter.VH> {

    public interface OnCategoryClickListener {
        void onClick(String category);
    }

    private final OnCategoryClickListener listener;
    private String selectedCategory = "All";

    public CategoryAdapter(OnCategoryClickListener listener) {
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
        String cat = getItem(position);
        holder.tvCategory.setText(cat);
        holder.itemView.setSelected(cat.equals(selectedCategory));
        holder.itemView.setOnClickListener(v -> {
            selectedCategory = cat;
            notifyDataSetChanged();
            listener.onClick(cat);
        });
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvCategory;

        VH(View v) {
            super(v);
            tvCategory = v.findViewById(R.id.tv_category);
        }
    }

    private static final DiffUtil.ItemCallback<String> DIFF =
            new DiffUtil.ItemCallback<String>() {
                @Override
                public boolean areItemsTheSame(@NonNull String a, @NonNull String b) {
                    return a.equals(b);
                }

                @Override
                public boolean areContentsTheSame(@NonNull String a, @NonNull String b) {
                    return a.equals(b);
                }
            };
}
