package com.ury.pos.model;

import com.google.gson.annotations.SerializedName;

public class Customer {
    @SerializedName("name")          public String id;
    @SerializedName("customer_name") public String customerName;
    @SerializedName("mobile_no")     public String phone;

    public String getDisplayName() {
        return customerName != null && !customerName.isEmpty() ? customerName : id;
    }
}
