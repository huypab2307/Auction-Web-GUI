package com.mikey.auction.socket;

import java.io.PrintWriter;
import java.util.List;

import com.google.gson.Gson;
import com.mikey.auction.auction.Notifications;
import com.mikey.auction.manager.NotificationManager;




public class NotificationHandlers {
    private static final Gson gson = new Gson();
    private static String response;
    public static void handleNotification(String message, PrintWriter out) {
    try {
    String[] parts = message.split(" \\| ", 3);
    String action = parts[1].trim();
    String data = parts[2].trim();

    switch (action) {
        case "FOLLOW":
            // data sẽ là "userId auctionId" -> dùng split(" ") để lấy 2 ID
            String[] ids = data.split(" ");
            boolean isFollowed = NotificationManager.getInstance()
                                .subscribeAuction(Integer.parseInt(ids[1]), Integer.parseInt(ids[0]));
            out.println(gson.toJson(isFollowed));
            break;
        
        case "READ":
            String[] readParams = data.split(" ");
            boolean isRead = NotificationManager.getInstance()
                             .markAsRead(Integer.parseInt(readParams[0]), Integer.parseInt(readParams[1]));
            out.println(gson.toJson(isRead));
            break;

        case "SHOW":
            int userId = Integer.parseInt(data);
            List<Notifications> list = NotificationManager.getInstance().findNotififications(userId);
            out.println(gson.toJson(list));
            break;
        case "UNFOLLOW":
            String[] unfollowIds = data.split(" ");
           // boolean isUnfollowed = NotificationManager.getInstance()
                                //.unsubscribeAuction(Integer.parseInt(unfollowIds[1]), Integer.parseInt(unfollowIds[0]));
           // out.println(gson.toJson(isUnfollowed));
            break;
        case "CHECK":
            String[] checkParams = data.split(" ");
            break;
        default:
            break;
    }
} catch (Exception e) {
    e.printStackTrace();
    }
}
}
