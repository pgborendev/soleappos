package com.ury.pos.api.response;

import com.google.gson.annotations.SerializedName;
import com.ury.pos.model.PosProfile;

public class PosProfileResponse {
    @SerializedName("message") public PosProfile message;
}
