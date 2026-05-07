package com.mikey.auction.socket;

import java.io.PrintWriter;

import com.google.gson.Gson;
import com.mikey.auction.database.AuctionDAO;
import com.mikey.auction.database.UserDAO;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.items.ItemType;
import com.mikey.auction.manager.AuctionManager;
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
                            response = "AUCTION|ALL|" + gson.toJson(AuctionManager.getInstance().auctionList());
                            break;
                        case "TYPE ARTS":
                            response = "AUCTION|TYPE ARTS|" + gson.toJson(AuctionDAO.getInstance().getAuctionsType(ItemType.ARTS));
                            break;
                        case "TYPE VEHICLE":
                            response = "AUCTION|TYPE VEHICLE|" + gson.toJson(AuctionDAO.getInstance().getAuctionsType(ItemType.VEHICLE));
                            break;
                        case "TYPE ELECTRONICS":
                            response = "AUCTION|TYPE ELECTRONICS|" + gson.toJson(AuctionDAO.getInstance().getAuctionsType(ItemType.ELECTRONICS));
                            break;
                        case "USER":
                            int userid = gson.fromJson(parts[2].trim(), int.class); 
                            response = "AUCTION|USER|" + gson.toJson(AuctionDAO.getInstance().searchAuctionByUserId(userid)); 
                            break;
                        case "SEARCH":
                            String keyword = gson.fromJson(parts[2].trim(), String.class); 
                            response = "AUCTION|SEARCH|" + gson.toJson(AuctionDAO.getInstance().searchAuction(keyword));
                            break;
                        case "CREATE":
                            try {
                                 AuctionInfo p = gson.fromJson(parts[2], AuctionInfo.class);
                                AuctionManager.getInstance().uploadItem(p.getItemInfo().toItem(), p.getCurPrice(), p.getBidStep(), p.getStartTime(), p.getEndTime());
                                response = "AUCTION|CREATE|SUCCESS";
                            } catch (Exception e) {
                                response = "AUCTION|CREATE|FAIL";
                            }
                            break;
                        case "PLACEBID":
                            try {
                                int auctionId = gson.fromJson(parts[2].trim(), int.class);
                                int userId = gson.fromJson(parts[3].trim(), int.class);
                                AuctionInfo auctionInfo = AuctionDAO.getInstance().searchAuctionById(auctionId);
                                Bidder bidder = (Bidder)UserDAO.getInstance().findById(userId);
                                AuctionManager.getInstance().placeBid(bidder, auctionInfo, auctionInfo.getCurPrice());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;

                    }
                    if (!response.isEmpty()) {
                synchronized (out) { 
                    out.println(response);
                    out.flush();
                }
            }
        }
    } catch (Exception e) {
            out.println("ERROR");
            e.printStackTrace();
        }
    }
}
