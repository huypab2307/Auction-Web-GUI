package com.mikey.auction.items;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

public abstract class Item {
    private String name;
    private String description;
    private int sellerId;
    private int id;
    private ItemType type;
    private String imagePath;

    public Item(String name, String description,ItemType type, int sellerId,int id, String imagePath) {
        this.imagePath = imagePath;
        this.name = name;
        this.description = description;
        this.sellerId = sellerId;
        this.type = type;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getSellerId() {
        return sellerId;
    }

    public int getId() {
        return id;
    }
    public ItemType getType(){
        return type;
    }
    public String getImagePath(){
        return imagePath;
    }

    public abstract Map<String,String> getSpecificInfo();

}
