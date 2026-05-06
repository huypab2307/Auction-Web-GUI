package com.mikey.auction.socket;

import com.google.gson.Gson;
import com.mikey.auction.manager.AuctionManager;
import com.mikey.auction.database.AuctionDAO;
import com.mikey.auction.dto.AuctionInfo;

import java.io.PrintWriter;
import java.util.ArrayList;

import com.mikey.auction.items.ItemType;

public class AuctionHandler {
    private static final Gson gson = new Gson();
    public static void handleAuction(String message, PrintWriter out) {
        try {
            String[] parts = message.split("\\|");
              if (parts.length >= 2) {
                    String auctionData = parts[1].trim();
                    // Xử lý dữ liệu đấu giá (ví dụ: lưu vào database, cập nhật trạng thái, v.v.)
                    switch (auctionData) {
                        case "All":
                            ArrayList<AuctionInfo> p = AuctionManager.getInstance().auctionList();
                            out.println("AUCTION|ALL|" + gson.toJson(p));
                            out.flush();
                            break;
                        case "TYPE ARTS":
                            ArrayList<AuctionInfo> p1 = AuctionDAO.getInstance().getAuctionsType(ItemType.ARTS);
                            out.println("AUCTION|TYPE ARTS|" + gson.toJson(p1));
                            out.flush();
                            break;
                        case "TYPE VEHICLE":
                            ArrayList<AuctionInfo> p2 = AuctionDAO.getInstance().getAuctionsType(ItemType.VEHICLE);
                            out.println("AUCTION|TYPE VEHICLE|" + gson.toJson(p2));
                            out.flush();
                            break;
                        case "TYPE ELECTRONICS":
                            ArrayList<AuctionInfo> p3 = AuctionDAO.getInstance().getAuctionsType(ItemType.ELECTRONICS);
                            out.println("AUCTION|TYPE ELECTRONICS|" + gson.toJson(p3));
                            out.flush();
                            break;
                        case "USER":
                            int userid = gson.fromJson(parts[2].trim(), int.class);  
                            ArrayList<AuctionInfo> p4 = AuctionDAO.getInstance(). searchAuctionByUserId(userid);
                            out.println("AUCTION|USER|" + gson.toJson(p4));
                            out.flush();
                            break;
                        case "SEARCH":
                            String keyword = gson.fromJson(parts[2].trim(), String.class); 
                            ArrayList<AuctionInfo> p5 = AuctionDAO.getInstance().searchAuction(keyword);
                            out.println("AUCTION|SEARCH|" + gson.toJson(p5));
                            out.flush();
                            break;
                        
                    }
                   

            
        }
    } catch (Exception e) {
            out.println("ERROR");
            e.printStackTrace();
        }
    }
}
