package com.ury.pos.repository;

import android.content.Context;

import com.ury.pos.api.ApiClient;
import com.ury.pos.api.ApiService;
import com.ury.pos.api.response.GenericResponse;
import com.ury.pos.api.response.InvoiceListResponse;
import com.ury.pos.api.response.OrderResponse;
import com.ury.pos.api.response.PaymentModeResponse;
import com.ury.pos.api.response.SyncOrderResponse;
import com.ury.pos.model.Order;
import com.ury.pos.model.OrderItem;
import com.ury.pos.model.Payment;
import com.ury.pos.util.ApiErrorParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderRepository {

    public interface Callback1<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    private final ApiService api;

    public OrderRepository(Context context) {
        this.api = ApiClient.getService(context);
    }

    public void getTableOrder(String table, Callback1<Order> callback) {
        api.getTableOrder(table).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().message);
                } else {
                    callback.onError(ApiErrorParser.parse(response));
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void syncOrder(String posProfile, String cashier, String owner,
                          String orderType, String table, String room,
                          String customer, String waiter, String aggregatorId,
                          String invoice, String lastInvoice,
                          List<OrderItem> items, List<Payment> payments,
                          Callback1<String> callback) {

        Map<String, Object> body = new HashMap<>();
        body.put("pos_profile",    posProfile  != null ? posProfile  : "");
        body.put("cashier",        cashier     != null ? cashier     : "");
        body.put("owner",          owner       != null ? owner       : "");
        body.put("order_type",     orderType   != null ? orderType   : "Dine In");
        body.put("invoice",        invoice     != null ? invoice     : "");
        body.put("last_invoice",   lastInvoice != null ? lastInvoice : "");
        body.put("customer",       customer    != null ? customer    : "");
        body.put("waiter",         waiter      != null ? waiter      : "");
        body.put("no_of_pax",      1);
        if (table         != null) body.put("table",        table);
        if (room          != null) body.put("room",         room);
        if (aggregatorId  != null) body.put("aggregator_id", aggregatorId);

        List<Map<String, Object>> itemsList = new ArrayList<>();
        for (OrderItem oi : items) {
            Map<String, Object> item = new HashMap<>();
            item.put("item", oi.itemCode);
            item.put("item_name", oi.itemName);
            item.put("rate", oi.getEffectivePrice());
            item.put("qty", oi.qty);
            itemsList.add(item);
        }
        body.put("items", itemsList);

        if (payments != null && !payments.isEmpty()) {
            Map<String, Object> p = new HashMap<>();
            p.put("mode_of_payment", payments.get(0).modeOfPayment);
            p.put("amount", payments.get(0).amount);
            body.put("mode_of_payment", payments.get(0).modeOfPayment);
        }

        api.syncOrder(body).enqueue(new Callback<SyncOrderResponse>() {
            @Override
            public void onResponse(Call<SyncOrderResponse> call,
                                   Response<SyncOrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().message);
                } else {
                    callback.onError(ApiErrorParser.parse(response));
                }
            }

            @Override
            public void onFailure(Call<SyncOrderResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getInvoices(String status, int limit, int limitStart,
                            Callback1<InvoiceListResponse.MessageBody> callback) {
        api.getPosInvoices(status, limit, limitStart).enqueue(
                new Callback<InvoiceListResponse>() {
                    @Override
                    public void onResponse(Call<InvoiceListResponse> call,
                                           Response<InvoiceListResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body().message);
                        } else {
                            callback.onError(ApiErrorParser.parse(response));
                        }
                    }

                    @Override
                    public void onFailure(Call<InvoiceListResponse> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

    public void updateInvoiceStatus(String invoice, String status, Callback1<Void> callback) {
        api.updateInvoiceStatus(invoice, status).enqueue(
                new Callback<GenericResponse<String>>() {
                    @Override
                    public void onResponse(Call<GenericResponse<String>> call,
                                           Response<GenericResponse<String>> response) {
                        if (response.isSuccessful()) {
                            callback.onSuccess(null);
                        } else {
                            callback.onError(ApiErrorParser.parse(response));
                        }
                    }

                    @Override
                    public void onFailure(Call<GenericResponse<String>> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

    public void getPaymentModes(Callback1<List<String>> callback) {
        api.getPaymentModes().enqueue(new Callback<PaymentModeResponse>() {
            @Override
            public void onResponse(Call<PaymentModeResponse> call,
                                   Response<PaymentModeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> modes = new ArrayList<>();
                    for (PaymentModeResponse.PaymentModeItem item : response.body().message) {
                        modes.add(item.modeOfPayment);
                    }
                    callback.onSuccess(modes);
                } else {
                    callback.onError(ApiErrorParser.parse(response));
                }
            }

            @Override
            public void onFailure(Call<PaymentModeResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}
