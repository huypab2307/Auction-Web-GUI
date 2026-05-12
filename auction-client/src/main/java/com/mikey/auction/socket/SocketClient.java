package com.mikey.auction.socket;

import javafx.application.Platform;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClient {
    private static SocketClient instance;
    private Socket socket;
    private BufferedReader in;
    private SocketListener currentListener;

    private SocketClient() {}

    public static SocketClient getInstance() {
        if (instance == null) {
            instance = new SocketClient();
        }
        return instance;
    }

    public void connect(String host, int port) throws IOException {
        if (socket == null || socket.isClosed()) {
            socket = new Socket(host, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            RequestHandler.getInstance().setPrintWriter(out);
            new Thread(this::listenToServer).start();
        }
    }

    public void setListener(SocketListener listener) {
        this.currentListener = listener;
    }

    private void listenToServer() {
        try {
            String response;
            while ((response = in.readLine()) != null) {
                System.out.println("[CLIENT RECEIVED]: " + response);
                
                // 1. Server chỉ trả về trạng thái đơn lẻ (Dành cho Đăng ký hoặc Báo lỗi)
                if (response.equals("SUCCESS") || response.equals("FAIL") || response.equals("ERROR")) {
                    final String status = response;
                    Platform.runLater(() -> {
                        if (currentListener != null) {
                            currentListener.onResponseReceived("AUTH", status, "");
                        }
                    });
                    continue;
                }

                // 2. Server trả về độc lập một chuỗi JSON (Dành cho Đăng nhập thành công)
                if (response.trim().startsWith("{")) {
                    final String json = response;
                    Platform.runLater(() -> {
                        if (currentListener != null) {
                            // Báo cho LoginController biết đây là dữ liệu JSON
                            currentListener.onResponseReceived("AUTH", "LOGIN_SUCCESS", json);
                        }
                    });
                    continue;
                }

                // 3. Xử lý các lệnh chuẩn khác (CATEGORY|ACTION|JSON)
                String[] parts = response.split("\\|", 3);
                if (parts.length >= 2) {
                    final String category = parts[0];
                    final String action = parts[1];
                    final String jsonData = (parts.length > 2) ? parts[2] : "";

                    Platform.runLater(() -> {
                        if (currentListener != null) {
                            currentListener.onResponseReceived(category, action, jsonData);
                        }
                    });
                }
            }
        } catch (IOException e) {
            System.err.println("Cảnh báo: Mất kết nối với Server!");
        }
    }
}