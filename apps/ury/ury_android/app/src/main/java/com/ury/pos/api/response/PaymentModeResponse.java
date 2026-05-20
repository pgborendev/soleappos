package com.ury.pos.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PaymentModeResponse {

    @SerializedName("message")
    public List<PaymentModeItem> message;

    public static class PaymentModeItem {
        @SerializedName("mode_of_payment") public String modeOfPayment;
        @SerializedName("opening_amount")  public double openingAmount;
    }
}
