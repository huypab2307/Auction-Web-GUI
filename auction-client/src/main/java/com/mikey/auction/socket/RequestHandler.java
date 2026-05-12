package com.mikey.auction.socket;

import com.google.gson.Gson;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.items.ItemType;
import java.io.PrintWriter;

public class RequestHandler {
    private static RequestHandler instance;
    private final Gson gson = new Gson();
    private PrintWriter out;

    private RequestHandler() {}

    public static RequestHandler getInstance() {
        if (instance == null) instance = new RequestHandler();
        return instance;
    }

    // Được gọi 1 lần duy nhất khi Client kết nối thành công tới Server
    public void setPrintWriter(PrintWriter out) { 
        this.out = out; 
    }

    // Hàm lõi để gửi dữ liệu
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
        // Đóng gói toàn bộ object AuctionInfo thành JSON
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
        send("ITEM|FIND|" + type.name() + "|" + itemId);
    }

}