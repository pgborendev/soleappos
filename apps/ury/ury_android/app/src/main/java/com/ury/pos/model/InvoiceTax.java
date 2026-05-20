package com.ury.pos.model;

import com.google.gson.annotations.SerializedName;

public class InvoiceTax {
    @SerializedName("description") public String description;
    @SerializedName("rate")        public double rate;
}
