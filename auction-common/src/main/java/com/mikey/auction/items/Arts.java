package com.mikey.auction.items;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import com.mikey.auction.auction.Auction;



public class Arts extends Item {
    private String artist; //Tác giả
    private int yearOfcreation; //Năm sáng tác
    private String dimensions; //Kích thước
    private String medium; //Chất liệu 

    
    public String getArtist(){
        return this.artist;
    }
    public void setArts(String artist, int yearOfcreation, String dimensions, String medium) {
        this.artist = artist;
        this.yearOfcreation = yearOfcreation;
        this.dimensions = dimensions;
        this.medium = medium;
    }

    public Arts(String name, String description, ItemType type,int sellerId, int itemId, String imagePath) {
        super(name, description, type, sellerId, itemId, imagePath);
    }
    // YearOfCreation
    public int getYearOfcreation(){
        return this.yearOfcreation;
    }
    
    // Dimensions
    public String getDimensions(){
        return this.dimensions;
    }

    // Medium
    public String getMedium(){
        return this.medium;
    }

    @Override
    public Map<String, String> getSpecificInfo() {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("Tác giả", artist);
        info.put("Năm sáng tác", String.valueOf(yearOfcreation));
        info.put("Kích thước", dimensions);
        info.put("Chất liệu", medium);
        return info;
    }
}