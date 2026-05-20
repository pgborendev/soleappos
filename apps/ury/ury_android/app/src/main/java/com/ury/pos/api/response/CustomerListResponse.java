package com.ury.pos.api.response;

import com.google.gson.annotations.SerializedName;
import com.ury.pos.model.Customer;

import java.util.List;

public class CustomerListResponse {
    @SerializedName("data") public List<Customer> data;
}
