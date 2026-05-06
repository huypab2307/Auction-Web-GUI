package com.mikey.auction.manager;


import com.mikey.auction.auction.Auction;
import com.mikey.auction.auction.Message;
import com.mikey.auction.auction.Notifications;
import com.mikey.auction.database.MessageDAO;
import com.mikey.auction.database.NotificationDAO;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.user.Bidder;
import com.mikey.auction.user.User;
import com.mysql.cj.jdbc.exceptions.SQLError;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationManager {
    private static final NotificationManager instance = new NotificationManager();

    public static NotificationManager getInstance(){
        return instance;
    }
    public void readNotification(User user){
        NotificationDAO notification = NotificationDAO.getInstance();
        try(Connection connection = notification.getConnect()){
            connection.setAutoCommit(false);
            ArrayList<Notifications> notifications = notification.checkNotifications(connection, user.getId());
            notifications.forEach(s -> s.readNotification());
            notification.markAllAsRead(connection, user.getId());
            connection.commit();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public Boolean subscribeAuction(Connection connection, int auctionId,int userId) throws SQLException {
        NotificationDAO notificationDAO = NotificationDAO.getInstance();
        return notificationDAO.subscribeAuction(connection, auctionId, userId);
    }


    public List<Notifications> findNotififications(int userId){
        NotificationDAO notificationDAO = NotificationDAO.getInstance();
        try(Connection connection = notificationDAO.getConnect()) {
            connection.setAutoCommit(false);
            ArrayList<Notifications> list = notificationDAO.checkNotifications(connection, userId);
            connection.commit();
            return list;
            
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    public boolean notiAll(AuctionInfo auction, Bidder bidder){
        NotificationDAO notificationDAO = NotificationDAO.getInstance();
        try(Connection connection = notificationDAO.getConnect()){
            Notifications notifications = notificationDAO.getNotification(connection, auction, bidder);
            ArrayList<Integer> list = notificationDAO.findNotificationList(connection, auction.getId());
            return notificationDAO.notiAll(connection, notifications, list);
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }
}