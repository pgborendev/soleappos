package com.ury.pos.model;

import com.google.gson.annotations.SerializedName;

public class Payment {
    @SerializedName("mode_of_payment") public String modeOfPayment;
    @SerializedName("amount")          public double amount;

    public Payment(String modeOfPayment, double amount) {
        this.modeOfPayment = modeOfPayment;
        this.amount = amount;
    }
}
