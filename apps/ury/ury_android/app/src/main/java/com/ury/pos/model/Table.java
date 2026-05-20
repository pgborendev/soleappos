package com.ury.pos.model;

import com.google.gson.annotations.SerializedName;

public class Table {
    @SerializedName("name")                  public String name;
    @SerializedName("occupied")              public int occupied;
    @SerializedName("latest_invoice_time")   public String latestInvoiceTime;
    @SerializedName("is_take_away")          public int isTakeAway;
    @SerializedName("restaurant_room")       public String restaurantRoom;
    @SerializedName("table_shape")           public String tableShape;
    @SerializedName("no_of_seats")           public int noOfSeats;
    @SerializedName("layout_x")             public float layoutX;
    @SerializedName("layout_y")             public float layoutY;

    public boolean isOccupied() { return occupied == 1; }
}
