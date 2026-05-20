package com.ury.pos.repository;

import android.content.Context;

import com.ury.pos.api.ApiClient;
import com.ury.pos.api.ApiService;
import com.ury.pos.api.response.MenuResponse;
import com.ury.pos.model.MenuItem;
import com.ury.pos.util.ApiErrorParser;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuRepository {

    public interface MenuCallback {
        void onSuccess(List<MenuItem> items);
        void onError(String message);
    }

    private final ApiService api;

    public MenuRepository(Context context) {
        this.api = ApiClient.getService(context);
    }

    public void getMenu(String posProfile, String room, String orderType, MenuCallback callback) {
        api.getRestaurantMenu(posProfile, room, orderType).enqueue(new Callback<MenuResponse>() {
            @Override
            public void onResponse(Call<MenuResponse> call, Response<MenuResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().message != null) {
                    callback.onSuccess(response.body().message.items);
                } else {
                    callback.onError(ApiErrorParser.parse(response));
                }
            }

            @Override
            public void onFailure(Call<MenuResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getAggregatorMenu(String aggregator, MenuCallback callback) {
        api.getAggregatorMenu(aggregator).enqueue(new Callback<MenuResponse>() {
            @Override
            public void onResponse(Call<MenuResponse> call, Response<MenuResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().message != null) {
                    callback.onSuccess(response.body().message.items);
                } else {
                    callback.onError(ApiErrorParser.parse(response));
                }
            }

            @Override
            public void onFailure(Call<MenuResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}
