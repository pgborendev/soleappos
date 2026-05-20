package com.ury.pos.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class MenuItem implements Serializable {
    @SerializedName("item")         public String item;       // item code
    @SerializedName("item_name")    public String itemName;
    @SerializedName("item_image")   public String itemImage;
    @SerializedName("rate")         public double rate;
    @SerializedName("course")       public String course;
    @SerializedName("description")  public String description;
    @SerializedName("special_dish") public int specialDish;
    @SerializedName("trending")     public int trending;
    @SerializedName("popular")      public int popular;

    // Populated from item master when needed (variants/addons not in basic menu response)
    public List<Variant> variants;
    public List<Addon>   addons;

    public double getBaseRate() { return rate; }

    public boolean hasVariants() { return variants != null && !variants.isEmpty(); }

    public boolean hasAddons() { return addons != null && !addons.isEmpty(); }
}
