package com.mikey.auction.database;

import com.mikey.auction.database.BaseDAO;
import com.mikey.auction.items.Item;

import java.sql.*;
import java.util.HashMap;



public abstract class ItemDAO extends BaseDAO {
    protected int insertBaseItem(Connection conn, Item item, String type, String imagePath) throws SQLException {
        String sql = "INSERT INTO items(title, sellerId, description, type, imagePath) VALUES(?, ?, ?, ?, ?)";
        
        try (PreparedStatement pr = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pr.setString(1, item.getName());
            pr.setInt(2, item.getSellerId());
            pr.setString(3, item.getDescription());
            pr.setString(4, type); 
            pr.setString(5, imagePath);

            pr.executeUpdate();
            
            ResultSet rs = pr.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); 
            }
        }
        throw new SQLException("Không lấy được ID từ bảng items.");
    }

    protected HashMap<String, Object> fetchBaseItemFields(int id) throws SQLException {
        String sql = "SELECT id, title, sellerId, description, type, imagePath FROM items WHERE id = ?";
        try (Connection conn = getConnect(); PreparedStatement pr = conn.prepareStatement(sql)) {
            pr.setInt(1, id);
            try (ResultSet rs = pr.executeQuery()) {
                if (rs.next()) {
                    HashMap<String, Object> fields = new HashMap<>();
                    fields.put("id", rs.getInt("id"));
                    fields.put("title", rs.getString("title"));
                    fields.put("sellerId", rs.getInt("sellerId"));
                    fields.put("description", rs.getString("description"));
                    fields.put("type", rs.getString("type"));
                    fields.put("imagePath", rs.getString("imagePath"));
                    return fields;
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Không thể truy vấn items: " + e.getMessage(), e);
        }
    }
}
