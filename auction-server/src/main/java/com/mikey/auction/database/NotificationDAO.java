package com.mikey.auction.database;

import com.mikey.auction.auction.Auction;
import com.mikey.auction.auction.Notifications;
import com.mikey.auction.user.Bidder;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;


public class NotificationDAO extends BaseDAO {
    private static final NotificationDAO instance = new NotificationDAO();
    private NotificationDAO() {}
    public static NotificationDAO getInstance() { return instance; }

    private Notifications mapNotification(ResultSet rs) throws SQLException {
        return new Notifications(
            rs.getInt("auctionId"),
            rs.getInt("id"),
            rs.getInt("userId"),
            rs.getString("message"),
            rs.getTimestamp("createdAt").toLocalDateTime()
        );
    }
    public Notifications subscribeAuction(Connection connection, Auction auction, Bidder bidder) throws SQLException {
        String query = "INSERT INTO notification(userId, auctionId, message, isChecked) VALUES(?,?,?,?)";
        String message = "Người dùng " + bidder.getUsername() + " vừa đấu giá món hàng id " + auction.getItemId();
        
        try (PreparedStatement pr = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pr.setInt(1, bidder.getId());
            pr.setInt(2, auction.getId());
            pr.setString(3, message);
            pr.setBoolean(4, true); 

            int affectedRows = pr.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pr.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        return new Notifications(
                            auction.getId(),
                            generatedId,
                            bidder.getId(),
                            message,
                            LocalDateTime.now() 
                        );
                    }
                }
            }
            throw new SQLException("Tạo thông báo thất bại, không có dòng nào được thay đổi.");
        }
    }
    public void notiAll(Connection connection, Notifications notification, ArrayList<Integer> log) throws SQLException {
        String query = "INSERT INTO notification(userId, auctionId, message, isChecked) VALUES(?,?,?,?)";
        
        try (PreparedStatement pr = connection.prepareStatement(query)) {
            for (int userId : log) {
          
                if (userId != notification.getUserId()) {
                    pr.setInt(1, userId);
                    pr.setInt(2, notification.getAuctionId()); 
                    pr.setString(3, notification.getMessage()); 
                    pr.setBoolean(4, false); 
                    
                    pr.addBatch(); 
                }
            }
            pr.executeBatch(); 
        }
    }
    public ArrayList<Integer> findNotificationList(Connection connection,int auctionId) throws SQLException{
        String query = "SELECT DISTINCT userId FROM notification WHERE auctionId = ?";
        ArrayList<Integer> log = new ArrayList<>();
        try(PreparedStatement pr = connection.prepareStatement(query)) {
            pr.setInt(1,auctionId);
            ResultSet rs = pr.executeQuery();
            while (rs.next()){
                log.add(rs.getInt("userId"));
            }
        return log;
        }
    }
    public ArrayList<Notifications> checkNotifications(Connection connection,int userId, boolean isReaded) throws SQLException{
        String query = "SELECT * FROM notification WHERE userId = ? AND isChecked = ?";
        ArrayList<Notifications> notifications = new ArrayList<>();
        try(PreparedStatement pr = connection.prepareStatement(query)) {
            pr.setInt(1,userId);
            pr.setBoolean(2, isReaded);
            ResultSet rs = pr.executeQuery();
            while(rs.next()){
                notifications.add(mapNotification(rs));
            }
        return notifications;
        } 
    }
    public void markAllAsRead(Connection connection, int userId) throws SQLException {
        String query = "UPDATE notification SET isChecked = true WHERE userId = ? AND isChecked = false";
        
        try (PreparedStatement pr = connection.prepareStatement(query)) {
            pr.setInt(1, userId);
            int rowsAffected = pr.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("Sytem: Đã đánh dấu " + rowsAffected + " thông báo là đã đọc.");
            }
        }
    }
    public void markByIdAsRead(Connection connection, int notificationId) throws SQLException {
        String query = "UPDATE notification SET isChecked = true WHERE id = ?";
        
        try (PreparedStatement pr = connection.prepareStatement(query)) {
            pr.setInt(1, notificationId);
            pr.executeUpdate();
        }
    }
}