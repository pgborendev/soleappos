package com.ury.pos.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.ury.pos.api.response.InvoiceListResponse;
import com.ury.pos.model.Order;
import com.ury.pos.repository.OrderRepository;

import java.util.ArrayList;
import java.util.List;

public class OrdersViewModel extends AndroidViewModel {

    public final MutableLiveData<List<Order>> orders       = new MutableLiveData<>(new ArrayList<>());
    public final MutableLiveData<Boolean>     loading      = new MutableLiveData<>(false);
    public final MutableLiveData<String>      error        = new MutableLiveData<>();
    public final MutableLiveData<Boolean>     hasMore      = new MutableLiveData<>(false);
    public final MutableLiveData<String>      statusFilter = new MutableLiveData<>("Draft");

    private static final int PAGE_SIZE = 20;
    private int limitStart = 0;

    private final OrderRepository repo;

    public OrdersViewModel(@NonNull Application app) {
        super(app);
        repo = new OrderRepository(app);
    }

    public void loadOrders(String status) {
        statusFilter.setValue(status);
        limitStart = 0;
        orders.setValue(new ArrayList<>());
        fetchPage(status, 0);
    }

    public void loadNextPage() {
        String status = statusFilter.getValue();
        if (status == null) return;
        fetchPage(status, limitStart);
    }

    private void fetchPage(String status, int start) {
        loading.setValue(true);
        repo.getInvoices(status, PAGE_SIZE, start, new OrderRepository.Callback1<InvoiceListResponse.MessageBody>() {
            @Override
            public void onSuccess(InvoiceListResponse.MessageBody result) {
                loading.postValue(false);
                List<Order> current = orders.getValue();
                if (current == null) current = new ArrayList<>();
                current.addAll(result.data);
                orders.postValue(current);
                hasMore.postValue(result.next);
                limitStart = start + result.data.size();
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    public void updateStatus(String invoice, String status) {
        repo.updateInvoiceStatus(invoice, status, new OrderRepository.Callback1<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadOrders(statusFilter.getValue());
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }
}
