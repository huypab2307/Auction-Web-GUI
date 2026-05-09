package com.mikey.auction.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mikey.auction.database.AuctionDAO;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.socket.Handlers.LoginHandlers;
import com.mikey.auction.socket.Handlers.RegisterHandlers;

public class AuctionServer {
    // Danh sách lưu trữ tất cả các Client đang kết nối (Thread-safe)
    private static Set<PrintWriter> clientWriters = Collections.synchronizedSet(new HashSet<>());
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(20); // Tối đa 20 client cùng lúc

    public static  void addClientWriter(PrintWriter out) {
        clientWriters.add(out);
    }
    public static  void broadcast(String msg) {
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println(msg);
                }
            }
        }
    public static void removeClientWriter(PrintWriter out) {
        clientWriters.remove(out);
    }
   

    public static void main(String[] args) {
        int port = 12345;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("========================================");
            System.out.println("AUCTION SERVER is started on port: " + port);
            System.out.println("Waiting for clients...");
            System.out.println("========================================");

            while (true) {
                startAuctionTimer(); // Bắt đầu timer kiểm tra phiên đấu giá (nếu chưa chạy)
                // Chấp nhận kết nối từ Client
                Socket clientSocket = serverSocket.accept();
                System.out.println("[NEW CONNECTION] " + clientSocket.getInetAddress());
                // Mỗi Client là một luồng riêng
                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Server Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }

    }

    public static void startAuctionTimer() {
    new Thread(() -> {
        while (true) {
            try {
                // Kiểm tra danh sách phiên đấu giá từ AuctionDAO[cite: 6]
                ArrayList<AuctionInfo> auctions = AuctionDAO.getInstance().getAllAuctions();
                LocalDateTime now = LocalDateTime.now();

                for (AuctionInfo a : auctions) {
                    // Nếu thời gian kết thúc nhỏ hơn hiện tại và vẫn đang mở (status = 1)
                    //if (a.getEndTime().isBefore(now) && a.getStatus() == 1) {
                        // 1. Gọi hàm setStatus(a.getId(), 0) - (Dương/Huy sẽ viết)
                        // 2. Gửi thông báo cho người thắng cuộc qua NotificationManager
                        System.out.println("Phiên " + a.getId() + " đã hết hạn. Đang đóng...");
                    //}
                }
                Thread.sleep(1000); // Kiểm tra lại sau mỗi 1 giây
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }).start();
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

                // Thêm vào danh sách để Broadcast
                AuctionServer.addClientWriter(out);

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("[RECEIVED]: " + message);

                    // Xử lý các lệnh từ Client
                    if (message.startsWith("LOGIN|")) {
                        LoginHandlers.handleLogin(message, out);
                    }else if (message.startsWith("REGISTER|")) {
                        RegisterHandlers.handleRegister(message, out);
                    } else if (message.startsWith("AUCTION")){
                       AuctionHandler.handleAuction(message, out);
                    } else if (message.startsWith("NOTIFICATION")) {
                        NotificationHandlers.handleNotification(message, out);
                    } else if (message.startsWith("BID|") || message.startsWith("CHAT|")) {
                        // Nếu là đặt giá hoặc chat thì gửi cho tất cả
                        AuctionServer.broadcast(message);
                    }
                }
            } catch (IOException e) {
                System.out.println("[DISCONNECTED] Một client đã thoát.");
            } finally {
                cleanUp();
            }
        }

      public void cleanUp() {
            if (out != null) {
                AuctionServer.removeClientWriter(out);
            }
            try {
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("[CLEANED UP] Tài nguyên của client đã được giải phóng.");
        }   

        
    }
    