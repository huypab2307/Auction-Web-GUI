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

    public void createItem(Vehicle vehicle) {
        String query = "INSERT INTO vehicles(itemId, mileage, mFG, brand, model, trim, titleStatus) VALUES (?, ?, ?, ?, ?, ?, ?);";

        try (Connection connection = getConnect()) {
            connection.setAutoCommit(false);
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

                connection.commit();
                System.out.println("Manager: Đã đăng bán thành công xe: " + vehicle.getBrand() + " " + vehicle.getModel());

            } catch (SQLException ex) {
                connection.rollback(); 
                throw ex; 
            }
        } catch (SQLException e) {
            System.err.println("Lỗi VehicleDAO: " + e.getMessage());
            System.out.println("Thêm xe không thành công!");
        }
    }
}