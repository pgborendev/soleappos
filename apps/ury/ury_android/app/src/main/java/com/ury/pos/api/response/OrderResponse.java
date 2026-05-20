package com.ury.pos.api.response;

import com.google.gson.annotations.SerializedName;
import com.ury.pos.model.Order;

public class OrderResponse {
    @SerializedName("message") public Order message;
}
