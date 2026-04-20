package Auction;

import java.time.*;

public class Auction{
    private int id;
    private int itemId;
    private int sellerId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double price;
    private double stepPrice;
    private double curPrice;
    private int LastBidder;
    private AuctionStatus status;

    public int getId() {
        return id;
    }

    public int getItemId() {
        return itemId;
    }
    public int getSellerId(){
        return sellerId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public double getPrice() {
        return price;
    }

    public double getStepPrice() {
        return stepPrice;
    }

    public double getCurPrice() {
        return curPrice;
    }

    public int getLastBidder() {
        return LastBidder;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public Auction(int id,int itemId,int sellerId, LocalDateTime startTime,LocalDateTime endTime, double price, double stepPrice, double curPrice,  int LastBidder,AuctionStatus status) {
        this.LastBidder = LastBidder;
        this.curPrice = curPrice;
        this.sellerId = sellerId;
        this.endTime = endTime;
        this.id = id;
        this.itemId = itemId;
        this.price = price;
        this.startTime = startTime;
        this.status = status;
        this.stepPrice = stepPrice;
    }
}