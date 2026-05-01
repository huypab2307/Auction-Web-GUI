package com.mikey.auction.database;

import java.sql.*;
import java.util.*;

import com.mikey.auction.auction.Message;
import com.mikey.auction.database.BaseDAO;

public class MessageDAO extends BaseDAO {
    private static final MessageDAO messageDAO = new MessageDAO();
    private MessageDAO(){}
    public static MessageDAO getInstance(){return messageDAO;}

    public List<Message> loadMessage(Connection connection, int userA, int userB) throws SQLException {
        String query = "SELECT * FROM messages " +
                    "WHERE (senderId = ? AND receiverId = ?) " +
                    "OR (senderId = ? AND receiverId = ?) " +
                    "ORDER BY createdAt ASC;";
        
        List<Message> list = new ArrayList<>();
        try (PreparedStatement pr = connection.prepareStatement(query)) {
            pr.setInt(1, userA);
            pr.setInt(2, userB);
            pr.setInt(3, userB); 
            pr.setInt(4, userA);
            
            ResultSet rs = pr.executeQuery();
            while (rs.next()) {
                list.add(new Message(
                    rs.getInt("id"),
                    rs.getInt("senderId"),
                    rs.getInt("receiverId"),
                    rs.getString("content"),
                    rs.getTimestamp("createdAt").toLocalDateTime()
                ));
            }
        }
        return list;
    }
    
    public List<Integer> loadUser(Connection connection, int userId) throws SQLException{
            // SỬA LẠI CÂU QUERY Ở ĐÂY
            String query = "SELECT receiverId FROM messages WHERE senderId = ? GROUP BY receiverId ORDER BY MAX(createdAt) DESC;";
            
            List<Integer> list = new ArrayList<>();
            try (PreparedStatement pr = connection.prepareStatement(query)){
                pr.setInt(1, userId);
                ResultSet rs = pr.executeQuery();
                while (rs.next()){
                    list.add(rs.getInt("receiverId"));
                }
                return list;
            }
    }
    public boolean sendMessage(Connection connection, int senderId, int receiverId, String content) throws SQLException {
        String query = "INSERT INTO messages (senderId, receiverId, content) VALUES (?, ?, ?);";
        try (PreparedStatement pr = connection.prepareStatement(query)) {
            pr.setInt(1, senderId);
            pr.setInt(2, receiverId);
            pr.setString(3, content);
            return pr.executeUpdate() > 0;
        }
    }
}
