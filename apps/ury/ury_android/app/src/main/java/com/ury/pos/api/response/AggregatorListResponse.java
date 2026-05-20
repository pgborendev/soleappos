package com.ury.pos.api.response;

import com.google.gson.annotations.SerializedName;
import com.ury.pos.model.Aggregator;

import java.util.List;

public class AggregatorListResponse {
    @SerializedName("data") public List<Aggregator> data;
}
