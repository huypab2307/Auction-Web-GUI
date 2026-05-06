package com.mikey.auction.auction;

import java.time.LocalDateTime;

public class Notifications {
    private int id;
    private int userId;
    private int auctionId;
    private boolean isRead;
    private String message;
    private LocalDateTime createdAt;

    public Notifications(int auctionId,int id, int userId, String message, LocalDateTime createdAt, boolean read ) {
        this.auctionId = auctionId;
        this.createdAt = createdAt;
        this.id = id;
        this.message = message;
        this.userId = userId;
        this.isRead = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public int getId() {
        return id;
    }
    public boolean isRead() {
        return isRead;
    }

    public int getAuctionId() {
        return auctionId;
    }

    public String getMessage() {
        return message;
    }

    public int getUserId() {
        return userId;
    }
    public void readNotification() {
        System.out.println("Notification:");
        // System.out.println("   • User ID: " + userId);
        // System.out.println("   • Auction ID: " + auctionId);
        System.out.println("   • Message: " + message);
        System.out.println("   • Created At: " + createdAt);
    }
}
