package Database;

import Items.Arts;
import java.sql.*;


public class ArtsDAO extends ItemDAO {
    private static final ArtsDAO instance = new ArtsDAO();
    private ArtsDAO() {}
    public static ArtsDAO getInstance() { return instance; }

    public void createItem(Arts art) {
        String sqlArts = "INSERT INTO arts(itemId, artist, yearOfcreation, dimensions, medium) VALUES(?, ?, ?, ?, ?)";

        try (Connection connection = getConnect()) {
            connection.setAutoCommit(false);

            try {
                int generatedId = insertBaseItem(connection, art, "ARTS");

                try (PreparedStatement pr = connection.prepareStatement(sqlArts)) {
                    pr.setInt(1, generatedId);
                    pr.setString(2, art.getArtist());
                    pr.setInt(3, art.getYearOfcreation());
                    pr.setString(4, art.getDimensions());
                    pr.setString(5, art.getMedium());

                    pr.executeUpdate();
                }

                connection.commit(); 
                System.out.println("Thêm sản phẩm Arts thành công!");

            } catch (SQLException e) {
                connection.rollback(); 
                throw e;
            }
        } catch (SQLException ex) {
            System.out.println("Lỗi tạo sản phẩm: " + ex.getMessage());
        }
    }
}