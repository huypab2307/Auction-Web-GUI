package com.mikey.auction.socket.Handlers;

import java.io.PrintWriter;

import com.google.gson.Gson;
import com.mikey.auction.database.UserDAO;
import com.mikey.auction.user.User;

public class LoginHandlers {
    private static Gson gson = new Gson();
     public static void handleLogin(String message, PrintWriter out) {
            try {
                String[] parts = message.split("\\|");
                if (parts.length >= 3) {
                    String username = parts[1].trim();
                    String password = parts[2].trim();

                    // Kiểm tra Database thông qua DAO
                    User user = UserDAO.getInstance().login(username, password);

                    if (user != null) {
                        // Gửi phản hồi SUCCESS và chuỗi JSON của User
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
        }
}
