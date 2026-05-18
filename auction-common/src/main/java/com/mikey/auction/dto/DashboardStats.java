package com.mikey.auction.dto;

import java.io.Serializable;

public class DashboardStats implements Serializable {
    private double totalSpent;
    private int activeBids;
    private int wonItems;
    private double winRate;
    private int outbidCount;
    private int followingCount;
    private int soldItems;

    // Constructor trống cho GSON
    public DashboardStats() {}

    // Getters và Setters
    public double getTotalSpent() { return totalSpent; }
    public void setTotalSpent(double totalSpent) { this.totalSpent = totalSpent; }

    public int getActiveBids() { return activeBids; }
    public void setActiveBids(int activeBids) { this.activeBids = activeBids; }

    public int getWonItems() { return wonItems; }
    public void setWonItems(int wonItems) { this.wonItems = wonItems; }

    public double getWinRate() { return winRate; }
    public void setWinRate(double winRate) { this.winRate = winRate; }

    public int getOutbidCount() { return outbidCount; }
    public void setOutbidCount(int outbidCount) { this.outbidCount = outbidCount; }

    public int getFollowingCount() { return followingCount; }
    public void setFollowingCount(int followingCount) { this.followingCount = followingCount; }

    public int getSoldItems() { return soldItems; }
    public void setSoldItems(int soldItems) { this.soldItems = soldItems; }
}