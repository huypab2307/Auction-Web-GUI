package com.mikey.auction.socket;

import java.io.PrintWriter;
import com.google.gson.Gson;
import com.mikey.auction.manager.NotificationManager;

public class NotificationHandlers {
    private static final Gson gson = new Gson();

    public static void handleNotification(String message, PrintWriter out) {
        try {
            String[] parts = message.split("\\|");
            if (parts.length < 2) return;

            String action = parts[1].trim();
            Object result = null;

            switch (action) {
                case "SHOW":
                    int userId = Integer.parseInt(parts[2].trim());
                    result = NotificationManager.getInstance().findNotififications(userId);
                    break;
                case "READ":
                    int rUserId = Integer.parseInt(parts[2].trim());
                    int notiId = Integer.parseInt(parts[3].trim());
                    result = NotificationManager.getInstance().markAsRead(rUserId, notiId);
                    break;
                case "FOLLOW":
                    int fUserId = Integer.parseInt(parts[2].trim());
                    int fAuctionId = Integer.parseInt(parts[3].trim());
                    result = NotificationManager.getInstance().subscribeAuction(fAuctionId, fUserId);
                    break;
                case "UNFOLLOW":
                    int uUserId = Integer.parseInt(parts[2].trim());
                    int uAuctionId = Integer.parseInt(parts[3].trim());
                    result = NotificationManager.getInstance().unsubcribeAuction(uAuctionId, uUserId);
                    break;
                case "CHECK":
                    int cUserId = Integer.parseInt(parts[2].trim());
                    int cAuctionId = Integer.parseInt(parts[3].trim());
                    result = NotificationManager.getInstance().checkSubscribed(cAuctionId, cUserId);
                    break;
            }

            if (result != null) {
                out.println("NOTIFICATION|" + action + "|" + gson.toJson(result));
                out.flush();
            }
        } catch (Exception e) {
            out.println("NOTIFICATION|ERROR|" + e.getMessage());
            out.flush();
            e.printStackTrace();
        }
    }
}