package com.ury.pos.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.ury.pos.R;
import com.ury.pos.adapter.OrdersAdapter;
import com.ury.pos.viewmodel.OrdersViewModel;

public class OrdersTabFragment extends Fragment {

    private OrdersViewModel viewModel;
    private OrdersAdapter   adapter;
    private ProgressBar     progressBar;

    private static final String[] STATUS_OPTIONS = {
            "Draft", "Unbilled", "Recently Paid", "Paid", "Consolidated"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel   = new ViewModelProvider(requireActivity()).get(OrdersViewModel.class);
        progressBar = view.findViewById(R.id.progress_bar);

        SwipeRefreshLayout swipeRefresh = view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(() -> {
            String current = viewModel.statusFilter.getValue();
            viewModel.loadOrders(current != null ? current : "Draft");
        });

        setupStatusChips(view);
        setupOrdersList(view);
        observe(swipeRefresh);

        viewModel.loadOrders("Draft");
    }

    private void setupStatusChips(View view) {
        ChipGroup chipGroup = view.findViewById(R.id.chip_group_status);
        for (String status : STATUS_OPTIONS) {
            Chip chip = new Chip(requireContext());
            chip.setText(status);
            chip.setCheckable(true);
            chip.setChecked("Draft".equals(status));
            chip.setOnCheckedChangeListener((v, checked) -> {
                if (checked) viewModel.loadOrders(status);
            });
            chipGroup.addView(chip);
        }
    }

    private void setupOrdersList(View view) {
        RecyclerView rv = view.findViewById(R.id.rv_orders);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new OrdersAdapter(order ->
                Toast.makeText(requireContext(), order.name, Toast.LENGTH_SHORT).show());
        rv.setAdapter(adapter);
    }

    private void observe(SwipeRefreshLayout swipeRefresh) {
        viewModel.loading.observe(getViewLifecycleOwner(), loading -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            if (!loading) swipeRefresh.setRefreshing(false);
        });

        viewModel.error.observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
        });

        viewModel.orders.observe(getViewLifecycleOwner(),
                orders -> adapter.submitList(orders));
    }
}
