package com.ury.pos.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Order {
    @SerializedName("name")                    public String name;
    @SerializedName("customer")                public String customer;
    @SerializedName("customer_name")           public String customerName;
    @SerializedName("mobile_number")           public String mobileNumber;
    @SerializedName("posting_date")            public String postingDate;
    @SerializedName("posting_time")            public String postingTime;
    @SerializedName("order_type")              public String orderType;
    @SerializedName("restaurant_table")        public String restaurantTable;
    @SerializedName("custom_restaurant_room")  public String restaurantRoom;
    @SerializedName("status")                  public String status;
    @SerializedName("rounded_total")           public double roundedTotal;
    @SerializedName("grand_total")             public double grandTotal;
    @SerializedName("items")                   public List<OrderLineItem> items;
    @SerializedName("waiter")                  public String waiter;
    @SerializedName("invoice_printed")         public int invoicePrinted;

    public static class OrderLineItem {
        @SerializedName("name")        public String name;
        @SerializedName("item_code")   public String itemCode;
        @SerializedName("item_name")   public String itemName;
        @SerializedName("description") public String description;
        @SerializedName("qty")         public double qty;
        @SerializedName("rate")        public double rate;
        @SerializedName("amount")      public double amount;
        @SerializedName("image")       public String image;
    }
}
