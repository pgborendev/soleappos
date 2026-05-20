package com.ury.pos.model;

import com.google.gson.annotations.SerializedName;

public class Variant {
    @SerializedName("item_code") public String id;
    @SerializedName("item_name") public String name;
    @SerializedName("price")     public double price;

    public Variant(String id, String name, double price) {
        this.id = id; this.name = name; this.price = price;
    }
}
