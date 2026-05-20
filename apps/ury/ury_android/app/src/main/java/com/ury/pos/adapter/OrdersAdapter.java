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
import com.ury.pos.model.Order;
import com.ury.pos.util.CurrencyFormatter;

public class OrdersAdapter extends ListAdapter<Order, OrdersAdapter.VH> {

    public interface OnOrderClickListener {
        void onClick(Order order);
    }

    private final OnOrderClickListener listener;

    public OrdersAdapter(OnOrderClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.item_order_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvInvoice, tvCustomer, tvTable, tvTotal, tvStatus, tvTime;

        VH(View v) {
            super(v);
            tvInvoice  = v.findViewById(R.id.tv_invoice_name);
            tvCustomer = v.findViewById(R.id.tv_customer_name);
            tvTable    = v.findViewById(R.id.tv_table);
            tvTotal    = v.findViewById(R.id.tv_total);
            tvStatus   = v.findViewById(R.id.tv_status);
            tvTime     = v.findViewById(R.id.tv_time);
        }

        void bind(Order order, OnOrderClickListener listener) {
            tvInvoice.setText(order.name);
            tvCustomer.setText(order.customerName != null ? order.customerName : order.customer);
            tvTable.setText(order.restaurantTable != null ? order.restaurantTable : "-");
            tvTotal.setText(CurrencyFormatter.format(
                    order.roundedTotal > 0 ? order.roundedTotal : order.grandTotal));
            tvStatus.setText(order.status);
            tvTime.setText(order.postingTime != null ? order.postingTime : "");
            itemView.setOnClickListener(v -> listener.onClick(order));
        }
    }

    private static final DiffUtil.ItemCallback<Order> DIFF =
            new DiffUtil.ItemCallback<Order>() {
                @Override
                public boolean areItemsTheSame(@NonNull Order a, @NonNull Order b) {
                    return a.name != null && a.name.equals(b.name);
                }

                @Override
                public boolean areContentsTheSame(@NonNull Order a, @NonNull Order b) {
                    return a.name.equals(b.name) && a.status.equals(b.status);
                }
            };
}
