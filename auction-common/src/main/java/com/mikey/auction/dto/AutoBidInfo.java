package com.mikey.auction.dto;

import java.io.Serializable;

public class AutoBidInfo implements Serializable {
    private int userId;
    private int auctionId;
    private double maxAmount;

    public AutoBidInfo() {}

    public AutoBidInfo(int userId, int auctionId, double maxAmount) {
        this.userId = userId;
        this.auctionId = auctionId;
        this.maxAmount = maxAmount;
    }

    public int getUserId() { 
        return userId; 
    }
    public int getAuctionId() { 
        return auctionId; 
    }

    public double getMaxAmount() { 
        return maxAmount; 
    }
}