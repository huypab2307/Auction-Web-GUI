package com.mikey.auction.socket.Handlers;

import java.io.PrintWriter;

import com.google.gson.Gson;
import com.mikey.auction.database.UserDAO;

public class RegisterHandlers {
    private static Gson gson = new Gson();
     public static void handleRegister(String message, PrintWriter out) {
            try {
                String[] parts = message.split("\\|");
                if (parts.length >= 3) {
                    String username = parts[1].trim();
                    String password = parts[2].trim();

                    // Kiểm tra Database thông qua DAO
                    UserDAO userDAO = UserDAO.getInstance();
                    boolean success = userDAO.register(username, password);

                    if (success) {
                        // Gửi phản hồi SUCCESS và chuỗi JSON của User
                        out.println("SUCCESS");
                        System.out.println("[Register SUCCESS] User: " + username);
                    } else {
                        out.println("FAIL");
                        System.out.println("[Register FAIL] User: " + username);
                    }
                }
            } catch (Exception e) {
                out.println("ERROR");
                e.printStackTrace();
            }
        }
}
