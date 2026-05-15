package com.mikey.auction.socket;

import java.io.PrintWriter;
import com.mikey.auction.database.AuctionDAO;
import com.mikey.auction.database.UserDAO;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.items.Item;
import com.mikey.auction.items.ItemType;
import com.mikey.auction.manager.AuctionManager;
import com.mikey.auction.manager.ItemManager;
import com.mikey.auction.user.Bidder;
import com.mikey.auction.user.Role;
import com.mikey.auction.manager.UserManager;

// THÊM THƯ VIỆN GSON CHO NGÀY THÁNG
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

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
                        // Gọi hàm update trong Manager (bạn cần viết thêm hàm này)
                        result = AuctionManager.getInstance().updateAuction(p);
                    } else {
                        // 3. Nếu ID <= 0 thì mới là TẠO MỚI (Logic cũ của bạn)
                        Item item = ItemManager.getInstance().findItemById(
                            p.getItemInfo().getItemType(), 
                            p.getItemInfo().getItemId()
                        );
                        
                        if (item != null) {
                            AuctionManager.getInstance().uploadItem(
                                item, p.getCurPrice(), p.getBidStep(), 
                                p.getStartTime(), p.getEndTime()
                            );
                            result = true;
                        } else {
                            result = false;
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
            }

            if (result != null) {
                out.println("AUCTION|" + action + "|" + gson.toJson(result));
                out.flush();
            }
        } catch (Exception e) {
            out.println("AUCTION|ERROR|" + e.getMessage());
            out.flush();
            e.printStackTrace();
        }
    }
}