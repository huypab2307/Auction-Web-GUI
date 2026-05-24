package com.mikey.auction.socket;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.mikey.auction.database.AuctionDAO;
import com.mikey.auction.database.UserDAO;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.items.Item;
import com.mikey.auction.items.ItemType;
import com.mikey.auction.manager.AuctionManager;
import com.mikey.auction.manager.ItemManager;
import com.mikey.auction.manager.UserManager;
import com.mikey.auction.user.Bidder;
import com.mikey.auction.user.Role;
import com.mikey.auction.user.User;

public class AuctionHandler {
    // ĐÃ FIX LỖI GSON CRASH APP
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, t, ctx) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, t, ctx) -> LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .create();

    public static void handleAuction(String message, PrintWriter out) {
        try {
            String[] parts = message.split("\\|");
            if (parts.length < 2) return;

            String action = parts[1].trim();
            Object result = null;

            switch (action) {
                case "All":
                    result = AuctionManager.getInstance().auctionList();
                    break;
                case "TYPE":
                    if (parts.length >= 3) {
                        ItemType type = ItemType.valueOf(parts[2].trim().toUpperCase());
                        result = AuctionDAO.getInstance().getAuctionsType(type);
                    }
                    break;
                case "SEARCH":
                    result = AuctionDAO.getInstance().searchAuction(parts[2].trim());
                    break;
                case "USER":
                    int userid = Integer.parseInt(parts[2].trim());
                    result = AuctionDAO.getInstance().searchAuctionByUserId(userid);
                    break;
                case "CREATE":
                    // 1. Giải mã JSON nhận được từ Client
                    AuctionInfo p = gson.fromJson(parts[2], AuctionInfo.class);

                    // 2. Kiểm tra: Nếu ID > 0 thì đây là yêu cầu CẬP NHẬT
                    if (p.getId() > 0) {
                        // Gọi hàm update trong Manager
                        result = AuctionManager.getInstance().updateAuction(p);

                        // 👉 ĐÃ THÊM: Nếu cập nhật thành công, đồng bộ lại bộ đếm ngược thời gian kết thúc mới
                   if (Boolean.TRUE.equals(result)) {
                        com.mikey.auction.manager.AuctionScheduler.getInstance().scheduleAuctionClose(p);
               }
                    } else {
                        // 3. Nếu ID <= 0 thì TẠO MỚI
                        java.sql.Connection conn = null;
                        try {
                            conn = AuctionDAO.getInstance().getConnect();

                            // Tạo Item mới từ dữ liệu trong extraData
                            java.util.HashMap<String, String> itemDataMap = new java.util.HashMap<>(p.getExtraData());
                            Item item = ItemManager.getInstance().preProcessing(itemDataMap);

                            // Lưu Item vào database (sẽ tạo ID tự động)
                            item = ItemManager.getInstance().uploadItem(conn, item);

                            // Tạo Auction từ Item đã lưu
                            AuctionManager.getInstance().uploadItem(
                                item, p.getCurPrice(), p.getBidStep(),
                                p.getStartTime(), p.getEndTime()
                            );
                            result = true;
                        } catch (Exception e) {
                            System.err.println("Lỗi khi tạo sản phẩm: " + e.getMessage());
                            e.printStackTrace();
                            result = false;
                        } finally {
                            if (conn != null) try { conn.close(); } catch (Exception e) {}
                        }
                    }
                    break;
                case "PLACEBID":
                    int auctionId = Integer.parseInt(parts[2].trim());
                    int uId = Integer.parseInt(parts[3].trim());
                    AuctionInfo auctionInfo = AuctionDAO.getInstance().searchAuctionById(auctionId);
                    Bidder bidder = (Bidder) UserManager.getInstance().createUser(Role.BIDDER, UserDAO.getInstance().findById(uId));
                    result = AuctionManager.getInstance().placeBid(bidder, auctionInfo, auctionInfo.getCurPrice());
                    break;

                case "DELETE":
                    int idToDelete = Integer.parseInt(parts[2].trim());
                    // Bạn cần triển khai hàm deleteAuction trong AuctionDAO để chạy lệnh SQL: 
                    // "DELETE FROM auctions WHERE id = ?"
                    result = AuctionDAO.getInstance().deleteAuction(idToDelete); 
                    break;

                case "AUTOBID":
                    // Xử lý gói tin: AUTOBID|SET|{json_data}
                    if (parts.length >= 3) {
                        // Nhớ tạo class AutoBidInfo trong package dto nhé!
                        com.mikey.auction.dto.AutoBidInfo autoInfo = gson.fromJson(parts[2], com.mikey.auction.dto.AutoBidInfo.class);
                        result = AuctionManager.getInstance().registerAutoBid(autoInfo);
                    }
                    break;

                case "DASHBOARD":
                    if (parts.length >= 3) {
                        int userId = Integer.parseInt(parts[2].trim());
                        // Lấy thống kê dữ liệu trực tiếp từ DAO
                        result = AuctionDAO.getInstance().getDashboardStats(userId);
                    }
                    break;

                // THÊM NHÁNH NÀY VÀO TRONG switch (action)
                case "HISTORY":
                    int histAuctionId = Integer.parseInt(parts[2].trim());
                    result = AuctionDAO.getInstance().getBidHistory(histAuctionId);
                    break;

                // THÊM VÀO TRONG switch (action) CỦA AuctionHandler.java
                case "HISTORY_DAILY":
                    int dailyAucId = Integer.parseInt(parts[2].trim());
                    result = AuctionDAO.getInstance().getBidHistoryByDate(dailyAucId);
                    break;

                // TÌM ĐẾN KHỐI SWITCH XỬ LÝ LỆNH "USER" TRÊN SERVER VÀ THÊM VÀO:
                // SỬA CASE NÀY TRONG AuctionHandler.java:
                case "GET_ALL_USERS":
                    ArrayList<User> allUsers = UserDAO.getInstance().getAllUsers();
                    
                    // Phải trả về mác AUCTION để Client nhận diện đúng cửa
                    out.println("AUCTION|GET_ALL_USERS|" + gson.toJson(allUsers));
                    out.flush();
                    break;

                // THÊM VÀO TRONG KHỐI SWITCH CỦA AuctionHandler.java
                case "BAN_USER":
                    int userIdToBan = Integer.parseInt(parts[2].trim());
                    boolean isSuccess = UserDAO.getInstance().updateUserStatus(userIdToBan, "BANNED");
                    
                    if (isSuccess) {
                        // Broadcast hét lên cho toàn hệ thống biết tài khoản này đã bị trảm
                        String broadcastMsg = "AUCTION|BAN_USER_SUCCESS|" + userIdToBan;
                        AuctionServer.broadcast(broadcastMsg);
                    }
                    break;

                // THÊM VÀO TRONG KHỐI SWITCH CỦA AuctionHandler.java
                case "UNBAN_USER":
                    int userIdToUnban = Integer.parseInt(parts[2].trim());
                    // Gọi hàm UPDATE cột status thành ACTIVE dưới Database
                    boolean isUnbanSuccess = UserDAO.getInstance().updateUserStatus(userIdToUnban, "ACTIVE");
                    
                    if (isUnbanSuccess) {
                        // Cầm loa phát thanh Broadcast báo cho toàn bộ các Client đang mở biết để cập nhật UI
                        String broadcastMsg = "AUCTION|UNBAN_USER_SUCCESS|" + userIdToUnban;
                        AuctionServer.broadcast(broadcastMsg);
                    }
                    break;

                // 👉 THÊM VÀO ĐỂ XỬ LÝ LỆNH LẤY TOÀN BỘ LỊCH SỬ CHO ADMIN
                case "GET_ALL_BID_HISTORY":
                    result = AuctionDAO.getInstance().getAllSystemBidHistory();
                    break;
            }

            if (result != null) {
                // 1. Dòng này là Server trả lời riêng cho máy vừa bấm Đấu giá
                out.println("AUCTION|" + action + "|" + gson.toJson(result));
                out.flush();

                // 2. 👉 FIX LỖI ĐỒNG BỘ: Nếu action là PLACEBID hoặc AUTOBID, phải HÉT LÊN cho các máy khác biết!
                if ("PLACEBID".equals(action) || "AUTOBID".equals(action)) {
                    String broadcastMsg = "AUCTION|UPDATE_PRICE|" + gson.toJson(result);
                    AuctionServer.broadcast(broadcastMsg);
                }
            }
        } catch (Exception e) {
            out.println("AUCTION|ERROR|" + e.getMessage());
            out.flush();
            e.printStackTrace();
        }
    }
}