package com.mikey.auction.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader; 
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;
import com.mikey.auction.socket.Handlers.LoginHandlers;
import com.mikey.auction.socket.Handlers.RegisterHandlers;

public class AuctionServer {
    // Danh sách lưu trữ tất cả các Client đang kết nối (Thread-safe)
    private static Set<PrintWriter> clientWriters = Collections.synchronizedSet(new HashSet<>());
    private static Gson gson = new Gson(); 

    public static void main(String[] args) {
        int port = 12345;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("========================================");
            System.out.println("AUCTION SERVER is started on port: " + port);
            System.out.println("Waiting for clients...");
            System.out.println("========================================");

            while (true) {
                // Chấp nhận kết nối từ Client
                Socket clientSocket = serverSocket.accept();
                System.out.println("[NEW CONNECTION] " + clientSocket.getInetAddress());

                // Mỗi Client là một luồng riêng
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            System.err.println("Server Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Thêm vào danh sách để Broadcast
                clientWriters.add(out);

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("[RECEIVED]: " + message);

                    // Xử lý các lệnh từ Client
                    if (message.startsWith("LOGIN|")) {
                        LoginHandlers.handleLogin(message, out);
                    }else if (message.startsWith("REGISTER|")) {
                        RegisterHandlers.handleRegister(message, out);
                    } else if (message.startsWith("BID|") || message.startsWith("CHAT|")) {
                        // Nếu là đặt giá hoặc chat thì gửi cho tất cả
                        broadcast(message);
                    }
                }
            } catch (IOException e) {
                System.out.println("[DISCONNECTED] Một client đã thoát.");
            } finally {
                cleanUp();
            }
        }

       

        private void broadcast(String msg) {
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println(msg);
                }
            }
        }

        private void cleanUp() {
            if (out != null) {
                clientWriters.remove(out);
            }
            try {
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("[CLEANED UP] Tài nguyên của client đã được giải phóng.");
        }
    }
}