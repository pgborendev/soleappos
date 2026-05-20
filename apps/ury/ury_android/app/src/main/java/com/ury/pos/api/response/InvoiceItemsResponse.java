package com.ury.pos.api.response;

import com.google.gson.annotations.SerializedName;
import com.ury.pos.model.Order;
import com.ury.pos.model.InvoiceTax;

import java.util.List;

public class InvoiceItemsResponse {
    // message is a 2-element array: [items[], taxes[]]
    @SerializedName("message")
    public List<Object> message;
}
