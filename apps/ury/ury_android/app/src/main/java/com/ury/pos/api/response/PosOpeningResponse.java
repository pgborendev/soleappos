package com.ury.pos.api.response;

import com.google.gson.annotations.SerializedName;

public class PosOpeningResponse {
    @SerializedName("message") public int message; // 1 = open, 0 = closed
}
