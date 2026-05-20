package com.ury.pos.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ury.pos.R;
import com.ury.pos.adapter.OrderItemAdapter;
import com.ury.pos.util.CurrencyFormatter;
import com.ury.pos.viewmodel.POSViewModel;

public class OrderPanelFragment extends BottomSheetDialogFragment {

    private POSViewModel viewModel;
    private OrderItemAdapter adapter;

    private TextView tvItemCount, tvTotal;
    private Button   btnClear, btnAddOrder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_panel, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Expand the sheet to 90 % of screen height
        BottomSheetDialog dialog = (BottomSheetDialog) requireDialog();
        View sheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (sheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(sheet);
            sheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel   = new ViewModelProvider(requireActivity()).get(POSViewModel.class);
        tvItemCount = view.findViewById(R.id.tv_item_count);
        tvTotal     = view.findViewById(R.id.tv_total);
        btnClear    = view.findViewById(R.id.btn_clear);
        btnAddOrder = view.findViewById(R.id.btn_add_order);

        RecyclerView rv = view.findViewById(R.id.rv_order_items);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new OrderItemAdapter(
                (uniqueId, delta) -> viewModel.updateCartItemQty(uniqueId, delta),
                uniqueId -> viewModel.removeCartItem(uniqueId)
        );
        rv.setAdapter(adapter);

        // Observe cart
        viewModel.cartItems.observe(getViewLifecycleOwner(), items -> {
            adapter.submitList(items);
            int count = viewModel.getCartCount();
            tvItemCount.setText(count + " item" + (count != 1 ? "s" : ""));
            tvTotal.setText(CurrencyFormatter.format(viewModel.getCartTotal()));
            btnAddOrder.setEnabled(items != null && !items.isEmpty());
        });

        // Buttons
        btnClear.setOnClickListener(v -> {
            viewModel.clearCart();
            dismiss();
        });

        btnAddOrder.setOnClickListener(v -> {
            btnAddOrder.setEnabled(false);
            viewModel.addNewOrder(() ->
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Order sent", Toast.LENGTH_SHORT).show();
                        dismiss();
                    })
            );
        });
    }

}
