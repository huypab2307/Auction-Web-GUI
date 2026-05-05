package com.mikey.auction.database;

import com.mikey.auction.items.Arts;
import com.mikey.auction.items.ItemType;

import java.sql.*;
import java.util.HashMap;


public class ArtsDAO extends ItemDAO {
    private static final ArtsDAO instance = new ArtsDAO();
    private ArtsDAO() {}
    public static ArtsDAO getInstance() { return instance; }

    public int createItem(Connection connection, Arts art) throws SQLException {
        String sqlArts = "INSERT INTO arts(itemId, artist, yearOfcreation, dimensions, medium) VALUES(?, ?, ?, ?, ?)";
        try {
            int generatedId = insertBaseItem(connection, art, "ARTS", art.getImagePath());

            try (PreparedStatement pr = connection.prepareStatement(sqlArts)) {
                pr.setInt(1, generatedId);
                pr.setString(2, art.getArtist());
                pr.setInt(3, art.getYearOfcreation());
                pr.setString(4, art.getDimensions());
                pr.setString(5, art.getMedium());

                pr.executeUpdate();
            }
            return generatedId;

        } catch (SQLException e) {
            throw new SQLException("không thể thêm arts vào");
        }
    }
    public Arts findById(int id) throws SQLException {
        HashMap<String, Object> base = fetchBaseItemFields(id);
        if (base == null) return null;

        int bid = (Integer) base.get("id");
        String title = (String) base.get("title");
        int sellerId = (Integer) base.get("sellerId");
        String description = (String) base.get("description");
        ItemType type = ItemType.valueOf((String) base.get("type"));
        String imagePath = (String) base.get("imagePath");

        String sql = "SELECT artist, yearOfcreation, dimensions, medium FROM arts WHERE itemId = ?";
        try (Connection connection = instance.getConnect(); PreparedStatement pr = connection.prepareStatement(sql)) {
            pr.setInt(1, id);
            try (ResultSet rs = pr.executeQuery()) {
                if (rs.next()) {
                    Arts art = new Arts(title, description, type, sellerId,bid, imagePath);
                    art.setArts(rs.getString("artist"), rs.getInt("yearOfcreation"), rs.getString("dimensions"), rs.getString("medium"));
                    return art;
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Không thể truy vấn arts: " + e.getMessage(), e);
        }
    }
}