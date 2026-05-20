package com.ury.pos.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.ury.pos.R;
import com.ury.pos.adapter.OrdersAdapter;
import com.ury.pos.viewmodel.OrdersViewModel;

public class OrdersActivity extends AppCompatActivity {

    private OrdersViewModel viewModel;
    private OrdersAdapter adapter;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;

    private static final String[] STATUS_OPTIONS = {
            "Draft", "Unbilled", "Recently Paid", "Paid", "Consolidated"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Orders");
        }

        progressBar  = findViewById(R.id.progress_bar);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        viewModel    = new ViewModelProvider(this).get(OrdersViewModel.class);

        swipeRefresh.setOnRefreshListener(() -> {
            String current = viewModel.statusFilter.getValue();
            viewModel.loadOrders(current != null ? current : "Draft");
        });

        setupStatusChips();
        setupOrdersList();
        observeViewModel();

        viewModel.loadOrders("Draft");
    }

    private void setupStatusChips() {
        ChipGroup chipGroup = findViewById(R.id.chip_group_status);
        for (String status : STATUS_OPTIONS) {
            Chip chip = new Chip(this);
            chip.setText(status);
            chip.setCheckable(true);
            chip.setChecked("Draft".equals(status));
            chip.setOnCheckedChangeListener((v, checked) -> {
                if (checked) viewModel.loadOrders(status);
            });
            chipGroup.addView(chip);
        }
    }

    private void setupOrdersList() {
        RecyclerView rv = findViewById(R.id.rv_orders);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrdersAdapter(order ->
                Toast.makeText(this, order.name, Toast.LENGTH_SHORT).show());
        rv.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.loading.observe(this, loading -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            if (!loading) swipeRefresh.setRefreshing(false);
        });

        viewModel.error.observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        });

        viewModel.orders.observe(this, orders -> adapter.submitList(orders));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
