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

                // Bước 1 đã làm: Hàm login giờ sẽ trả về user kể cả khi bị khóa (để lấy status)
                User user = UserDAO.getInstance().login(username, password);

                if (user != null) {
                    // 👉 BƯỚC 2 CHÍNH LÀ ĐÂY: Chốt chặn kiểm tra trạng thái khóa
                    if ("BANNED".equals(user.getStatus())) {
                        out.println("AUTH|BANNED|Account is banned"); 
                        System.out.println("[LOGIN BLOCKED - BANNED] User: " + username);
                    } else {
                        // Trạng thái ACTIVE -> Cho phép đăng nhập, trả về chuẩn AUTH|LOGIN_SUCCESS|json
                        out.println("AUTH|LOGIN_SUCCESS|" + gson.toJson(user)); 
                        System.out.println("[LOGIN SUCCESS] User: " + username);
                    }
                } else {
                    // Sai tài khoản hoặc mật khẩu thật sự (UserDAO trả về null)
                    out.println("AUTH|FAIL|Wrong credentials");
                    System.out.println("[LOGIN FAIL] User: " + username);
                }
            }
        } catch (Exception e) {
            out.println("AUTH|ERROR|Server exception");
            e.printStackTrace();
        }
        out.flush();
    }
}