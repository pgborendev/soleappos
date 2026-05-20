package com.ury.pos.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.ury.pos.R;
import com.ury.pos.fragment.OrdersTabFragment;
import com.ury.pos.fragment.PosSessionFragment;
import com.ury.pos.fragment.PosTabFragment;
import com.ury.pos.fragment.TableTabFragment;
import com.ury.pos.repository.AuthRepository;
import com.ury.pos.util.SessionManager;
import com.ury.pos.viewmodel.POSViewModel;

public class POSActivity extends AppCompatActivity {

    private POSViewModel viewModel;
    private EditText     etSearch;

    private View          navPos, navTable, navOrder;
    private View          navPrevSelected;

    private PosTabFragment    posFragment;
    private TableTabFragment  tableFragment;
    private OrdersTabFragment ordersFragment;
    private Fragment          activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos);

        viewModel = new ViewModelProvider(this).get(POSViewModel.class);

        setupToolbar();
        setupFragments(savedInstanceState);
        setupBottomNav();
        observeViewModel();

        viewModel.init();
    }

    private void setupToolbar() {
        etSearch = findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                viewModel.searchQuery.setValue(s.toString());
            }
        });

        View btnUser = findViewById(R.id.btn_user);
        btnUser.setOnClickListener(v -> showUserMenu(v));
    }

    private void showUserMenu(View anchor) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.popup_user_menu, null);

        SessionManager sm = SessionManager.getInstance(this);
        String fullName = sm.getUser();
        String username = sm.getUsername();

        TextView tvName = popupView.findViewById(R.id.tv_user_name);
        TextView tvEmail = popupView.findViewById(R.id.tv_user_email);
        tvName.setText(fullName != null ? fullName : "User");
        tvEmail.setText(username != null ? username : "");

        PopupWindow popup = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        popup.setElevation(8f);
        popup.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(
                android.graphics.Color.TRANSPARENT
        ));
        popup.setOutsideTouchable(true);

        popupView.findViewById(R.id.ll_logout).setOnClickListener(v -> {
            popup.dismiss();
            logout();
        });

        popup.showAsDropDown(anchor, 0, 0);
    }

    private void setupFragments(Bundle savedInstanceState) {
        posFragment    = new PosTabFragment();
        tableFragment  = new TableTabFragment();
        ordersFragment = new OrdersTabFragment();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, ordersFragment, "orders").hide(ordersFragment)
                    .add(R.id.fragment_container, tableFragment,  "table").hide(tableFragment)
                    .add(R.id.fragment_container, posFragment,    "pos")
                    .commit();
            activeFragment = posFragment;
        } else {
            posFragment    = (PosTabFragment)    getSupportFragmentManager().findFragmentByTag("pos");
            tableFragment  = (TableTabFragment)  getSupportFragmentManager().findFragmentByTag("table");
            ordersFragment = (OrdersTabFragment) getSupportFragmentManager().findFragmentByTag("orders");
            activeFragment = posFragment;
        }
    }

    private void setupBottomNav() {
        navPos   = findViewById(R.id.nav_pos);
        navTable = findViewById(R.id.nav_table);
        navOrder = findViewById(R.id.nav_order);

        navPos.setOnClickListener(v -> {
            switchFragment(posFragment, "POS");
            selectNav(navPos);
        });
        navTable.setOnClickListener(v -> {
            switchFragment(tableFragment, "Tables");
            selectNav(navTable);
        });
        navOrder.setOnClickListener(v -> {
            switchFragment(ordersFragment, "Orders");
            selectNav(navOrder);
        });

        selectNav(navPos);
    }

    private void selectNav(View selected) {
        if (navPrevSelected != null) {
            navPrevSelected.setSelected(false);
        }
        selected.setSelected(true);
        navPrevSelected = selected;
    }

    public void switchToPosTab() {
        switchFragment(posFragment, "POS");
        selectNav(navPos);
    }

    public void switchToTableTab() {
        switchFragment(tableFragment, "Tables");
        selectNav(navTable);
    }

    private void switchFragment(Fragment target, String title) {
        if (activeFragment == target) return;
        getSupportFragmentManager().beginTransaction()
                .hide(activeFragment)
                .show(target)
                .commit();
        activeFragment = target;
    }

    private void observeViewModel() {
        viewModel.error.observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        });

        viewModel.syncedInvoice.observe(this, invoice -> {
            if (invoice != null) Toast.makeText(this, "Order saved", Toast.LENGTH_SHORT).show();
        });

        viewModel.posOpen.observe(this, open -> {
            if (Boolean.FALSE.equals(open)) {
                if (getSupportFragmentManager().findFragmentByTag("pos_session") == null) {
                    new PosSessionFragment().show(getSupportFragmentManager(), "pos_session");
                }
            }
        });
    }

    private void logout() {
        new AuthRepository(this).logout(new AuthRepository.AuthCallback<Void>() {
            @Override public void onSuccess(Void result) {
                startActivity(new Intent(POSActivity.this, LoginActivity.class));
                finish();
            }
            @Override public void onError(String message) {
                startActivity(new Intent(POSActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
}
