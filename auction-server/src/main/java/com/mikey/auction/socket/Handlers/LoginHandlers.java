package com.mikey.auction.socket.Handlers;

import java.io.PrintWriter;
import com.mikey.auction.database.UserDAO;
import com.mikey.auction.user.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

public class LoginHandlers {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, t, ctx) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, t, ctx) -> LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .create();

    public static void handleLogin(String message, PrintWriter out) {
        try {
            String[] parts = message.split("\\|");
            if (parts.length >= 3) {
                String username = parts[1].trim();
                String password = parts[2].trim();

                User user = UserDAO.getInstance().login(username, password);

                if (user != null) {
                    out.println("SUCCESS");
                    out.println(gson.toJson(user)); 
                    System.out.println("[LOGIN SUCCESS] User: " + username);
                } else {
                    out.println("FAIL");
                    System.out.println("[LOGIN FAIL] User: " + username);
                }
            }
        } catch (Exception e) {
            out.println("ERROR");
            e.printStackTrace();
        }
        out.flush();
    }
}