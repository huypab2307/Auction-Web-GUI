package Database;

import Auction.*;
import java.sql.*;
import java.util.*;

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
        String query = "SELECT DISTINCT receiverId FROM messages WHERE senderId = ? ORDER BY createdAt; ";
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
}
