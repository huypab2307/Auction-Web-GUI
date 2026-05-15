package com.mikey.auction.socket.Handlers;

import com.mikey.auction.items.Item;
import com.mikey.auction.items.ItemType;
import com.mikey.auction.manager.ItemManager;
import java.io.PrintWriter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

public class ItemHandler {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, t, ctx) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, t, ctx) -> LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .create();

    public static void handleItem(String message, PrintWriter out) {
        try {
            String[] parts = message.split("\\|");
            if (parts.length < 4) return;

            String action = parts[1];
            if ("FIND".equals(action)) {
                ItemType type = ItemType.valueOf(parts[2].toUpperCase());
                int itemId = Integer.parseInt(parts[3]);
                
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