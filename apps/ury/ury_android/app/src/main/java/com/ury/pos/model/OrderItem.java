package com.ury.pos.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderItem {
    public String       uniqueId;
    public String       itemCode;
    public String       itemName;
    public String       itemImage;
    public double       rate;
    public double       qty;
    public String       course;
    public String       comment;
    public Variant      selectedVariant;
    public List<Addon>  selectedAddons;

    public OrderItem(OrderItem other) {
        this.uniqueId       = other.uniqueId;
        this.itemCode       = other.itemCode;
        this.itemName       = other.itemName;
        this.itemImage      = other.itemImage;
        this.rate           = other.rate;
        this.qty            = other.qty;
        this.course         = other.course;
        this.comment        = other.comment;
        this.selectedVariant = other.selectedVariant;
        this.selectedAddons  = other.selectedAddons != null
                ? new ArrayList<>(other.selectedAddons) : null;
    }

    public OrderItem(MenuItem menuItem) {
        this.uniqueId      = UUID.randomUUID().toString();
        this.itemCode      = menuItem.item;
        this.itemName      = menuItem.itemName;
        this.itemImage     = menuItem.itemImage;
        this.rate          = menuItem.rate;
        this.qty           = 1;
        this.course        = menuItem.course;
        this.selectedAddons = new ArrayList<>();
    }

    public double getEffectivePrice() {
        double base = (selectedVariant != null) ? selectedVariant.price : rate;
        double addonsTotal = 0;
        if (selectedAddons != null) {
            for (Addon a : selectedAddons) addonsTotal += a.price;
        }
        return base + addonsTotal;
    }

    public double getLineTotal() {
        return getEffectivePrice() * qty;
    }

    public void regenerateUniqueId() {
        StringBuilder sb = new StringBuilder(itemCode != null ? itemCode : "");
        if (selectedVariant != null) sb.append("_v_").append(selectedVariant.id);
        if (selectedAddons != null) {
            for (Addon a : selectedAddons) sb.append("_a_").append(a.id);
        }
        this.uniqueId = sb + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
