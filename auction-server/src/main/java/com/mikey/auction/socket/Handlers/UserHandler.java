package com.mikey.auction.socket.Handlers;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.mikey.auction.database.UserDAO;

public class UserHandler {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, t, ctx) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, t, ctx) -> LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .create();

    public static void handleUser(String message, PrintWriter out) {
        try {
            String[] parts = message.split("\\|", 5);
            if (parts.length < 2) return;

            String action = parts[1].trim();
            Object result = null;

            switch (action) {
                case "CHANGE_PASSWORD":
                    int userId = Integer.parseInt(parts[2].trim());
                    String oldPass = parts[3].trim();
                    String newPass = parts[4].trim();
                    
                    if (UserDAO.getInstance().checkPassword(userId, oldPass)) {
                        result = UserDAO.getInstance().changePassword(userId, newPass);
                    } else {
                        result = false; // Mật khẩu cũ sai
                    }
                    break;
                case "DELETE_ACCOUNT":
                    result = true;
                    break;
                    
                case "UPDATE_AVATAR":
                    if (parts.length < 4) {
                        result = "FAIL";
                        break;
                    }
                    int id = Integer.parseInt(parts[2].trim());
                    String base64String = parts[3];

                    boolean isAvatarUpdated = UserDAO.getInstance().updateAvatar(id, base64String);
                    result = isAvatarUpdated ? "SUCCESS" : "FAIL";
                    break;
            }

            if (result != null) {
                out.println("USER|" + action + "|" + gson.toJson(result));
                out.flush();
            }
        } catch (Exception e) {
            out.println("USER|ERROR|" + e.getMessage());
            out.flush();
            e.printStackTrace();
        }
    }
}