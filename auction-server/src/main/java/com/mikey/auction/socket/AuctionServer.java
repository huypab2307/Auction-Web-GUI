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
import com.mikey.auction.socket.Handlers.*;

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

            startAuctionTimer(); 

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[NEW CONNECTION] " + clientSocket.getInetAddress());
                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Server Error: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }

    public static void startAuctionTimer() {
        new Thread(() -> {
            while (true) {
                try {
                    ArrayList<AuctionInfo> auctions = AuctionDAO.getInstance().getAllAuctions();
                    LocalDateTime now = LocalDateTime.now();

                    for (AuctionInfo a : auctions) {
                        // Thêm logic cập nhật trạng thái nếu cần
                    }
                    Thread.sleep(60000); // Tối ưu: Kiểm tra 1 phút/lần thay vì 1 giây/lần
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
}