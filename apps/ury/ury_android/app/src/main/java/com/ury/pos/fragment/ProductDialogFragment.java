package com.ury.pos.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ury.pos.R;
import com.ury.pos.adapter.AddonAdapter;
import com.ury.pos.adapter.VariantAdapter;
import com.ury.pos.model.Addon;
import com.ury.pos.model.MenuItem;
import com.ury.pos.model.OrderItem;
import com.ury.pos.model.Variant;
import com.ury.pos.util.CurrencyFormatter;
import com.ury.pos.viewmodel.POSViewModel;

import java.util.ArrayList;
import java.util.List;

public class ProductDialogFragment extends BottomSheetDialogFragment {

    private static final String ARG_ITEM = "menu_item";

    private POSViewModel viewModel;
    private MenuItem menuItem;
    private Variant selectedVariant;
    private final List<Addon> selectedAddons = new ArrayList<>();

    public static ProductDialogFragment newInstance(MenuItem item) {
        ProductDialogFragment f = new ProductDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ITEM, item);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            menuItem = (MenuItem) getArguments().getSerializable(ARG_ITEM);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(POSViewModel.class);

        ImageView ivImage       = view.findViewById(R.id.iv_item_image);
        TextView tvName         = view.findViewById(R.id.tv_item_name);
        TextView tvDescription  = view.findViewById(R.id.tv_item_description);
        TextView tvPrice        = view.findViewById(R.id.tv_item_price);
        Button btnAddToCart     = view.findViewById(R.id.btn_add_to_cart);
        RecyclerView rvVariants = view.findViewById(R.id.rv_variants);
        RecyclerView rvAddons   = view.findViewById(R.id.rv_addons);

        tvName.setText(menuItem.itemName);
        tvDescription.setText(menuItem.description);
        tvPrice.setText(CurrencyFormatter.format(menuItem.getBaseRate()));

        if (menuItem.itemImage != null && !menuItem.itemImage.isEmpty()) {
            String baseUrl = com.ury.pos.util.SessionManager
                    .getInstance(requireContext()).getBaseUrl();
            Glide.with(this).load(baseUrl + menuItem.itemImage).into(ivImage);
        }

        // Variants
        if (menuItem.variants != null && !menuItem.variants.isEmpty()) {
            rvVariants.setVisibility(View.VISIBLE);
            rvVariants.setLayoutManager(new LinearLayoutManager(requireContext()));
            VariantAdapter variantAdapter = new VariantAdapter(variant -> {
                selectedVariant = variant;
                tvPrice.setText(CurrencyFormatter.format(variant.price));
            });
            variantAdapter.submitList(menuItem.variants);
            rvVariants.setAdapter(variantAdapter);
        } else {
            rvVariants.setVisibility(View.GONE);
        }

        // Addons
        if (menuItem.addons != null && !menuItem.addons.isEmpty()) {
            rvAddons.setVisibility(View.VISIBLE);
            rvAddons.setLayoutManager(new LinearLayoutManager(requireContext()));
            AddonAdapter addonAdapter = new AddonAdapter(addon -> {
                if (selectedAddons.contains(addon)) selectedAddons.remove(addon);
                else selectedAddons.add(addon);
            });
            addonAdapter.submitList(menuItem.addons);
            rvAddons.setAdapter(addonAdapter);
        } else {
            rvAddons.setVisibility(View.GONE);
        }

        btnAddToCart.setOnClickListener(v -> {
            OrderItem orderItem = new OrderItem(menuItem);
            orderItem.selectedVariant = selectedVariant;
            orderItem.selectedAddons  = new ArrayList<>(selectedAddons);
            orderItem.regenerateUniqueId();
            // Add directly to cart list
            List<com.ury.pos.model.OrderItem> cart =
                    new ArrayList<>(viewModel.cartItems.getValue() != null
                            ? viewModel.cartItems.getValue() : new ArrayList<>());
            cart.add(orderItem);
            viewModel.cartItems.setValue(cart);
            dismiss();
        });
    }

    public interface OnItemSelectedListener {
        void onSelected(OrderItem item);
    }

    private OnItemSelectedListener listener;

    public void setOnItemSelectedListener(OnItemSelectedListener l) {
        this.listener = l;
    }
}
