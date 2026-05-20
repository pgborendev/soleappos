package com.ury.pos.api.response;

import com.google.gson.annotations.SerializedName;

public class PosSessionStatusResponse {
    @SerializedName("message") public Body message;

    public static class Body {
        @SerializedName("is_open")              public boolean isOpen;
        @SerializedName("needs_close_previous") public boolean needsClosePrevious;
        @SerializedName("opening_entry")        public String openingEntry;
    }
}
