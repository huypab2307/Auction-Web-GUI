package com.mikey.auction.database;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

import com.mikey.auction.auction.Auction;
import com.mikey.auction.auction.AuctionStatus;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.dto.AutoBidInfo;
import com.mikey.auction.dto.ItemSummary;
import com.mikey.auction.items.ItemType;


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
        String dbStatus = rs.getString("status");
        AuctionStatus status;
        if ("CANCELED".equals(dbStatus)) {
            status = AuctionStatus.CANCELED; // Tôn trọng trạng thái Hủy từ Admin/Seller
        } else {
            status = calculateStatus(startTime, endTime); // Nếu bình thường thì mới tính theo thời gian
        }

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
        // Chỉ lấy những phiên đang OPEN (Đang mở) hoặc PENDING (Sắp mở)
        // Lọc sạch bách những thằng CANCELED và CLOSED
        String sql = BASE_SELECT_QUERY + " WHERE a.status = 'OPEN' OR a.status = 'PENDING'";
        return executeQueryAndGetList(sql);
    }

    public ArrayList<AuctionInfo> getAuctionsType(ItemType type) {
        // Tương tự cho hàm lấy theo danh mục
        String sql = BASE_SELECT_QUERY + " WHERE i.type = ? AND (a.status = 'OPEN' OR a.status = 'PENDING')";
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

// 👉 HÀM 1: DÀNH CHO ADMIN (Giữ nguyên chữ deleteAuction và 1 tham số như cũ)
    // Code cũ của bạn gọi hàm này sẽ KHÔNG BỊ BÁO LỖI, không cần sửa gì thêm!
    public boolean deleteAuction(int auctionId) {
        String sql = "UPDATE auctions SET status = 'CANCELED' WHERE id = ?";
        try (Connection conn = getConnect(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, auctionId);
            if (ps.executeUpdate() > 0) {
                System.out.println("✅ [ADMIN] Đã hủy thành công phiên đấu giá ID: " + auctionId);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 👉 HÀM 2: DÀNH CHO SELLER (Cũng tên là deleteAuction nhưng có thêm ID người bán)
    // Sau này làm chức năng cho Seller, bạn chỉ cần truyền thêm sellerId vào là nó tự chạy hàm này.
    public boolean deleteAuction(int auctionId, int sellerId) {
        String sql = "UPDATE auctions SET status = 'CANCELED' WHERE id = ? AND sellerId = ? AND lastBidderId IS NULL";
        try (Connection conn = getConnect(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, auctionId);
            ps.setInt(2, sellerId);
            
            if (ps.executeUpdate() > 0) {
                System.out.println("✅ [SELLER] Đã tự hủy thành công phiên đấu giá ID: " + auctionId);
                return true;
            } else {
                System.out.println("❌ [SELLER] Hủy thất bại: Sai quyền hoặc đã có người đặt giá!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateAuction(AuctionInfo info) {
        // 1. SQL cho bảng items (Chỉ cập nhật Tên, Mô tả - BỎ cập nhật cột type)
        String sqlItem = "UPDATE items SET title = ?, description = ? WHERE id = (SELECT itemId FROM auctions WHERE id = ?)";
        
        // 2. SQL cho bảng auctions (Đã cập nhật cả startingPrice và curPrice để đồng bộ UI hiển thị)
        String sqlAuc = "UPDATE auctions SET startingPrice = ?, curPrice = ?, priceStep = ?, startTime = ?, endTime = ? WHERE id = ?";
        
        // 3. SQL cho các bảng chi tiết (Quay lại dùng UPDATE trực tiếp vì loại hàng đã được khóa cố định)
        String sqlDetail = switch (info.getItemInfo().getItemType()) {
            case ARTS -> "UPDATE arts SET artist = ?, yearOfcreation = ?, dimensions = ?, medium = ? WHERE itemId = (SELECT itemId FROM auctions WHERE id = ?)";
            case ELECTRONICS -> "UPDATE electronics SET brand = ?, power = ?, voltage = ?, current = ?, status = ?, color = ?, weight = ? WHERE itemId = (SELECT itemId FROM auctions WHERE id = ?)";
            case VEHICLE -> "UPDATE vehicles SET brand = ?, model = ?, mileage = ?, mFG = ?, trim = ?, titleStatus = ? WHERE itemId = (SELECT itemId FROM auctions WHERE id = ?)";
            default -> "";
        };

        try (Connection conn = getConnect()) {
            conn.setAutoCommit(false); // Bắt đầu Transaction để bảo vệ dữ liệu

            try (PreparedStatement psItem = conn.prepareStatement(sqlItem);
                 PreparedStatement psAuc = conn.prepareStatement(sqlAuc);
                 PreparedStatement psDetail = !sqlDetail.isEmpty() ? conn.prepareStatement(sqlDetail) : null) {

                // Bind dữ liệu cho bảng Items (Tên & Mô tả)
                psItem.setString(1, info.getItemInfo().getTitle());
                psItem.setString(2, info.getItemInfo().getDescription());
                psItem.setInt(3, info.getId());
                psItem.executeUpdate();

                // Bind dữ liệu cho bảng Auctions (Giá khởi điểm, Giá hiện tại, Bước giá và Thời gian)
                psAuc.setDouble(1, info.getCurPrice()); // Lưu vào cột startingPrice mới
                psAuc.setDouble(2, info.getCurPrice()); // Cập nhật luôn cột curPrice giúp UI đồng bộ lập tức
                psAuc.setDouble(3, info.getBidStep());
                psAuc.setTimestamp(4, Timestamp.valueOf(info.getStartTime()));
                psAuc.setTimestamp(5, Timestamp.valueOf(info.getEndTime()));
                psAuc.setInt(6, info.getId());
                psAuc.executeUpdate();

                // Bind dữ liệu cho bảng thuộc tính chi tiết tương ứng
                Map<String, String> data = info.getExtraData();
                if (psDetail != null && data != null) {
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
                    psDetail.executeUpdate();
                }

                conn.commit(); // Lưu toàn bộ thay đổi thành công vào Database
                return true;
            } catch (Exception e) {
                conn.rollback(); // Hoàn tác nếu có bất kỳ lỗi xung đột nào xảy ra
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // 1. Hàm lưu cài đặt của người dùng vào bảng autoBidding
    public boolean registerAutoBid(AutoBidInfo info) {
        // ON DUPLICATE KEY UPDATE: Nếu họ cài rồi mà muốn đổi giá Max, nó sẽ tự update chứ không sinh ra 2 dòng
        String sql = "INSERT INTO autoBidding (userId, auctionId, maxPrice) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE maxPrice = ?";
        try (Connection conn = getConnect(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, info.getUserId());
            ps.setInt(2, info.getAuctionId());
            ps.setDouble(3, info.getMaxAmount()); // Lấy số tiền từ DTO
            ps.setDouble(4, info.getMaxAmount());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

// Trái tim của Auto Bidding: Đọ giá tự động bằng vòng lặp (Chống Deadlock)
    public void triggerAutoBids(int auctionId) {
        // Dùng 1 connection duy nhất xuyên suốt quá trình
        try (Connection conn = getConnect()) {
            boolean keepBidding = true;
            
            // Dùng vòng lặp thay cho đệ quy để tránh tràn bộ nhớ (StackOverflow)
            while (keepBidding) {
                conn.setAutoCommit(false); // Bắt đầu giao dịch an toàn
                try {
                    // 1. Lấy trạng thái MỚI NHẤT của phiên đấu giá (Dùng hàm findById để xài chung conn)
                    Auction currentAuc = findById(conn, auctionId);
                    if (currentAuc == null || currentAuc.getStatus() != AuctionStatus.OPEN) {
                        conn.rollback();
                        break; // Phiên đã đóng thì nghỉ luôn
                    }

                    // Giá tổi thiểu cần để nhảy vào đua: Giá hiện tại + Bước giá
                    double requiredToBid = currentAuc.getCurPrice() + currentAuc.getStepPrice();

                    // 2. Tìm người Auto Bid (Tiền phải đủ và không phải người vừa đặt giá)
                    String sqlFindAuto = "SELECT userId, maxPrice FROM autoBidding " +
                                         "WHERE auctionId = ? AND userId != ? AND maxPrice >= ? " +
                                         "ORDER BY maxPrice DESC LIMIT 1";

                    int autoBidderId = -1;
                    try (PreparedStatement ps = conn.prepareStatement(sqlFindAuto)) {
                        ps.setInt(1, auctionId);
                        ps.setInt(2, currentAuc.getLastBidder()); // Tránh tự đè giá chính mình
                        ps.setDouble(3, requiredToBid);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                autoBidderId = rs.getInt("userId");
                            }
                        }
                    }

                    // 3. Nếu có người đủ điều kiện -> Tiến hành đè giá!
                    if (autoBidderId != -1) {
                        // Viết lệnh SQL update trực tiếp cho nhẹ nhàng
                        String updateAuc = "UPDATE auctions SET curPrice = ?, lastBidderId = ? WHERE id = ? AND curPrice = ?";
                        try (PreparedStatement psUp = conn.prepareStatement(updateAuc)) {
                            psUp.setDouble(1, requiredToBid);
                            psUp.setInt(2, autoBidderId);
                            psUp.setInt(3, auctionId);
                            psUp.setDouble(4, currentAuc.getCurPrice()); // Chống đồng thời (Optimistic Locking)
                            
                            int rows = psUp.executeUpdate();
                            if (rows > 0) {
                                // Ghi đè thành công -> Lưu luôn vào lịch sử bidTransactions
                                String insertTrans = "INSERT INTO bidTransactions(userId, auctionId, bidAmount) VALUES (?,?,?)";
                                try (PreparedStatement psTrans = conn.prepareStatement(insertTrans)) {
                                    psTrans.setInt(1, autoBidderId);
                                    psTrans.setInt(2, auctionId);
                                    psTrans.setDouble(3, requiredToBid);
                                    psTrans.executeUpdate();
                                }
                                conn.commit(); 
                                System.out.println("🔥 AUTO-BID: User " + autoBidderId + " tự động trả " + requiredToBid + " đ cho Auction " + auctionId);
                                // Vòng lặp sẽ tiếp tục chạy để xem có đại gia nào khác muốn vào đọ tiền tiếp không!
                            } else {
                                conn.rollback(); // Giá bị lệch nhịp, roll lại rồi vòng sau check lại
                            }
                        }
                    } else {
                        // Không tìm thấy ai thỏa mãn nữa -> Dừng cuộc chơi
                        conn.rollback(); 
                        keepBidding = false; 
                    }
                } catch (SQLException e) {
                    conn.rollback();
                    System.err.println("Lỗi vòng lặp Auto Bid: " + e.getMessage());
                    keepBidding = false; // Có biến là dừng luôn
                } finally {
                    conn.setAutoCommit(true); // Trả lại trạng thái mặc định cho Connection
                }
            } // Hết while
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public com.mikey.auction.dto.DashboardStats getDashboardStats(int userId) {
        com.mikey.auction.dto.DashboardStats stats = new com.mikey.auction.dto.DashboardStats();
        
        try (Connection conn = getConnect()) {
            // 1. Tổng chi tiêu (Mua thành công)
            String sqlSpent = "SELECT IFNULL(SUM(curPrice), 0) FROM auctions WHERE lastBidderId = ? AND status = 'CLOSED'";
            try (PreparedStatement ps = conn.prepareStatement(sqlSpent)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) stats.setTotalSpent(rs.getDouble(1)); }
            }

            // 2. Đang tham gia đấu giá (Phiên đang OPEN và mình đang giữ top giá)
            String sqlActive = "SELECT COUNT(*) FROM auctions WHERE lastBidderId = ? AND status = 'OPEN'";
            try (PreparedStatement ps = conn.prepareStatement(sqlActive)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) stats.setActiveBids(rs.getInt(1)); }
            }

            // 3. Sản phẩm đã đấu giá thắng
            String sqlWon = "SELECT COUNT(*) FROM auctions WHERE lastBidderId = ? AND status = 'CLOSED'";
            try (PreparedStatement ps = conn.prepareStatement(sqlWon)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) stats.setWonItems(rs.getInt(1)); }
            }

            // 4. Số phiên bị vượt giá (Có vết lịch sử đặt giá nhưng hiện tại top 1 thuộc về người khác)
            String sqlOutbid = "SELECT COUNT(DISTINCT auctionId) FROM bidTransactions WHERE userId = ? " +
                               "AND auctionId IN (SELECT id FROM auctions WHERE lastBidderId != ? AND status = 'OPEN')";
            try (PreparedStatement ps = conn.prepareStatement(sqlOutbid)) {
                ps.setInt(1, userId);
                ps.setInt(2, userId);
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) stats.setOutbidCount(rs.getInt(1)); }
            }

            // 5. Sản phẩm đang theo dõi (Yêu thích)
            // 5. Sản phẩm đang theo dõi (Yêu thích) - ĐÃ SỬA LỖI TRÙNG BIẾN CONN
String sqlFollow = "SELECT COUNT(*) FROM notificationList WHERE userId = ?";
try (PreparedStatement ps = conn.prepareStatement(sqlFollow)) { // Sử dụng trực tiếp biến conn có sẵn của hàm
    ps.setInt(1, userId);
    try (ResultSet rs = ps.executeQuery()) { 
        if (rs.next()) stats.setFollowingCount(rs.getInt(1)); 
    }
} catch (Exception e) {
    System.err.println("Lỗi đồng bộ đếm người theo dõi: " + e.getMessage());
    stats.setFollowingCount(0);
}

            // 6. Sản phẩm người này đã bán thành công
            String sqlSold = "SELECT COUNT(*) FROM auctions WHERE sellerId = ? AND status = 'CLOSED' AND lastBidderId IS NOT NULL";
            try (PreparedStatement ps = conn.prepareStatement(sqlSold)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) stats.setSoldItems(rs.getInt(1)); }
            }

            // 7. Tính toán tỷ lệ thắng (%) = (Số phiên thắng / Tổng số phiên đã tham gia và đã kết thúc) * 100
            String sqlTotalJoined = "SELECT COUNT(DISTINCT auctionId) FROM bidTransactions t " +
                                    "INNER JOIN auctions a ON t.auctionId = a.id WHERE t.userId = ? AND a.status = 'CLOSED'";
            try (PreparedStatement ps = conn.prepareStatement(sqlTotalJoined)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        double rate = ((double) stats.getWonItems() / rs.getInt(1)) * 100;
                        stats.setWinRate(Math.round(rate * 10.0) / 10.0); // Làm tròn 1 chữ số thập phân
                    } else {
                        stats.setWinRate(0.0);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

// ĐÃ SỬA: Đổi 'users' thành 'user' cho khớp với Database của bạn
    public java.util.ArrayList<String> getBidHistory(int auctionId) {
        java.util.ArrayList<String> history = new java.util.ArrayList<>();
        
        // Chú ý dòng SQL thứ 2: JOIN user u
        String sql = "SELECT u.username, b.bidAmount FROM bidTransactions b " +
                     "JOIN user u ON b.userId = u.id " +
                     "WHERE b.auctionId = ? ORDER BY b.createdAt ASC";
                     
        try (java.sql.Connection conn = getConnect();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, auctionId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    history.add(rs.getString("username") + "|" + rs.getDouble("bidAmount"));
                }
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    public java.util.ArrayList<String> getBidHistoryByDate(int auctionId) {
        java.util.ArrayList<String> history = new java.util.ArrayList<>();
        // Lệnh GROUP BY DATE() là trái tim của tính năng này!
        String sql = "SELECT DATE(b.createdAt) as bidDate, MAX(b.bidAmount) as maxPrice " +
                     "FROM bidTransactions b " +
                     "JOIN user u ON b.userId = u.id " +
                     "WHERE b.auctionId = ? " +
                     "GROUP BY DATE(b.createdAt) " +
                     "ORDER BY bidDate ASC";
                     
        try (java.sql.Connection conn = getConnect();
             // ... phần code phía dưới giữ nguyên
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, auctionId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Trả về dạng: "2026-05-21|8500.0"
                    history.add(rs.getString("bidDate") + "|" + rs.getDouble("maxPrice"));
                }
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    // 👉 THÊM HÀM NÀY VÀO AUCTIONDAO BÊN SERVER
    public java.util.ArrayList<com.mikey.auction.dto.BidHistory> getAllSystemBidHistory() {
        java.util.ArrayList<com.mikey.auction.dto.BidHistory> list = new java.util.ArrayList<>();
        String sql = "SELECT b.id, b.auctionId, u.username, b.bidAmount, b.createdAt " +
                     "FROM bidTransactions b JOIN user u ON b.userId = u.id ORDER BY b.createdAt DESC";
        try (java.sql.Connection conn = getConnect();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new com.mikey.auction.dto.BidHistory(
                    rs.getInt("id"), rs.getInt("auctionId"), rs.getString("username"),
                    rs.getDouble("bidAmount"), rs.getObject("createdAt", java.time.LocalDateTime.class)
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public boolean closeSingleAuction(int auctionId) {
    String sql = "UPDATE auctions SET status = 'CLOSED' WHERE id = ? AND status = 'OPEN'";
    try (Connection conn = getConnect(); PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, auctionId);
        return ps.executeUpdate() > 0;
    } catch (SQLException e) {
        System.err.println("Lỗi khi đóng phiên đấu giá đơn lẻ: " + e.getMessage());
        return false;
    }
}
    // =========================================================================
    // 🔥 SIÊU THUẬT TOÁN ĐẤU GIÁ: CHỐNG LOST UPDATE & CHỐNG BẮN TỈA (ANTI-SNIPING)
    // =========================================================================
    public boolean placeBid(int auctionId, int userId) {
        // 1. KHÓA LUỒNG THEO ID (Concurrency Control)
        // Những người đấu giá CÙNG 1 sản phẩm sẽ phải xếp hàng.
        // Những sản phẩm khác nhau vẫn đấu giá song song mượt mà!
        synchronized (String.valueOf(auctionId).intern()) {
            
            try (java.sql.Connection conn = getConnect()) {
                // 2. TẮT AUTO COMMIT ĐỂ BẢO VỆ GIAO DỊCH (Transaction)
                conn.setAutoCommit(false); 

                try {
                    // 3. SELECT ... FOR UPDATE: Bắt các luồng đến sau phải đứng chờ luồng trước chạy xong
                    String sqlCheck = "SELECT curPrice, priceStep, endTime, status FROM auctions WHERE id = ? FOR UPDATE";
                    try (java.sql.PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {
                        psCheck.setInt(1, auctionId);
                        try (java.sql.ResultSet rs = psCheck.executeQuery()) {
                            if (rs.next()) {
                                double currentPrice = rs.getDouble("curPrice");
                                double stepPrice = rs.getDouble("priceStep");
                                java.time.LocalDateTime endTime = rs.getObject("endTime", java.time.LocalDateTime.class);
                                String status = rs.getString("status");

                                // Rào cản 1: Phiên không mở hoặc đã hết hạn
                                if (!"OPEN".equals(status) || java.time.LocalDateTime.now().isAfter(endTime)) {
                                    conn.rollback(); 
                                    return false;
                                }
                                
                                // Rào cản 2: Tính toán chính xác giá thầu tiếp theo ngay tại thời điểm Server đọc Database
                                double newBidAmount = currentPrice + stepPrice;

                                // 4. THUẬT TOÁN ANTI-SNIPING (Gia hạn thời gian)
                                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                                long secondsLeft = java.time.Duration.between(now, endTime).getSeconds();
                                java.time.LocalDateTime newEndTime = endTime;

                                // Nếu bị "bắn tỉa" ở 30 giây cuối -> Phạt gia hạn thêm 60 giây
                                if (secondsLeft <= 30 && secondsLeft > 0) {
                                    newEndTime = now.plusSeconds(60);
                                    System.out.println("⏰ ANTI-SNIPING KÍCH HOẠT! Phiên #" + auctionId + " bị bắn tỉa, gia hạn đến: " + newEndTime);
                                }

                                // 5. Cập nhật bảng auctions (Giá mới, Người giữ giá mới, Thời gian mới)
                                String sqlUpdate = "UPDATE auctions SET curPrice = ?, lastBidderId = ?, endTime = ? WHERE id = ?";
                                try (java.sql.PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate)) {
                                    psUpdate.setDouble(1, newBidAmount);
                                    psUpdate.setInt(2, userId);
                                    psUpdate.setTimestamp(3, java.sql.Timestamp.valueOf(newEndTime));
                                    psUpdate.setInt(4, auctionId);
                                    psUpdate.executeUpdate();
                                }

                                // 6. Ghi log lịch sử giao dịch (BidTransactions)
                                String sqlHistory = "INSERT INTO bidTransactions (userId, auctionId, bidAmount) VALUES (?, ?, ?)";
                                try (java.sql.PreparedStatement psHistory = conn.prepareStatement(sqlHistory)) {
                                    psHistory.setInt(1, userId);
                                    psHistory.setInt(2, auctionId);
                                    psHistory.setDouble(3, newBidAmount);
                                    psHistory.executeUpdate();
                                }

                                // MỌI THỨ AN TOÀN TUYỆT ĐỐI -> LƯU VÀO DATABASE
                                conn.commit();
                                return true;
                            }
                        }
                    }
                } catch (java.sql.SQLException e) {
                    conn.rollback(); // Có biến là hoàn tác lại toàn bộ, không sợ sai lệch data
                    e.printStackTrace();
                } finally {
                    conn.setAutoCommit(true); // Trả lại cơ chế mặc định
                }
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
            return false;
        }
    }
}