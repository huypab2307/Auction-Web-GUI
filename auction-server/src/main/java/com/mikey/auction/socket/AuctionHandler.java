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
import com.mikey.auction.user.Role;
import com.mikey.auction.manager.UserManager;

public class AuctionHandler {
    private static final Gson gson = new Gson();

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
                    AuctionInfo p = gson.fromJson(parts[2], AuctionInfo.class);
                    Item item = ItemManager.getInstance().findItemById(
                        p.getItemInfo().getItemType(), p.getItemInfo().getItemId()
                    );
                    if (item != null) {
                        AuctionManager.getInstance().uploadItem(item, p.getCurPrice(), p.getBidStep(), p.getStartTime(), p.getEndTime());
                        result = true;
                    } else {
                        result = false;
                    }
                    break;
                case "PLACEBID":
                    int auctionId = Integer.parseInt(parts[2].trim());
                    int uId = Integer.parseInt(parts[3].trim());
                    AuctionInfo auctionInfo = AuctionDAO.getInstance().searchAuctionById(auctionId);
                    Bidder bidder = (Bidder) UserManager.getInstance().createUser(Role.BIDDER, UserDAO.getInstance().findById(uId));
                    result = AuctionManager.getInstance().placeBid(bidder, auctionInfo, auctionInfo.getCurPrice());
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