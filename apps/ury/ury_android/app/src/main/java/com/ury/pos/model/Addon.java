package com.ury.pos.model;

import com.google.gson.annotations.SerializedName;

public class Addon {
    @SerializedName("item_code") public String id;
    @SerializedName("item_name") public String name;
    @SerializedName("price")     public double price;
    @SerializedName("category")  public String category;

    public Addon() {}
    public Addon(String id, String name, double price) {
        this.id = id; this.name = name; this.price = price;
    }
}
