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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ury.pos.R;
import com.ury.pos.adapter.RoomTabAdapter;
import com.ury.pos.adapter.TableAdapter;
import com.ury.pos.viewmodel.POSViewModel;
import com.ury.pos.viewmodel.TableViewModel;

public class TableTabFragment extends Fragment {

    private TableViewModel tableViewModel;
    private POSViewModel   posViewModel;
    private TableAdapter   tableAdapter;
    private RoomTabAdapter roomTabAdapter;
    private ProgressBar    progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_table_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tableViewModel = new ViewModelProvider(requireActivity()).get(TableViewModel.class);
        posViewModel   = new ViewModelProvider(requireActivity()).get(POSViewModel.class);
        progressBar    = view.findViewById(R.id.progress_bar);

        SwipeRefreshLayout swipeRefresh = view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(() -> tableViewModel.refreshCurrentRoom());

        setupRoomTabs(view);
        setupTableGrid(view);
        observe(swipeRefresh);

        posViewModel.posProfile.observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) tableViewModel.loadRooms(profile.branch);
        });
    }

    private void setupRoomTabs(View view) {
        RecyclerView rvRooms = view.findViewById(R.id.rv_rooms);
        rvRooms.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        roomTabAdapter = new RoomTabAdapter(room -> tableViewModel.selectRoom(room));
        rvRooms.setAdapter(roomTabAdapter);
    }

    private void setupTableGrid(View view) {
        RecyclerView rvTables = view.findViewById(R.id.rv_tables);
        rvTables.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        tableAdapter = new TableAdapter(table -> {
            posViewModel.selectedTable.setValue(table.name);
            posViewModel.selectedRoom.setValue(table.restaurantRoom);
            posViewModel.orderType.setValue("Dine In");
            if (getActivity() instanceof com.ury.pos.activity.POSActivity) {
                ((com.ury.pos.activity.POSActivity) getActivity()).switchToPosTab();
            }
        });
        rvTables.setAdapter(tableAdapter);
    }

    private void observe(SwipeRefreshLayout swipeRefresh) {
        tableViewModel.loading.observe(getViewLifecycleOwner(), loading -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            if (!loading) swipeRefresh.setRefreshing(false);
        });

        tableViewModel.error.observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
        });

        tableViewModel.rooms.observe(getViewLifecycleOwner(),
                rooms -> roomTabAdapter.submitList(rooms));

        tableViewModel.tables.observe(getViewLifecycleOwner(),
                tables -> tableAdapter.submitList(tables));
    }
}
