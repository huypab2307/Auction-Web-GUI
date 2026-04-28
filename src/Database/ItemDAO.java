package Database;
import Items.*;
import java.sql.*;



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
}


