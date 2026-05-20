package com.ury.pos.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ury.pos.R;
import com.ury.pos.adapter.RoomTabAdapter;
import com.ury.pos.adapter.TableAdapter;
import com.ury.pos.model.PosProfile;
import com.ury.pos.model.Room;
import com.ury.pos.model.Table;
import com.ury.pos.viewmodel.POSViewModel;
import com.ury.pos.viewmodel.TableViewModel;

public class TableActivity extends AppCompatActivity {

    private TableViewModel     tableViewModel;
    private POSViewModel       posViewModel;
    private TableAdapter       tableAdapter;
    private RoomTabAdapter     roomTabAdapter;
    private ProgressBar        progressBar;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tables");
        }

        progressBar      = findViewById(R.id.progress_bar);
        swipeRefresh     = findViewById(R.id.swipe_refresh);
        tableViewModel   = new ViewModelProvider(this).get(TableViewModel.class);
        posViewModel     = new ViewModelProvider(this).get(POSViewModel.class);

        swipeRefresh.setOnRefreshListener(() -> tableViewModel.refreshCurrentRoom());

        setupRoomTabs();
        setupTableGrid();
        observeViewModels();

        posViewModel.posProfile.observe(this, profile -> {
            if (profile != null) tableViewModel.loadRooms(profile.branch);
        });
    }

    private void setupRoomTabs() {
        RecyclerView rvRooms = findViewById(R.id.rv_rooms);
        rvRooms.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        roomTabAdapter = new RoomTabAdapter(room -> tableViewModel.selectRoom(room));
        rvRooms.setAdapter(roomTabAdapter);
    }

    private void setupTableGrid() {
        RecyclerView rvTables = findViewById(R.id.rv_tables);
        rvTables.setLayoutManager(new GridLayoutManager(this, 2));
        tableAdapter = new TableAdapter(table -> selectTable(table));
        rvTables.setAdapter(tableAdapter);
    }

    private void selectTable(Table table) {
        posViewModel.selectedTable.setValue(table.name);
        posViewModel.selectedRoom.setValue(table.restaurantRoom);
        Intent result = new Intent();
        result.putExtra("table_name", table.name);
        result.putExtra("room_name", table.restaurantRoom);
        setResult(RESULT_OK, result);
        finish();
    }

    private void observeViewModels() {
        tableViewModel.loading.observe(this, loading -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            if (!loading) swipeRefresh.setRefreshing(false);
        });

        tableViewModel.error.observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        });

        tableViewModel.rooms.observe(this, rooms -> roomTabAdapter.submitList(rooms));

        tableViewModel.tables.observe(this, tables -> tableAdapter.submitList(tables));
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
