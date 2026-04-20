package Auction;

import java.time.LocalDateTime;

public class Notifications {
    private int id;
    private int userId;
    private int auctionId;
    private String message;
    private LocalDateTime createdAt;

    public Notifications(int auctionId,int id, int userId, String message, LocalDateTime createdAt ) {
        this.auctionId = auctionId;
        this.createdAt = createdAt;
        this.id = id;
        this.message = message;
        this.userId = userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public int getId() {
        return id;
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
