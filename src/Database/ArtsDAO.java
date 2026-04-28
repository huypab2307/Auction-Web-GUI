package Database;

import Items.Arts;
import java.sql.*;


public class ArtsDAO extends ItemDAO {
    private static final ArtsDAO instance = new ArtsDAO();
    private ArtsDAO() {}
    public static ArtsDAO getInstance() { return instance; }

    public int createItem(Connection connection, Arts art) throws SQLException {
        String sqlArts = "INSERT INTO arts(itemId, artist, yearOfcreation, dimensions, medium) VALUES(?, ?, ?, ?, ?)";
        connection.setAutoCommit(false);
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
}