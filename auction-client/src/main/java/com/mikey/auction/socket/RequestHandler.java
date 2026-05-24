package com.mikey.auction.socket;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.items.ItemType;

public class RequestHandler {
    private static RequestHandler instance;
    
    // ĐÃ THAY LÕI GSON ĐỂ CHỐNG CRASH KHI ĐĂNG SẢN PHẨM MỚI
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, t, ctx) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, t, ctx) -> LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .create();
            
    private PrintWriter out;

    private RequestHandler() {}

    public static RequestHandler getInstance() {
        if (instance == null) instance = new RequestHandler();
        return instance;
    }

    public void setPrintWriter(PrintWriter out) { 
        this.out = out; 
    }

    private void send(String message) {
        if (out != null) {
            out.println(message);
            out.flush();
            System.out.println("[CLIENT SENT]: " + message);
        } else {
            System.err.println("Lỗi: Chưa có kết nối Socket (PrintWriter is null)!");
        }
    }

    // ==========================================
    // 1. NHÓM TÀI KHOẢN (AUTH)
    // ==========================================
    public void requestLogin(String username, String password) {
        send("LOGIN|" + username + "|" + password);
    }

    public void requestRegister(String username, String password) {
        send("REGISTER|" + username + "|" + password);
    }

    // ==========================================
    // 2. NHÓM NGHIỆP VỤ ĐẤU GIÁ (AUCTION)
    // ==========================================
    public void requestAllAuctions() {
        send("AUCTION|All");
    }

    public void requestAuctionsByType(ItemType type) {
        send("AUCTION|TYPE|" + type.name());
    }

    public void requestSearch(String keyword) {
        send("AUCTION|SEARCH|" + keyword);
    }

    public void requestSearchById(int auctionId) {
        send("AUCTION|SEARCH_BY_ID|" + auctionId);
    }

    public void requestUserAuctions(int userId) {
        send("AUCTION|USER|" + userId);
    }

    public void requestCreateAuction(AuctionInfo info) {
        send("AUCTION|CREATE|" + gson.toJson(info));
    }

    public void requestPlaceBid(int auctionId, int userId) {
        send("AUCTION|PLACEBID|" + auctionId + "|" + userId);
    }

    // ==========================================
    // 3. NHÓM THÔNG BÁO (NOTIFICATION)
    // ==========================================
    public void requestShowNotifications(int userId) {
        send("NOTIFICATION|SHOW|" + userId);
    }

    public void requestMarkAsRead(int userId, int notificationId) {
        send("NOTIFICATION|READ|" + userId + "|" + notificationId);
    }

    public void requestFollow(int userId, int auctionId) {
        send("NOTIFICATION|FOLLOW|" + userId + "|" + auctionId);
    }

    public void requestUnfollow(int userId, int auctionId) {
        send("NOTIFICATION|UNFOLLOW|" + userId + "|" + auctionId);
    }

    public void requestCheckSubscription(int userId, int auctionId) {
        send("NOTIFICATION|CHECK|" + userId + "|" + auctionId);
    }

    // ==========================================
    // 4. NHÓM CÀI ĐẶT NGƯỜI DÙNG (USER)
    // ==========================================
    public void requestChangePassword(int userId, String oldPass, String newPass) {
        send("USER|CHANGE_PASSWORD|" + userId + "|" + oldPass + "|" + newPass);
    }

    public void requestDeleteAccount(int userId) {
        send("USER|DELETE_ACCOUNT|" + userId);
    }

    public void requestFindItem(ItemType type, int itemId) {
        String typeStr = (type != null) ? type.name() : "UNKNOWN";
        send("ITEM|FIND|" + typeStr + "|" + itemId);
    }

    // Thêm vào RequestHandler.java
    public void requestDeleteAuction(int auctionId) {
        send("AUCTION|DELETE|" + auctionId);
    }

// Hàm gửi yêu cầu cài đặt Auto Bid lên Server
    public void requestSetAutoBid(com.mikey.auction.dto.AutoBidInfo autoInfo) {
        try {
            // Tận dụng luôn biến gson đã được cấu hình sẵn ở đầu class
            String jsonData = gson.toJson(autoInfo);
            
            // 👉 ĐÃ SỬA: Gọi đúng tên hàm send() đang có trong class của bạn
            send("AUCTION|AUTOBID|" + jsonData); 
            
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi yêu cầu Auto Bid: " + e.getMessage());
        }
    }   

    public void requestDashboardStats(int userId) {
        send("AUCTION|DASHBOARD|" + userId);
    }

    // THÊM VÀO RequestHandler.java
    public void requestBidHistory(int auctionId) {
        send("AUCTION|HISTORY|" + auctionId);
    }

    // THÊM VÀO RequestHandler.java
    public void requestBidHistoryDaily(int auctionId) {
        send("AUCTION|HISTORY_DAILY|" + auctionId);
    }

    public void requestAllUsers() {
        send("AUCTION|GET_ALL_USERS"); // Sửa lại thành AUCTION
    }

    // THÊM VÀO RequestHandler.java
    public void requestBanUser(int userId) {
        send("AUCTION|BAN_USER|" + userId);
    }

    // THÊM VÀO CUỐI FILE RequestHandler.java
    public void requestUnbanUser(int userId) {
        send("AUCTION|UNBAN_USER|" + userId);
    }

// SỬA LẠI HÀM NÀY CHO CHUẨN VỚI KIẾN TRÚC CỦA BẠN:
    public void requestAllBidHistory() {
        send("AUCTION|GET_ALL_BID_HISTORY");
    }

    public void requestUpdateAvatar(int userId, String base64Image) {
    // Cấu trúc gói tin: CATEGORY | ACTION | ID | DATA_ẢNH
    String message = "USER|UPDATE_AVATAR|" + userId + "|" + base64Image;
        send(message); 
}

    
}