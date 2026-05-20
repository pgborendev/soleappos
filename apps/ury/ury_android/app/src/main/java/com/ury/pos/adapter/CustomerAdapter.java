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
import com.ury.pos.model.Customer;

public class CustomerAdapter extends ListAdapter<Customer, CustomerAdapter.VH> {

    public interface OnCustomerClickListener {
        void onClick(Customer customer);
    }

    private final OnCustomerClickListener listener;

    public CustomerAdapter(OnCustomerClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.item_customer, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvName, tvPhone;

        VH(View v) {
            super(v);
            tvName  = v.findViewById(R.id.tv_customer_name);
            tvPhone = v.findViewById(R.id.tv_customer_phone);
        }

        void bind(Customer c, OnCustomerClickListener listener) {
            tvName.setText(c.getDisplayName());
            tvPhone.setText(c.phone != null ? c.phone : "");
            itemView.setOnClickListener(v -> listener.onClick(c));
        }
    }

    private static final DiffUtil.ItemCallback<Customer> DIFF =
            new DiffUtil.ItemCallback<Customer>() {
                @Override
                public boolean areItemsTheSame(@NonNull Customer a, @NonNull Customer b) {
                    return a.id != null && a.id.equals(b.id);
                }

                @Override
                public boolean areContentsTheSame(@NonNull Customer a, @NonNull Customer b) {
                    return a.id.equals(b.id);
                }
            };
}
