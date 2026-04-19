package Database;

import java.sql.*;

import Items.Arts;


public class ArtsDAO extends ItemDAO {
    private static final ArtsDAO instance = new ArtsDAO();
    private ArtsDAO() {}
    public static ArtsDAO getItemDAO() { return instance; }

    public void createItem(Arts art) {
        String sqlArts = "INSERT INTO arts(itemId, artist, yearOfcreation, dimensions, medium) VALUES(?, ?, ?, ?, ?)";

        try (Connection connection = getConnect()) {
            connection.setAutoCommit(false);

            try {
                int generatedId = insertBaseItem(connection, art, "ARTS");

                try (PreparedStatement pr2 = connection.prepareStatement(sqlArts)) {
                    pr2.setInt(1, generatedId);
                    pr2.setString(2, art.getArtist());
                    pr2.setInt(3, art.getYearOfcreation());
                    pr2.setString(4, art.getDimensions());
                    pr2.setString(5, art.getMedium());

                    pr2.executeUpdate();
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