package com.ury.pos.api.response;

import com.google.gson.annotations.SerializedName;
import com.ury.pos.model.MenuItem;

import java.util.List;

public class MenuResponse {

    @SerializedName("message")
    public MessageBody message;

    public static class MessageBody {
        @SerializedName("items") public List<MenuItem> items;
    }
}
