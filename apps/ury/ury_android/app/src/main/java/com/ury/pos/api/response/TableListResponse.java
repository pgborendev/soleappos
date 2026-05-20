package com.ury.pos.api.response;

import com.google.gson.annotations.SerializedName;
import com.ury.pos.model.Table;

import java.util.List;

public class TableListResponse {
    @SerializedName("data") public List<Table> data;
}
