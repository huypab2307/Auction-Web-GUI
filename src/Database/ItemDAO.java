package Database;
import Items.*;
import java.sql.*;



public abstract class ItemDAO extends BaseDAO {
    protected int insertBaseItem(Connection conn, Item item, String type) throws SQLException {
        String sql = "INSERT INTO items(title, sellerId, description, type) VALUES(?, ?, ?, ?)";
        
        try (PreparedStatement pr = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pr.setString(1, item.getName());
            pr.setInt(2, item.getSellerId());
            pr.setString(3, item.getDescription());
            pr.setString(4, type); 

            pr.executeUpdate();
            
            ResultSet rs = pr.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); 
            }
        }
        throw new SQLException("Không lấy được ID từ bảng items.");
    }

    public String[] getItemDisplayInfo(int itemId) {
        String title = "Sản phẩm không xác định";
        StringBuilder details = new StringBuilder();
        
        // Giả sử khóa chính của bảng items là id
        String sql = "SELECT * FROM items WHERE id = ?"; 
        
        try (Connection conn = this.getConnect();
             PreparedStatement pr = conn.prepareStatement(sql)) {
             
            pr.setInt(1, itemId);
            ResultSet rs = pr.executeQuery();
            
            if (rs.next()) {
                title = rs.getString("title");
                String type = rs.getString("type");
                
                // Lấy thông tin chi tiết tùy theo loại
                if ("ARTS".equals(type)) {
                    String sqlArt = "SELECT * FROM arts WHERE itemId = ?";
                    try (PreparedStatement p2 = conn.prepareStatement(sqlArt)) {
                        p2.setInt(1, itemId);
                        ResultSet r2 = p2.executeQuery();
                        if (r2.next()) {
                            details.append(String.format("Nghệ sĩ: %s | Năm: %d | Kích thước: %s | Chất liệu: %s",
                                    r2.getString("artist"), r2.getInt("yearOfcreation"),
                                    r2.getString("dimensions"), r2.getString("medium")));
                        }
                    }
                } else if ("ELECTRONICS".equals(type)) {
                    String sqlElec = "SELECT * FROM electronics WHERE itemId = ?";
                    try (PreparedStatement p2 = conn.prepareStatement(sqlElec)) {
                        p2.setInt(1, itemId);
                        ResultSet r2 = p2.executeQuery();
                        if (r2.next()) {
                            details.append(String.format("Hãng: %s | Tình trạng: %s | Trọng lượng: %.1fkg",
                                    r2.getString("brand"), r2.getString("status"), r2.getDouble("weight")));
                        }
                    }
                } else if ("VEHICLE".equals(type)) {
                    String sqlVeh = "SELECT * FROM vehicles WHERE itemId = ?";
                    try (PreparedStatement p2 = conn.prepareStatement(sqlVeh)) {
                        p2.setInt(1, itemId);
                        ResultSet r2 = p2.executeQuery();
                        if (r2.next()) {
                            details.append(String.format("Hãng: %s | Model: %s | Số km: %.1f | Tình trạng: %s",
                                    r2.getString("brand"), r2.getString("model"),
                                    r2.getDouble("mileage"), r2.getString("titleStatus")));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Lỗi khi lấy chi tiết sản phẩm: " + e.getMessage());
        }
        
        // Trả về mảng 2 phần tử: [0] là Tiêu đề, [1] là Chi tiết
        return new String[]{title, details.toString()};
    }
}


