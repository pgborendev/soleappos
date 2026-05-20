package com.ury.pos.adapter;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ury.pos.R;
import com.ury.pos.model.MenuItem;
import com.ury.pos.util.CurrencyFormatter;
import com.ury.pos.util.SessionManager;

public class MenuAdapter extends ListAdapter<MenuItem, MenuAdapter.VH> {

    public interface OnItemClickListener {
        void onClick(MenuItem item);
    }

    private final OnItemClickListener listener;

    public MenuAdapter(OnItemClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.item_menu_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView ivImage;
        final View      viewInitials;
        final TextView  tvName, tvCourse, tvPrice, tvInitials;

        VH(View v) {
            super(v);
            ivImage      = v.findViewById(R.id.iv_menu_image);
            viewInitials = v.findViewById(R.id.view_initials);
            tvInitials   = v.findViewById(R.id.tv_initials);
            tvName       = v.findViewById(R.id.tv_menu_name);
            tvCourse     = v.findViewById(R.id.tv_menu_course);
            tvPrice      = v.findViewById(R.id.tv_menu_price);
        }

        void bind(MenuItem item, OnItemClickListener listener) {
            tvName.setText(item.itemName);
            if (tvCourse != null) {
                tvCourse.setText(item.course != null ? item.course : "");
                tvCourse.setVisibility(item.course != null && !item.course.isEmpty() ? View.VISIBLE : View.GONE);
            }
            tvPrice.setText(CurrencyFormatter.format(item.rate));

            if (item.itemImage != null && !item.itemImage.isEmpty()) {
                ivImage.setVisibility(View.VISIBLE);
                viewInitials.setVisibility(View.GONE);
                String base = SessionManager.getInstance(itemView.getContext()).getBaseUrl();
                Glide.with(itemView)
                     .load(base + item.itemImage)
                     .listener(new RequestListener<Drawable>() {
                         @Override
                         public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                      Target<Drawable> target, boolean isFirstResource) {
                             ivImage.setVisibility(View.GONE);
                             viewInitials.setVisibility(View.VISIBLE);
                             tvInitials.setText(initials(item.itemName));
                             return false;
                         }
                         @Override
                         public boolean onResourceReady(Drawable resource, Object model,
                                                         Target<Drawable> target, DataSource dataSource,
                                                         boolean isFirstResource) {
                             return false;
                         }
                     })
                     .into(ivImage);
            } else {
                ivImage.setVisibility(View.GONE);
                viewInitials.setVisibility(View.VISIBLE);
                tvInitials.setText(initials(item.itemName));
            }

            itemView.setOnClickListener(v -> listener.onClick(item));
        }

        private static String initials(String name) {
            if (name == null || name.isEmpty()) return "?";
            String[] parts = name.trim().split("\\s+");
            if (parts.length >= 2) {
                return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
            }
            return name.substring(0, Math.min(2, name.length())).toUpperCase();
        }
    }

    private static final DiffUtil.ItemCallback<MenuItem> DIFF =
            new DiffUtil.ItemCallback<MenuItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull MenuItem a, @NonNull MenuItem b) {
                    return a.item != null && a.item.equals(b.item);
                }

                @Override
                public boolean areContentsTheSame(@NonNull MenuItem a, @NonNull MenuItem b) {
                    return a.item.equals(b.item) && a.rate == b.rate;
                }
            };
}
