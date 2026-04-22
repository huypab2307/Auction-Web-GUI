package Auction;

import Database.MessageDAO;
import Database.NotificationDAO;
import User.Bidder;
import User.User;
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
    public List<Message> messageList(int senderId, int receiverId) {
        MessageDAO messageDAO = MessageDAO.getInstance();
        try (Connection connection = messageDAO.getConnect()){
            connection.setAutoCommit(false);
            List<Message> messageList = messageDAO.loadMessage(connection, senderId, receiverId);
            return messageList;
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }
    public List<Integer> loadUserMessageList(int userId){
        MessageDAO messageDAO = MessageDAO.getInstance();
        try (Connection connection = messageDAO.getConnect()){
            connection.setAutoCommit(false);
            List<Integer> userFriendList = messageDAO.loadUser(connection, userId);
            return userFriendList;
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }
}