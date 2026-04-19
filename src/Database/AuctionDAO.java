package Database;

import java.sql.*;
import java.time.LocalDateTime;

public class AuctionDAO extends BaseDAO {
    private static final AuctionDAO instance = new AuctionDAO();
    private AuctionDAO() {}
    
    public static AuctionDAO getInstance(){return instance;}



    public void createAuction(Connection connection, int itemId, double price, double stepPrice, int durations) throws SQLException {
        LocalDateTime endDateTime = LocalDateTime.now().plusDays(durations);
        String query = "INSERT INTO auctions(itemId, startingPrice, priceStep, curPrice, endTime) VALUES(?,?,?,?,?);";
        
        try (PreparedStatement pr = connection.prepareStatement(query)) {
            pr.setInt(1, itemId);
            pr.setDouble(2, price);
            pr.setDouble(3, stepPrice);
            pr.setDouble(4, price); 
            pr.setTimestamp(5, Timestamp.valueOf(endDateTime));

            if (pr.executeUpdate() > 0) {
                System.out.println("Manager: Đã đưa sản phẩm lên sàn, kết thúc sau " + durations + " ngày.");
            } else {
                connection.rollback();
                throw new SQLException("Lỗi: Không thể tạo phiên đấu giá.");
            }
        }
    }
}