package com.mikey.auction.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mikey.auction.database.AuctionDAO;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.manager.NotificationManager;
import com.mikey.auction.socket.Handlers.ItemHandler;
import com.mikey.auction.socket.Handlers.LoginHandlers;
import com.mikey.auction.socket.Handlers.RegisterHandlers;
import com.mikey.auction.socket.Handlers.UserHandler;
import com.mikey.auction.user.Bidder;

public class AuctionServer {
    private static Set<PrintWriter> clientWriters = Collections.synchronizedSet(new HashSet<>());
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(20);

    public static void addClientWriter(PrintWriter out) { clientWriters.add(out); }
    
    public static void removeClientWriter(PrintWriter out) { clientWriters.remove(out); }

    public static void broadcast(String msg) {
        synchronized (clientWriters) {
            for (PrintWriter writer : clientWriters) {
                writer.println(msg);
                writer.flush();
            }
        }
    }

    public static void main(String[] args) {
    int port = 12345;
    try (ServerSocket serverSocket = new ServerSocket(port)) {
        System.out.println("========================================");
        System.out.println("AUCTION SERVER is started on port: " + port);
        System.out.println("Waiting for clients...");
        System.out.println("========================================");

        // 👉 KÍCH HOẠT HỆ THỐNG LẬP LỊCH TỰ ĐỘNG KHÔI PHỤC KHI KHỞI ĐỘNG SERVER
        com.mikey.auction.manager.AuctionScheduler.getInstance().loadActiveAuctionsOnStartup();

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("[NEW CONNECTION] " + clientSocket.getInetAddress());
            threadPool.execute(new ClientHandler(clientSocket)); //
        }
    } catch (IOException e) {
        System.err.println("Server Error: " + e.getMessage()); //
    } finally {
        threadPool.shutdown(); //
        // Đóng an toàn luồng đếm ngược khi Server sập
        com.mikey.auction.manager.AuctionScheduler.getInstance().shutdown();
    }
}
}

class ClientHandler implements Runnable {
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
            AuctionServer.addClientWriter(out);

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("[RECEIVED]: " + message);

                // Phân luồng các Request
                if (message.startsWith("LOGIN|")) {
                    LoginHandlers.handleLogin(message, out);
                } else if (message.startsWith("REGISTER|")) {
                    RegisterHandlers.handleRegister(message, out);
                } else if (message.startsWith("AUCTION|")) {
                    AuctionHandler.handleAuction(message, out);
                } else if (message.startsWith("NOTIFICATION|")) {
                    NotificationHandlers.handleNotification(message, out);
                } else if (message.startsWith("ITEM|")) {           // <--- THÊM DÒNG NÀY
                    ItemHandler.handleItem(message, out);
                } else if (message.startsWith("USER|")) {
                    UserHandler.handleUser(message, out);
                } else if (message.startsWith("BID|") || message.startsWith("CHAT|")) {
                    AuctionServer.broadcast(message);
                }
            }
        } catch (IOException e) {
            System.out.println("[DISCONNECTED] Một client đã thoát.");
        } finally {
            if (out != null) AuctionServer.removeClientWriter(out);
            try { if (socket != null) socket.close(); } catch (IOException e) { e.printStackTrace(); }
        }
    }

    // 👉 ĐÃ SỬA: Thêm lệnh Phát sóng (Broadcast) sau khi chốt đơn
    public boolean placeBid(Bidder bidder, AuctionInfo auctionInfo, double oldPrice) {
        AuctionDAO auctionDAO = AuctionDAO.getInstance();
        try (Connection connection = auctionDAO.getConnect()) {
            connection.setAutoCommit(false);
            auctionDAO.updateAuction(connection, auctionInfo, bidder.getId(), oldPrice);
            auctionDAO.updateTransaction(connection, auctionInfo, bidder.getId());
            
            connection.commit(); // Chốt đơn cho người đặt giá thủ công

            // Máy sẽ tự động kiểm tra xem có ai muốn nâng giá đè lên không
            auctionDAO.triggerAutoBids(auctionInfo.getId());

            // LẤY DỮ LIỆU MỚI NHẤT & CHUYỂN THÀNH JSON
            AuctionInfo freshInfo = auctionDAO.searchAuctionById(auctionInfo.getId());
            if (freshInfo != null) {
                com.google.gson.Gson gson = new com.google.gson.GsonBuilder()
                    .registerTypeAdapter(java.time.LocalDateTime.class, (com.google.gson.JsonSerializer<java.time.LocalDateTime>) (src, t, ctx) -> new com.google.gson.JsonPrimitive(src.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .create();
                    
                // PHÁT SÓNG CHO TẤT CẢ CÁC MÁY KHÁC ĐỂ ÉP NHẢY SỐ (Broadcast)
                com.mikey.auction.socket.AuctionServer.broadcast("AUCTION|UPDATE_PRICE|" + gson.toJson(freshInfo));
            }

            return NotificationManager.getInstance().notiAll(auctionInfo, bidder);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    // 👉 ĐÃ SỬA: Phát sóng ngay cả khi Auto Bid tự động làm nhảy giá
    public boolean registerAutoBid(com.mikey.auction.dto.AutoBidInfo info) {
        boolean success = AuctionDAO.getInstance().registerAutoBid(info);
        if (success) {
            AuctionDAO.getInstance().triggerAutoBids(info.getAuctionId());
            
            // Lấy giá mới nhất và phát sóng cho toàn bộ Client
            AuctionInfo freshInfo = AuctionDAO.getInstance().searchAuctionById(info.getAuctionId());
            if (freshInfo != null) {
                com.google.gson.Gson gson = new com.google.gson.GsonBuilder()
                    .registerTypeAdapter(java.time.LocalDateTime.class, (com.google.gson.JsonSerializer<java.time.LocalDateTime>) (src, t, ctx) -> new com.google.gson.JsonPrimitive(src.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .create();
                com.mikey.auction.socket.AuctionServer.broadcast("AUCTION|UPDATE_PRICE|" + gson.toJson(freshInfo));
            }
        }
        return success;
    }
}