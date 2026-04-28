package Database;

import Items.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class VehicleDAO extends ItemDAO {
    private static final VehicleDAO instance = new VehicleDAO();

    private VehicleDAO() {}

    public static VehicleDAO getInstance() {
        return instance;
    }

    public int createItem(Connection connection, Vehicle vehicle) throws  SQLException {
        String query = "INSERT INTO vehicles(itemId, mileage, mFG, brand, model, trim, titleStatus) VALUES (?, ?, ?, ?, ?, ?, ?);";

        try {
            int generatedId = insertBaseItem(connection, vehicle, "VEHICLE", vehicle.getImagePath());

            try (PreparedStatement pr = connection.prepareStatement(query)) {
                pr.setInt(1, generatedId);
                pr.setDouble(2, vehicle.getMileage());
                pr.setInt(3, vehicle.getmFG());
                pr.setString(4, vehicle.getBrand());
                pr.setString(5, vehicle.getModel());
                pr.setString(6, vehicle.getTrim());
                pr.setString(7, vehicle.getTitleStatus());

                pr.executeUpdate();
            }

            System.out.println("Manager: Đã đăng bán thành công xe: ");
            return generatedId;

        } catch (SQLException e) {
            connection.rollback(); 
            System.out.println(e.getMessage());
            throw new SQLException("thêm vehicle không thành công"); 
        }
    }

    public Items.Vehicle findById(int id) throws SQLException {
        HashMap<String, Object> base = fetchBaseItemFields(id);
        if (base == null) return null;

        int bid = (Integer) base.get("id");
        String title = (String) base.get("title");
        int sellerId = (Integer) base.get("sellerId");
        String description = (String) base.get("description");
        ItemType type = ItemType.valueOf((String) base.get("type"));
        String imagePath = (String) base.get("imagePath");

        String sql = "SELECT mileage, mFG, brand, model, trim, titleStatus FROM vehicles WHERE itemId = ?";
        try (Connection connection = getInstance().getConnect(); PreparedStatement pr = connection.prepareStatement(sql)) {
            pr.setInt(1, id);
            try (ResultSet rs = pr.executeQuery()) {
                if (rs.next()) {
                    Items.Vehicle v = new Items.Vehicle(title, description, type, sellerId,bid, imagePath);
                    v.setVehicle(rs.getDouble("mileage"), rs.getInt("mFG"), rs.getString("brand"), rs.getString("model"), rs.getString("trim"), rs.getString("titleStatus"));
                    return v;
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Không thể truy vấn vehicles: " + e.getMessage(), e);
        }
    }
}