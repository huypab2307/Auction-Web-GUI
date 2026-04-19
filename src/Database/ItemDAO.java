package Database;
import java.sql.*;

import Items.*;



public abstract class ItemDAO extends BaseDAO {
    protected int insertBaseItem(Connection conn, Item item, String type) throws SQLException {
        String sql = "INSERT INTO items(title, sellerId, price, description, type) VALUES(?, ?, ?, ?, ?)";
        
        try (PreparedStatement pr = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pr.setString(1, item.getName());
            pr.setInt(2, item.getSellerId());
            pr.setDouble(3, item.getPrice());
            pr.setString(4, item.getDescription());
            pr.setString(5, type); // 'ARTS', 'ELECTRONICS', v.v.

            pr.executeUpdate();
            
            ResultSet rs = pr.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); // Trả về ID vừa tạo
            }
        }
        throw new SQLException("Không lấy được ID từ bảng items.");
    }
}


