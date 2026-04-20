package Auction;
import java.time.*;

public class BidTransaction {
    private int id;
    private int userId;
    private int auctionId;
    private double bidAmount;
    private LocalDateTime createdAt; 

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public int getAuctionId() {
        return auctionId;
    }

    public double getBidAmount() {
        return bidAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public BidTransaction(int id, int userId, int auctionId, double bidAmount, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.auctionId = auctionId;
        this.bidAmount = bidAmount;
        this.createdAt = createdAt;
    }

}