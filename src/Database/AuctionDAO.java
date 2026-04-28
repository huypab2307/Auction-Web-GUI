package Database;

import Auction.*;
import DTO.AuctionInfo;
import DTO.ItemSummary;
import Items.ItemType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class AuctionDAO extends BaseDAO {
    private static final AuctionDAO instance = new AuctionDAO();
    private AuctionDAO() {}
    public static AuctionDAO getInstance() { return instance; }

    private static final String BASE_SELECT_QUERY =
            "SELECT a.id, i.id AS itemId, i.title AS itemName, i.description AS description, " +
                    "i.type AS type, i.imagePath, u_seller.username AS sellerName, " +
                    "u_bidder.username AS bidderName, a.curPrice, a.priceStep, a.status, " +
                    "a.startTime, a.endTime " +
                    "FROM auctions a " +
                    "INNER JOIN items i ON a.itemId = i.id " +
                    "INNER JOIN user u_seller ON a.sellerId = u_seller.id " +
                    "LEFT JOIN user u_bidder ON a.lastBidderId = u_bidder.id ";

    private AuctionInfo mapToAuctionInfo(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int itemId = rs.getInt("itemId");
        String itemName = rs.getString("itemName");
        String description = rs.getString("description");
        String imagePath = rs.getString("imagePath");
        String sellerName = rs.getString("sellerName");
        String lastBidder = rs.getString("bidderName");
        double curPrice = rs.getDouble("curPrice");
        double priceStep = rs.getDouble("priceStep");

        LocalDateTime startTime = rs.getObject("startTime", LocalDateTime.class);
        LocalDateTime endTime = rs.getObject("endTime", LocalDateTime.class);

        AuctionStatus status = AuctionStatus.valueOf(rs.getString("status").toUpperCase());
        ItemType itemType = ItemType.valueOf(rs.getString("type").toUpperCase());

        ItemSummary itemInfo = new ItemSummary(itemId, itemName, description, itemType, imagePath);
        return new AuctionInfo(itemInfo, id, sellerName, lastBidder, curPrice, status, startTime, endTime, priceStep);
    }

    private ArrayList<AuctionInfo> executeQueryAndGetList(String sql, Object... params) {
        ArrayList<AuctionInfo> list = new ArrayList<>();
        try (Connection connection = this.getConnect();
             PreparedStatement pr = connection.prepareStatement(sql)) {

            // Tự động nạp tham số vào dấu "?" (nếu có)
            for (int i = 0; i < params.length; i++) {
                pr.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = pr.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToAuctionInfo(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi Database: " + e.getMessage());
        }
        return list;
    }


    public ArrayList<AuctionInfo> getAllAuctions() {
        return executeQueryAndGetList(BASE_SELECT_QUERY);
    }

    public ArrayList<AuctionInfo> getAuctionsType(ItemType type) {
        String sql = BASE_SELECT_QUERY + " WHERE i.type = ?";
        return executeQueryAndGetList(sql, type.name());
    }

    public ArrayList<AuctionInfo> searchAuction(String text) {
        String sql = BASE_SELECT_QUERY + " WHERE UPPER(i.title) LIKE ?";
        return executeQueryAndGetList(sql, text.toUpperCase() + "%");
    }

    public AuctionInfo searchAuctionById(int auctionId) {
        String sql = BASE_SELECT_QUERY + " WHERE a.id = ?";
        ArrayList<AuctionInfo> results = executeQueryAndGetList(sql, auctionId);
        return results.isEmpty() ? null : results.get(0);
    }


    public boolean createAuction(Connection connection, int itemId, int sellerId, double price, double stepPrice, int durations) throws SQLException {
        LocalDateTime endDateTime = LocalDateTime.now().plusDays(durations);
        String query = "INSERT INTO auctions(itemId, sellerId, startingPrice, priceStep, curPrice, endTime) VALUES(?,?,?,?,?,?);";

        try (PreparedStatement pr = connection.prepareStatement(query)) {
            pr.setInt(1, itemId);
            pr.setInt(2, sellerId);
            pr.setDouble(3, price);
            pr.setDouble(4, stepPrice);
            pr.setDouble(5, price);
            pr.setTimestamp(6, Timestamp.valueOf(endDateTime));
            return pr.executeUpdate() > 0;
        }
    }

    public void updateAuction(Connection connection, Auction auction, int bidderId, double curPrice) throws SQLException {
        String query = "UPDATE auctions SET curPrice = ?, lastBidderId = ? WHERE id = ? AND curPrice = ?;";
        try (PreparedStatement pr = connection.prepareStatement(query)) {
            pr.setDouble(1, auction.getCurPrice() + auction.getStepPrice());
            pr.setInt(2, bidderId);
            pr.setInt(3, auction.getId());
            pr.setDouble(4, curPrice);
            if (pr.executeUpdate() == 0) throw new SQLException("Cập nhật đấu giá thất bại (Có thể do giá đã thay đổi).");
        }
    }

    public void updateTransaction(Connection connection, Auction auction, int bidderId) throws SQLException {
        String query = "INSERT INTO bidTransactions(userId, auctionId, bidAmount) VALUES (?,?,?);";
        try (PreparedStatement pr = connection.prepareStatement(query)) {
            pr.setInt(1, bidderId);
            pr.setInt(2, auction.getId());
            pr.setDouble(3, auction.getCurPrice() + auction.getStepPrice());
            pr.executeUpdate();
        }
    }
    public Auction findById(Connection connection, int id) throws SQLException {
        String query = "SELECT * FROM auctions WHERE id = ?;";
        try (PreparedStatement pr = connection.prepareStatement(query)) {
            pr.setInt(1, id);
            try (ResultSet rs = pr.executeQuery()) {
                if (rs.next()) {
                    return new Auction(
                            rs.getInt("id"),
                            rs.getInt("itemId"),
                            rs.getInt("sellerId"),
                            rs.getObject("startTime", LocalDateTime.class),
                            rs.getObject("endTime", LocalDateTime.class),
                            rs.getDouble("startingPrice"),
                            rs.getDouble("priceStep"),
                            rs.getDouble("curPrice"),
                            rs.getInt("lastBidderId"),
                            AuctionStatus.valueOf(rs.getString("status").toUpperCase())
                    );
                }
            }
        }
        return null;
    }
}