package Database;

import Auction.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

import User.*;

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
    public Auction findById(Connection connection, int id) throws SQLException{
        String query = "SELECT * FROM auctions WHERE id = ?;";
        try(PreparedStatement pr = connection.prepareStatement(query)){
            pr.setInt(1, id);
            ResultSet rs = pr.executeQuery();
            if (rs.next()) {
                int itemId = rs.getInt("itemId");
                double price = rs.getDouble("startingPrice");
                double stepPrice = rs.getDouble("priceStep");
                double curPrice = rs.getDouble("curPrice");
                int lastBidder = rs.getInt("lastBidderId");

                LocalDateTime startTime = rs.getObject("startTime", LocalDateTime.class);
                LocalDateTime endTime = rs.getObject("endTime", LocalDateTime.class);
                String statusStr = rs.getString("status");
                AuctionStatus status = AuctionStatus.valueOf(statusStr.toUpperCase());

                return new Auction(id, itemId, startTime, endTime, price, stepPrice, curPrice, lastBidder, status);
            }
            throw new SQLException("Không tìm thấy id");
        }
    }
    public void updateAuction(Connection connection ,Auction auction, int bidderId) throws SQLException{
        String query = "UPDATE auctions SET curPrice = ?, lastBidderId = ? WHERE id = ?;";
        try(PreparedStatement pr = connection.prepareStatement(query)){
            pr.setDouble(1, auction.getCurPrice() + auction.getStepPrice());
            pr.setInt(2, bidderId);
            pr.setInt(3,auction.getId());
            if (pr.executeUpdate() > 0){
                System.out.println("đấu giá thành công");
                return;
            }
            throw new SQLException("đấu giá không thành công");
        }
    }
    public void updateTransaction(Connection connection, Auction auction, int bidderId) throws SQLException{
        String query = "INSERT INTO bidTransactions(userId,auctionId,bidAmount) VALUES (?,?,?);";
        try(PreparedStatement pr = connection.prepareStatement(query)){
            pr.setInt(1,bidderId);
            pr.setInt(2, auction.getId());
            pr.setDouble(3, auction.getCurPrice() + auction.getStepPrice());

            if(pr.executeUpdate() > 0){
                System.out.println("Thêm thành công");
                return;
            }
            throw new SQLException("thêm không thành công");
        }
    }
}