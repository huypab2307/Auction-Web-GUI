package com.mikey.auction.socket.Handlers;

import java.io.PrintWriter;
import com.mikey.auction.database.UserDAO;

public class RegisterHandlers {
    public static void handleRegister(String message, PrintWriter out) {
        try {
            String[] parts = message.split("\\|");
            if (parts.length >= 3) {
                String username = parts[1].trim();
                String password = parts[2].trim();

                boolean success = UserDAO.getInstance().register(username, password);

                if (success) {
                    out.println("SUCCESS");
                    System.out.println("[REGISTER SUCCESS] User: " + username);
                } else {
                    out.println("FAIL");
                    System.out.println("[REGISTER FAIL] User: " + username);
                }
            }
        } catch (Exception e) {
            out.println("ERROR");
            e.printStackTrace();
        }
        out.flush();
    }
}