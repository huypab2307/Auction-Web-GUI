package com.mikey.auction.socket;

import java.io.PrintWriter;

import com.google.gson.Gson;
import com.mikey.auction.database.AuctionDAO;
import com.mikey.auction.database.UserDAO;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.items.Item;
import com.mikey.auction.items.ItemType;
import com.mikey.auction.manager.AuctionManager;
import com.mikey.auction.manager.ItemManager;
import com.mikey.auction.user.Bidder;

public class AuctionHandler {
    private static String response;
    private static final Gson gson = new Gson();
    public static void handleAuction(String message, PrintWriter out) {
        try {
            String[] parts = message.split("\\|");
              if (parts.length >= 2) {
                    String auctionData = parts[1].trim();
                    // Xử lý dữ liệu đấu giá (ví dụ: lưu vào database, cập nhật trạng thái, v.v.)
                    switch (auctionData) {
                        case "All":
                            response = gson.toJson(AuctionManager.getInstance().auctionList());
                            out.println(response);
                            out.flush();
                            break;
                        case "TYPE ARTS":
                            response = gson.toJson(AuctionDAO.getInstance().getAuctionsType(ItemType.ARTS));
                                out.println(response);
                                out.flush();
                            break;
                        case "TYPE VEHICLE":
                            response = gson.toJson(AuctionDAO.getInstance().getAuctionsType(ItemType.VEHICLE));
                            out.println(response);
                            out.flush();
                            break;
                        case "TYPE ELECTRONICS":
                            response = gson.toJson(AuctionDAO.getInstance().getAuctionsType(ItemType.ELECTRONICS));
                            out.println(response);
                            out.flush();
                            break;
                        case "USER":
                            int userid = gson.fromJson(parts[2].trim(), int.class); 
                            response = "AUCTION|USER|" + gson.toJson(AuctionDAO.getInstance().searchAuctionByUserId(userid)); 
                            out.println(response);
                            out.flush();
                            break;
                        case "SEARCH":
                            String keyword = gson.fromJson(parts[2].trim(), String.class); 
                            response = gson.toJson(AuctionDAO.getInstance().searchAuction(keyword));
                            out.println(response);
                            out.flush();
                            break;
                        case "CREATE":// Tạo đấu giá mới
                            try {
                                 AuctionInfo p = gson.fromJson(parts[2], AuctionInfo.class);
                                  // Lấy Item đầy đủ từ DB qua itemId trong ItemSummary
                                   Item item = ItemManager.getInstance().findItemById(
                                   p.getItemInfo().getItemType(), 
                                   p.getItemInfo().getItemId()
                                   );
        
                                    // Nếu Item không tồn tại, trả false
                                    if (item == null) {
                                     response = gson.toJson(false);
                                     out.println(response);
                                     out.flush();
                                     break;
                                    }
                                AuctionManager.getInstance().uploadItem(item, p.getCurPrice(), p.getBidStep(), p.getStartTime(), p.getEndTime());
                                response = gson.toJson(true);
                                out.println(response);
                                out.flush();
                    
                            } catch (Exception e) {
                                response = gson.toJson(false);
                                out.println(response);
                                out.flush();
                            }
                            break;
                        case "PLACEBID":
                            try {
                                int auctionId = gson.fromJson(parts[2].trim(), int.class);
                                int userId = gson.fromJson(parts[3].trim(), int.class);
                                AuctionInfo auctionInfo = AuctionDAO.getInstance().searchAuctionById(auctionId);
                                Bidder bidder = (Bidder)UserDAO.getInstance().findById(userId);
                                AuctionManager.getInstance().placeBid(bidder, auctionInfo, auctionInfo.getCurPrice());
                                response = gson.toJson(true);
                                out.println(response);
                                out.flush();
                            } catch (Exception e) {
                                response =  gson.toJson(false);
                                out.println(response);
                                out.flush();
                                e.printStackTrace();
                            }
                            break;

                    }
        }
    } catch (Exception e) {
            out.println("ERROR");
            e.printStackTrace();
        }
    }
}
