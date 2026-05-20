package com.ury.pos.api.response;

import com.google.gson.annotations.SerializedName;
import com.ury.pos.model.Order;

import java.util.List;

public class InvoiceListResponse {

    @SerializedName("message")
    public MessageBody message;

    public static class MessageBody {
        @SerializedName("data") public List<Order> data;
        @SerializedName("next") public boolean next;
    }
}
