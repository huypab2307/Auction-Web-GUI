package Auction;

import Database.NotificationDAO;
import User.Bidder;
import User.User;
import java.sql.*;
import java.util.ArrayList;

public class NotificationManager {
    private static final NotificationManager instance = new NotificationManager();

    public static NotificationManager getInstance(){
        return instance;
    }
    public void readNotification(User user){
        NotificationDAO notification = NotificationDAO.getInstance();
        try(Connection connection = notification.getConnect()){
            connection.setAutoCommit(false);
            ArrayList<Notifications> notifications = notification.checkNotifications(connection, user.getId(), false);
            notifications.forEach(s -> s.readNotification());
            notification.markAllAsRead(connection, user.getId());
            connection.commit();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }
    public Notifications subscribeAuction(Connection connection, Auction auction, Bidder bidder) throws SQLException {
        NotificationDAO notificationDAO = NotificationDAO.getInstance();
        Notifications notification = notificationDAO.subscribeAuction(connection, auction, bidder);
        ArrayList<Integer> log = notificationDAO.findNotificationList(connection, auction.getId());
        notificationDAO.notiAll(connection, notification, log);
        return notification;
    }
}