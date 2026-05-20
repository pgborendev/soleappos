package com.ury.pos.api.response;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("message") public String message;
    @SerializedName("home_page") public String homePage;
    @SerializedName("full_name") public String fullName;
}
