package com.mikey.auction.database;


import com.mikey.auction.auction.Auction;
import com.mikey.auction.auction.AuctionStatus;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.dto.ItemSummary;
import com.mikey.auction.items.ItemType;


import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;


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
        ItemType itemType = ItemType.valueOf(rs.getString("type").toUpperCase());

        AuctionStatus status = calculateStatus(startTime, endTime);

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
    public ArrayList<AuctionInfo> searchAuctionByUserId(int userId) {
        String sql = BASE_SELECT_QUERY + " WHERE u_seller.id = ?";
        ArrayList<AuctionInfo> results = executeQueryAndGetList(sql, userId);
        return results;
    }
//    public AuctionInfo searchAuctionByInterestedId(int userId) {
//        String sql = BASE_SELECT_QUERY + " WHERE .id = ?";
//        ArrayList<AuctionInfo> results = executeQueryAndGetList(sql, userId);
//        return results.isEmpty() ? null : results.get(0);
//    }


    public boolean createAuction(Connection connection, int itemId, int sellerId, double price, double stepPrice, LocalDateTime startTime, LocalDateTime endTime) throws SQLException {
        AuctionStatus initialStatus = AuctionDAO.getInstance().calculateStatus(startTime, endTime);
        
        String query = "INSERT INTO auctions(itemId, sellerId, startingPrice, priceStep, curPrice, startTime, endTime, status) VALUES(?,?,?,?,?,?,?,?);";

        try (PreparedStatement pr = connection.prepareStatement(query)) {
            pr.setInt(1, itemId);
            pr.setInt(2, sellerId);
            pr.setDouble(3, price);
            pr.setDouble(4, stepPrice);
            pr.setDouble(5, price);
            pr.setTimestamp(6, Timestamp.valueOf(startTime));
            pr.setTimestamp(7, Timestamp.valueOf(endTime));
            pr.setString(8, initialStatus.name());
            return pr.executeUpdate() > 0;
        }
    }

    public void updateAuction(Connection connection, AuctionInfo auction, int bidderId, double curPrice) throws SQLException {
        String query = "UPDATE auctions SET curPrice = ?, lastBidderId = ? " +
                   "WHERE id = ? AND curPrice = ? AND status = 'OPEN' AND endTime > NOW();";
        try (PreparedStatement pr = connection.prepareStatement(query)) {
            pr.setDouble(1, auction.getCurPrice() + auction.getBidStep());
            pr.setInt(2, bidderId);
            pr.setInt(3, auction.getId());
            pr.setDouble(4, curPrice);
            if (pr.executeUpdate() == 0) throw new SQLException("Cập nhật đấu giá thất bại (Có thể do giá đã thay đổi).");
        }
    }

    public void updateTransaction(Connection connection, AuctionInfo auction, int bidderId) throws SQLException {
        String query = "INSERT INTO bidTransactions(userId, auctionId, bidAmount) VALUES (?,?,?);";
        try (PreparedStatement pr = connection.prepareStatement(query)) {
            pr.setInt(1, bidderId);
            pr.setInt(2, auction.getId());
            pr.setDouble(3, auction.getCurPrice() + auction.getBidStep());
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

    public void refreshAuctionStatuses() throws SQLException {
        try (Connection conn = getConnect()) {
            String openSql = "UPDATE auctions SET status = 'OPEN' WHERE status = 'PENDING' AND startTime <= NOW()";
            String closeSql = "UPDATE auctions SET status = 'CLOSED' WHERE status = 'OPEN' AND endTime <= NOW()";
            
            try (Statement st = conn.createStatement()) {
                st.executeUpdate(openSql);
                st.executeUpdate(closeSql);
            }
        }
    }

    private AuctionStatus calculateStatus(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(start)) return AuctionStatus.PENDING;
        if (now.isAfter(end)) return AuctionStatus.CLOSED;
        return AuctionStatus.OPEN;
    }

    public void autoUpdateStatuses() {
        String sql = "UPDATE auctions SET status = " +
                    "CASE " +
                    "  WHEN startTime > NOW() THEN 'PENDING' " +
                    "  WHEN endTime < NOW() THEN 'CLOSED' " +
                    "  ELSE 'OPEN' " +
                    "END " +
                    "WHERE status != 'CANCELED' AND status != 'CLOSED'";
        try (Connection conn = getConnect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean deleteAuction(int itemId) {
        String sql = "DELETE FROM items WHERE id = ?"; 
        try (Connection conn = getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            return pstmt.executeUpdate() > 0;
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

public boolean updateAuction(AuctionInfo info) {
        // 1. SQL cho bảng items (Tên, mô tả)
        String sqlItem = "UPDATE items SET title = ?, description = ? WHERE id = (SELECT itemId FROM auctions WHERE id = ?)";
        
        // 2. SQL cho bảng auctions (Giá, Bước giá)
        String sqlAuc = "UPDATE auctions SET startingPrice = ?, priceStep = ?, startTime = ?, endTime = ? WHERE id = ?";
        
        // 3. SQL cho bảng chi tiết (Tùy loại) - Đã viết đủ cột
        String sqlDetail = switch (info.getItemInfo().getItemType()) {
            case ARTS -> "UPDATE arts SET artist = ?, yearOfcreation = ?, dimensions = ?, medium = ? WHERE itemId = (SELECT itemId FROM auctions WHERE id = ?)";
            case ELECTRONICS -> "UPDATE electronics SET brand = ?, power = ?, voltage = ?, current = ?, status = ?, color = ?, weight = ? WHERE itemId = (SELECT itemId FROM auctions WHERE id = ?)";
            case VEHICLE -> "UPDATE vehicles SET brand = ?, model = ?, mileage = ?, mFG = ?, trim = ?, titleStatus = ? WHERE itemId = (SELECT itemId FROM auctions WHERE id = ?)";
            default -> "";
        };

        try (Connection conn = getConnect()) {
            conn.setAutoCommit(false); // Bắt đầu Transaction

            try (PreparedStatement psItem = conn.prepareStatement(sqlItem);
                 PreparedStatement psAuc = conn.prepareStatement(sqlAuc);
                 PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {

                // Bind dữ liệu cho Items
                psItem.setString(1, info.getItemInfo().getTitle());
                psItem.setString(2, info.getItemInfo().getDescription());
                psItem.setInt(3, info.getId());
                psItem.executeUpdate();

                // Bind dữ liệu cho Auctions
                psAuc.setDouble(1, info.getCurPrice());
                psAuc.setDouble(2, info.getBidStep());
                psAuc.setTimestamp(3, Timestamp.valueOf(info.getStartTime()));
                psAuc.setTimestamp(4, Timestamp.valueOf(info.getEndTime()));
                psAuc.setInt(5, info.getId());
                psAuc.executeUpdate();

                // 👉 ĐÃ FIX: Bind dữ liệu đầy đủ cho từng loại mặt hàng
                Map<String, String> data = info.getExtraData();
                if (data != null && !sqlDetail.isEmpty()) {
                    switch (info.getItemInfo().getItemType()) {
                        case ARTS -> {
                            psDetail.setString(1, data.getOrDefault("artist", ""));
                            psDetail.setInt(2, Integer.parseInt(data.getOrDefault("year", "0")));
                            psDetail.setString(3, data.getOrDefault("dimensions", ""));
                            psDetail.setString(4, data.getOrDefault("medium", ""));
                            psDetail.setInt(5, info.getId());
                        }
                        case ELECTRONICS -> {
                            psDetail.setString(1, data.getOrDefault("brand", ""));
                            psDetail.setInt(2, Integer.parseInt(data.getOrDefault("power", "0")));
                            psDetail.setDouble(3, Double.parseDouble(data.getOrDefault("voltage", "0")));
                            psDetail.setDouble(4, Double.parseDouble(data.getOrDefault("current", "0")));
                            psDetail.setString(5, data.getOrDefault("status", ""));
                            psDetail.setString(6, data.getOrDefault("color", ""));
                            psDetail.setDouble(7, Double.parseDouble(data.getOrDefault("weight", "0")));
                            psDetail.setInt(8, info.getId());
                        }
                        case VEHICLE -> {
                            psDetail.setString(1, data.getOrDefault("brand", ""));
                            psDetail.setString(2, data.getOrDefault("model", ""));
                            psDetail.setDouble(3, Double.parseDouble(data.getOrDefault("mileage", "0")));
                            psDetail.setInt(4, Integer.parseInt(data.getOrDefault("mFG", "0")));
                            psDetail.setString(5, data.getOrDefault("trim", ""));
                            psDetail.setString(6, data.getOrDefault("titleStatus", ""));
                            psDetail.setInt(7, info.getId());
                        }
                    }
                    psDetail.executeUpdate(); // Lệnh thực thi đã an toàn
                }

                conn.commit(); // Chốt đơn!
                return true;
            } catch (Exception e) {
                conn.rollback(); 
                e.printStackTrace();
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return false;
    }
    
}