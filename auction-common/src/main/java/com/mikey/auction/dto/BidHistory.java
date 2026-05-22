package com.mikey.auction.dto;

import java.time.LocalDateTime;

public class BidHistory {
    private int id;
    private int auctionId;
    private String bidderUsername;
    private double bidAmount;
    private LocalDateTime createdAt;

    public BidHistory(int id, int auctionId, String bidderUsername, double bidAmount, LocalDateTime createdAt) {
        this.id = id;
        this.auctionId = auctionId;
        this.bidderUsername = bidderUsername;
        this.bidAmount = bidAmount;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(int auctionId) {
        this.auctionId = auctionId;
    }

    public String getBidderUsername() {
        return bidderUsername;
    }

    public void setBidderUsername(String bidderUsername) {
        this.bidderUsername = bidderUsername;
    }

    public double getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(double bidAmount) {
        this.bidAmount = bidAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}