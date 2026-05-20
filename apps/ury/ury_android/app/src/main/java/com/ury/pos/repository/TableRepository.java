package com.ury.pos.repository;

import android.content.Context;

import com.ury.pos.api.ApiClient;
import com.ury.pos.api.ApiService;
import com.ury.pos.api.response.RoomListResponse;
import com.ury.pos.api.response.TableListResponse;
import com.ury.pos.model.Room;
import com.ury.pos.model.Table;
import com.ury.pos.util.ApiErrorParser;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TableRepository {

    public interface TableCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    private final ApiService api;

    public TableRepository(Context context) {
        this.api = ApiClient.getService(context);
    }

    public void getRooms(String branch, TableCallback<List<Room>> callback) {
        String fields = "[\"name\",\"branch\"]";
        String filters = "[[\"branch\",\"like\",\"" + branch + "\"]]";
        api.getRooms(fields, filters, 100).enqueue(new Callback<RoomListResponse>() {
            @Override
            public void onResponse(Call<RoomListResponse> call,
                                   Response<RoomListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().data);
                } else {
                    callback.onError(ApiErrorParser.parse(response));
                }
            }

            @Override
            public void onFailure(Call<RoomListResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getTables(String room, TableCallback<List<Table>> callback) {
        String fields = "[\"name\",\"occupied\",\"latest_invoice_time\",\"is_take_away\"," +
                "\"restaurant_room\",\"table_shape\",\"no_of_seats\",\"layout_x\",\"layout_y\"]";
        String filters = "[[\"restaurant_room\",\"=\",\"" + room + "\"]]";
        api.getTables(fields, filters, 200).enqueue(new Callback<TableListResponse>() {
            @Override
            public void onResponse(Call<TableListResponse> call,
                                   Response<TableListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().data);
                } else {
                    callback.onError(ApiErrorParser.parse(response));
                }
            }

            @Override
            public void onFailure(Call<TableListResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}
