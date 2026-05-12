package com.mikey.auction.socket.Handlers;

import com.google.gson.Gson;
import com.mikey.auction.items.Item;
import com.mikey.auction.items.ItemType;
import com.mikey.auction.manager.ItemManager;
import java.io.PrintWriter;

public class ItemHandler {
    private static final Gson gson = new Gson();

    public static void handleItem(String message, PrintWriter out) {
        try {
            String[] parts = message.split("\\|");
            if (parts.length < 4) return;

            String action = parts[1];
            if ("FIND".equals(action)) {
                // ITEM|FIND|TYPE|ID
                ItemType type = ItemType.valueOf(parts[2].toUpperCase());
                int itemId = Integer.parseInt(parts[3]);
                
                // Gọi Manager xử lý phía Server
                Item item = ItemManager.getInstance().findItemById(type, itemId);
                
                out.println("ITEM|FIND|" + gson.toJson(item));
                out.flush();
            }
        } catch (Exception e) {
            out.println("ITEM|ERROR|" + e.getMessage());
            out.flush();
        }
    }
}