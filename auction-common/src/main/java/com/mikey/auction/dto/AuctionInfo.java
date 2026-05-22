package com.mikey.auction.dto;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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
    private String itemName;   // Để chứa tên sản phẩm
    private String sellerName; // Để chứa tên người bán

    public String getItemName() {
        return itemName;
    }
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
    public String getSellerName() {
        return sellerName;
    }
    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setSellerUsername(String sellerUsername) {
        this.sellerUsername = sellerUsername;
    }
    public void setLastBidderName(String lastBidderName) {
        this.lastBidderName = lastBidderName;
    }
    public void setCurPrice(double curPrice) {
        this.curPrice = curPrice;
    }
    public void setBidStep(double bidStep) {
        this.bidStep = bidStep;
    }
    public void setStatus(AuctionStatus status) {
        this.status = status;
    }
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    public void setItemInfo(ItemSummary itemInfo) {
        this.itemInfo = itemInfo;
    }
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

    public AuctionInfo() {}
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

    private Map<String, String> extraData = new HashMap<>();

    public Map<String, String> getExtraData() {
        return extraData;
    }

    public void setExtraData(Map<String, String> extraData) {
        this.extraData = extraData;
    }
}
