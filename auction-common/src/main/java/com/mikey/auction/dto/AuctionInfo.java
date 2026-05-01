package com.mikey.auction.dto;

import java.time.LocalDateTime;

import com.mikey.auction.auction.AuctionStatus;

//This class is made only to display info quickly, rather than do many frivolous queries of Auction, Item, and User
public class AuctionInfo {
    private int id;
    private String sellerUsername;
    private String lastBidderName;
    private double curPrice;
    private double bidStep;
    private AuctionStatus status;
    private LocalDateTime endTime;
    private LocalDateTime startTime;
    private ItemSummary itemInfo;

    public int getId() {
        return id;
    }
    public String getSellerUsername() {
        return sellerUsername;
    }
    public String getLastBidderName() {
        return lastBidderName;
    }
    public double getCurPrice() {
        return curPrice;
    }
    public AuctionStatus getStatus() {
        return status;
    }
    public LocalDateTime getEndTime() {
        return endTime;
    }
    public double getBidStep() {return bidStep;}
    public ItemSummary getItemInfo(){return itemInfo;}
    public LocalDateTime getStartTime(){ return startTime;}

    public AuctionInfo(ItemSummary itemInfo, int id, String sellerUsername, String lastBidderName, double curPrice,
            AuctionStatus status,LocalDateTime startTime, LocalDateTime endTime, double bidStep) {
        this.id = id;
        this.itemInfo = itemInfo;
        this.sellerUsername = sellerUsername;
        this.lastBidderName = lastBidderName;
        this.curPrice = curPrice;
        this.status = status;
        this.endTime = endTime;
        this.bidStep = bidStep;
        this.startTime = startTime;
    }
}
