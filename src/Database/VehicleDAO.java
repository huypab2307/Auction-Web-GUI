package Database;

import Items.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class VehicleDAO extends ItemDAO {
    private static final VehicleDAO instance = new VehicleDAO();

    private VehicleDAO() {}

    public static VehicleDAO getInstance() {
        return instance;
    }

    public int createItem(Connection connection, Vehicle vehicle) throws  SQLException {
        String query = "INSERT INTO vehicles(itemId, mileage, mFG, brand, model, trim, titleStatus) VALUES (?, ?, ?, ?, ?, ?, ?);";

        try {
            int generatedId = insertBaseItem(connection, vehicle, "VEHICLE");

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
}