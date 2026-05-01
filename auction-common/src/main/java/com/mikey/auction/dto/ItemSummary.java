package com.mikey.auction.dto;


import com.mikey.auction.items.ItemType;

public class ItemSummary {
    private int itemId;
    private String title;
    private String description;
    private String imagePath;
    private ItemType itemType;

    public ItemSummary(int itemId, String title, String description, ItemType itemType, String imagePath) {
        this.itemId = itemId;
        this.title = title;
        this.description = description;
        this.itemType = itemType;
        this.imagePath = imagePath;
    }

    // Getter
    public int getItemId() {
        return itemId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public ItemType getItemType() {
        return itemType;
    }
}
