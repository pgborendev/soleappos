package com.ury.pos.model;

import com.google.gson.annotations.SerializedName;

public class PosProfile {
    @SerializedName("pos_profile")       public String name;
    @SerializedName("restaurant")       public String restaurant;
    @SerializedName("branch")          public String branch;
    @SerializedName("cashier")         public String cashier;
    @SerializedName("owner")           public String owner;
    @SerializedName("currency")        public String currency;
    @SerializedName("enable_discount") public int enableDiscount;
    @SerializedName("view_all_status") public int viewAllStatus;
    @SerializedName("paid_limit")        public int paidLimit;
    @SerializedName("default_customer")  public String defaultCustomer;

    public boolean isDiscountEnabled() { return enableDiscount == 1; }
}
