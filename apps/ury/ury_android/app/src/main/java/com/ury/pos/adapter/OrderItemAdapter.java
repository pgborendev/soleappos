package com.ury.pos.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ury.pos.R;
import com.ury.pos.model.OrderItem;
import com.ury.pos.util.CurrencyFormatter;

public class OrderItemAdapter extends ListAdapter<OrderItem, OrderItemAdapter.VH> {

    public interface OnQtyChange {
        void onChange(String uniqueId, int delta);
    }

    public interface OnRemove {
        void onRemove(String uniqueId);
    }

    private final OnQtyChange onQtyChange;
    private final OnRemove    onRemove;

    public OrderItemAdapter(OnQtyChange onQtyChange, OnRemove onRemove) {
        super(DIFF);
        this.onQtyChange = onQtyChange;
        this.onRemove    = onRemove;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.item_order_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(getItem(position), onQtyChange, onRemove);
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView    tvName, tvQty, tvPrice;
        final ImageButton btnIncrease, btnDecrease, btnRemove;

        VH(View v) {
            super(v);
            tvName     = v.findViewById(R.id.tv_item_name);
            tvQty      = v.findViewById(R.id.tv_qty);
            tvPrice    = v.findViewById(R.id.tv_item_price);
            btnIncrease = v.findViewById(R.id.btn_increase);
            btnDecrease = v.findViewById(R.id.btn_decrease);
            btnRemove   = v.findViewById(R.id.btn_remove);
        }

        void bind(OrderItem item, OnQtyChange onQtyChange, OnRemove onRemove) {
            tvName.setText(item.itemName);
            tvQty.setText(String.valueOf((int) item.qty));
            tvPrice.setText(CurrencyFormatter.format(item.getLineTotal()));

            btnIncrease.setOnClickListener(v -> onQtyChange.onChange(item.uniqueId, 1));
            btnDecrease.setOnClickListener(v -> onQtyChange.onChange(item.uniqueId, -1));
            btnRemove.setOnClickListener(v -> onRemove.onRemove(item.uniqueId));
        }
    }

    private static final DiffUtil.ItemCallback<OrderItem> DIFF =
            new DiffUtil.ItemCallback<OrderItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull OrderItem a, @NonNull OrderItem b) {
                    return a.uniqueId.equals(b.uniqueId);
                }

                @Override
                public boolean areContentsTheSame(@NonNull OrderItem a, @NonNull OrderItem b) {
                    return a.uniqueId.equals(b.uniqueId) && a.qty == b.qty;
                }
            };
}
