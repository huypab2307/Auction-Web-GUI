package com.mikey.auction.database;

import com.mikey.auction.items.Electronics;
import com.mikey.auction.items.ItemType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class ElectronicsDAO extends ItemDAO {
    private static final ElectronicsDAO electronicsDAO = new ElectronicsDAO();
    private ElectronicsDAO(){}
    public static ElectronicsDAO getInstance(){
        return electronicsDAO;
    }
    public int createItem(Connection connection, Electronics electronic) throws SQLException{
        String query = "INSERT INTO electronics(itemId,brand,power,voltage,current,status,color,weight) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
        int generatedId = insertBaseItem(connection, electronic, "ELECTRONICS", electronic.getImagePath());
        try{
            try (PreparedStatement pr = connection.prepareStatement(query)){
                pr.setInt(1,generatedId);
                pr.setString(2,electronic.getBrand());
                pr.setInt(3,electronic.getPower());
                pr.setDouble(4,electronic.getVoltage());
                pr.setDouble(5,electronic.getCurrent());
                pr.setString(6,electronic.getStatus());
                pr.setString(7,electronic.getColor());
                pr.setDouble(8,electronic.getWeight());

                pr.executeUpdate();
            }
            System.out.println("THêm sản phẩm thành công");
            return generatedId;
        }catch(SQLException e){
            connection.rollback();
            throw e;
        }
    }

    public Electronics findById(int id) throws SQLException {
        HashMap<String, Object> base = fetchBaseItemFields(id);
        if (base == null) return null;

        int bid = (Integer) base.get("id");
        String title = (String) base.get("title");
        int sellerId = (Integer) base.get("sellerId");
        String description = (String) base.get("description");
        ItemType type = ItemType.valueOf((String) base.get("type"));
        String imagePath = (String) base.get("imagePath");

        String sql = "SELECT brand,power,voltage,current,status,color,weight FROM electronics WHERE itemId = ?";
        try (Connection connection = getInstance().getConnect(); PreparedStatement pr = connection.prepareStatement(sql)) {
            pr.setInt(1, id);
            try (ResultSet rs = pr.executeQuery()) {
                if (rs.next()) {
                    Electronics e = new Electronics(title, description, type, sellerId,bid, imagePath);
                    e.setElectronics(rs.getString("brand"), rs.getInt("power"), rs.getDouble("voltage"), rs.getDouble("current"), rs.getString("status"), rs.getString("color"), rs.getDouble("weight"));
                    return e;
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Không thể truy vấn electronics: " + e.getMessage(), e);
        }
    }
}
