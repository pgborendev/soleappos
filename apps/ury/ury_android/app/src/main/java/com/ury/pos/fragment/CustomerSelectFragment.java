package com.ury.pos.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ury.pos.R;
import com.ury.pos.adapter.CustomerAdapter;
import com.ury.pos.api.ApiClient;
import com.ury.pos.api.ApiService;
import com.ury.pos.api.response.CustomerListResponse;
import com.ury.pos.model.Customer;
import com.ury.pos.viewmodel.POSViewModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerSelectFragment extends BottomSheetDialogFragment {

    private POSViewModel  viewModel;
    private CustomerAdapter adapter;
    private ApiService    api;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_customer_select, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(POSViewModel.class);
        api       = ApiClient.getService(requireContext());

        EditText etSearch = view.findViewById(R.id.et_customer_search);
        RecyclerView rv   = view.findViewById(R.id.rv_customers);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CustomerAdapter(customer -> {
            viewModel.selectedCustomer.setValue(customer.id);
            dismiss();
        });
        rv.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() >= 2) searchCustomers(s.toString());
            }
        });

        searchCustomers("");
    }

    private void searchCustomers(String query) {
        String fields   = "[\"name\",\"customer_name\",\"mobile_no\"]";
        String filters  = query.isEmpty() ? "[]"
                : "[[\"customer_name\",\"like\",\"%" + query + "%\"]]";
        api.searchCustomers(fields, filters, 20).enqueue(new Callback<CustomerListResponse>() {
            @Override
            public void onResponse(Call<CustomerListResponse> call,
                                   Response<CustomerListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.submitList(response.body().data);
                }
            }

            @Override
            public void onFailure(Call<CustomerListResponse> call, Throwable t) {}
        });
    }
}
