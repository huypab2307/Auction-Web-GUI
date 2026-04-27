package Auction;

import java.time.LocalDateTime;

import Items.ItemType;

public class AuctionInfo {
    private int id;
    private String itemTitle;  
    private String imagePath;
    private String sellerUsername;
    private String lastBidderName; 
    private ItemType type;
    private double curPrice;
    private AuctionStatus status;
    private LocalDateTime endTime;

    public int getId() {
        return id;
    }
    public String imagePath(){
        return imagePath;
    }
    public ItemType getItemType(){
        return type;
    }
    public String getItemTitle() {
        return itemTitle;
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
    public AuctionInfo(int id, String itemTitle, ItemType type,String imagePath, String sellerUsername, String lastBidderName, double curPrice,
            AuctionStatus status, LocalDateTime endTime) {
        this.id = id;
        this.itemTitle = itemTitle;
        this.type = type;
        this.imagePath = imagePath;
        this.sellerUsername = sellerUsername;
        this.lastBidderName = lastBidderName;
        this.curPrice = curPrice;
        this.status = status;
        this.endTime = endTime;
    }
}
