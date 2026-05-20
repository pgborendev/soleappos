package com.ury.pos.api.response;

import com.google.gson.annotations.SerializedName;
import com.ury.pos.model.Room;

import java.util.List;

public class RoomListResponse {
    @SerializedName("data") public List<Room> data;
}
