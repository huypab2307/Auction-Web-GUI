package com.mikey.auction.socket.Handlers;

import java.io.PrintWriter;
import com.google.gson.Gson;
import com.mikey.auction.database.UserDAO;

public class UserHandler {
    private static final Gson gson = new Gson();

    public static void handleUser(String message, PrintWriter out) {
        try {
            String[] parts = message.split("\\|");
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
                    // Chèn logic DAO xóa tài khoản ở đây nếu có
                    result = true;
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