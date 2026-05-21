package com.mikey.auction.dto;

public class AutoBidInfo {
    private int userId;
    private int auctionId;
    private double maxPrice;

    // Constructor
    public AutoBidInfo(int userId, int auctionId, double maxPrice) {
        this.userId = userId;
        this.auctionId = auctionId;
        this.maxPrice = maxPrice;
    }

    // Thêm Getter/Setter cho id và curPrice nếu AuctionManagerTest cần dùng
    public void setId(int id) { this.auctionId = id; }
    public void setCurPrice(double price) { this.maxPrice = price; }
}