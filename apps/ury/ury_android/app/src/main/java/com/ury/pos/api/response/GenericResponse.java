package com.ury.pos.api.response;

import com.google.gson.annotations.SerializedName;

public class GenericResponse<T> {
    @SerializedName("message") public T message;
}
