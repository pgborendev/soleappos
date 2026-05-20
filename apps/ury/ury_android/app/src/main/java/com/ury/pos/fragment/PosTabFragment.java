package com.ury.pos.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ury.pos.R;
import com.ury.pos.activity.POSActivity;
import com.ury.pos.adapter.CategoryAdapter;
import com.ury.pos.adapter.MenuAdapter;
import com.ury.pos.viewmodel.POSViewModel;

public class PosTabFragment extends Fragment {

    private static final String DINE_IN   = "Dine In";
    private static final String TAKE_AWAY = "Take Away";
    private static final String DELIVERY  = "Delivery";
    private static final String[] ORDER_TYPES = { DINE_IN, TAKE_AWAY, DELIVERY };

    private POSViewModel viewModel;
    private MenuAdapter menuAdapter;
    private CategoryAdapter categoryAdapter;
    private View progressBar;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefresh;
    private ChipGroup chipSpecialFilter;
    private ChipGroup cgOrderType;
    private TextView tvCustomerName, tvCustomerPhone;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pos_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel    = new ViewModelProvider(requireActivity()).get(POSViewModel.class);
        progressBar  = view.findViewById(R.id.progress_bar);
        tvEmpty      = view.findViewById(R.id.tv_empty);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);

        swipeRefresh.setOnRefreshListener(() -> viewModel.init());

        setupOrderTypeHeader(view);
        setupCategoryBar(view);
        setupSpecialFilter(view);
        setupMenuGrid(view);
        setupFAB(view);
        observe();
    }

    private void setupOrderTypeHeader(View view) {
        cgOrderType = view.findViewById(R.id.cg_order_type);
        tvCustomerName = view.findViewById(R.id.tv_customer_name);
        tvCustomerPhone = view.findViewById(R.id.tv_customer_phone);

        for (String type : ORDER_TYPES) {
            Chip chip = new Chip(requireContext());
            chip.setText(type);
            chip.setCheckable(true);
            chip.setTag(type);
            chip.setChipMinHeight(32f);
            chip.setTextSize(12f);
            cgOrderType.addView(chip);
        }

        cgOrderType.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            View checked = group.findViewById(checkedIds.get(0));
            if (checked == null) return;
            String type = (String) checked.getTag();
            if (DINE_IN.equals(type)) {
                if (getActivity() instanceof POSActivity) {
                    ((POSActivity) getActivity()).switchToTableTab();
                }
            } else {
                viewModel.orderType.setValue(type);
                viewModel.selectedTable.setValue(null);
                viewModel.selectedRoom.setValue(null);
            }
        });

        // Customer button
        view.findViewById(R.id.btn_customer).setOnClickListener(v ->
                new CustomerSelectFragment()
                        .show(getParentFragmentManager(), "customer_select"));
    }

    private void setupCategoryBar(View view) {
        RecyclerView rvCategories = view.findViewById(R.id.rv_categories);
        rvCategories.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryAdapter(cat -> {
            viewModel.selectedCategory.setValue(cat);
            menuAdapter.submitList(viewModel.getFilteredMenu());
        });
        rvCategories.setAdapter(categoryAdapter);
    }

    private void setupSpecialFilter(View view) {
        chipSpecialFilter = view.findViewById(R.id.chip_special_filter);
        chipSpecialFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            boolean specialOnly = !checkedIds.isEmpty() && checkedIds.get(0) == R.id.chip_special;
            viewModel.specialFilter.setValue(specialOnly);
            menuAdapter.submitList(viewModel.getFilteredMenu());
        });
    }

    private void setupMenuGrid(View view) {
        RecyclerView rvMenu = view.findViewById(R.id.rv_menu);
        int cols = getResources().getInteger(R.integer.menu_grid_columns);
        rvMenu.setLayoutManager(new GridLayoutManager(requireContext(), cols));
        menuAdapter = new MenuAdapter(menuItem -> {
            if (menuItem.hasVariants()) {
                ProductDialogFragment.newInstance(menuItem)
                        .show(getParentFragmentManager(), "product_dialog");
            } else {
                viewModel.addToCart(menuItem);
            }
        });
        rvMenu.setAdapter(menuAdapter);
    }

    private void setupFAB(View view) {
        FloatingActionButton fabCart  = view.findViewById(R.id.fab_cart);
        TextView             tvCount  = view.findViewById(R.id.tv_cart_count);
        fabCart.setOnClickListener(v ->
                new OrderPanelFragment().show(getParentFragmentManager(), "order_panel"));

        viewModel.cartItems.observe(getViewLifecycleOwner(), cart -> {
            int count = viewModel.getCartCount();
            tvCount.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
            tvCount.setText(count > 9 ? "9+" : String.valueOf(count));
        });
    }

    private void observeOrderType() {
        viewModel.orderType.observe(getViewLifecycleOwner(), type -> {
            if (type == null) return;
            for (int i = 0; i < cgOrderType.getChildCount(); i++) {
                Chip chip = (Chip) cgOrderType.getChildAt(i);
                chip.setChecked(type.equals(chip.getTag()));
            }
        });

        viewModel.selectedTable.observe(getViewLifecycleOwner(), table -> {
            for (int i = 0; i < cgOrderType.getChildCount(); i++) {
                Chip chip = (Chip) cgOrderType.getChildAt(i);
                if (DINE_IN.equals(chip.getTag())) {
                    chip.setText(table != null ? "Dine In: " + table : "Dine In");
                }
            }
        });

        viewModel.selectedCustomer.observe(getViewLifecycleOwner(), customer -> {
            tvCustomerName.setText(customer != null ? customer : "Walk In");
            tvCustomerPhone.setText("");
        });
    }

    private void observe() {
        observeOrderType();

        viewModel.loading.observe(getViewLifecycleOwner(), loading -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            if (!loading) swipeRefresh.setRefreshing(false);
        });

        viewModel.categories.observe(getViewLifecycleOwner(),
                cats -> categoryAdapter.submitList(cats));

        viewModel.menuItems.observe(getViewLifecycleOwner(), items -> {
            menuAdapter.submitList(viewModel.getFilteredMenu());
            tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.selectedCategory.observe(getViewLifecycleOwner(),
                cat -> menuAdapter.submitList(viewModel.getFilteredMenu()));

        viewModel.searchQuery.observe(getViewLifecycleOwner(),
                q -> menuAdapter.submitList(viewModel.getFilteredMenu()));

        viewModel.specialFilter.observe(getViewLifecycleOwner(),
                v -> menuAdapter.submitList(viewModel.getFilteredMenu()));
    }
}
