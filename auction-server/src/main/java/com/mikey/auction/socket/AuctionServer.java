package com.mikey.auction.socket;

import java.io.*;
import java.net.*;
import java.util.*;

public class AuctionServer {
    // Danh sách để lưu trữ tất cả các Client đang kết nối
    private static Set<PrintWriter> clientWriters = new HashSet<>();

    public static void main(String[] args) throws Exception {
        ServerSocket ss = new ServerSocket(12345);
        System.out.println("Server is started...");

        try {
            while (true) {
                // Luôn lắng nghe kết nối mới
                Socket s = ss.accept();
                System.out.println("New client connected!");

                // Tạo một luồng (Thread) mới để xử lý Client này
                new ClientHandler(s).start();
            }
        } finally {
            ss.close();
        }
    }

    // Lớp xử lý từng Client riêng biệt
    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader br;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Lưu output của client này vào danh sách chung
                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                String message;
                while ((message = br.readLine()) != null) {
                    System.out.println("Received: " + message);
                    
                    // Gửi tin nhắn này tới TẤT CẢ các client khác (Broadcast)
                    synchronized (clientWriters) {
                        for (PrintWriter writer : clientWriters) {
                            writer.println(message);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("A client disconnected.");
            } finally {
                // Khi client thoát, xóa khỏi danh sách và đóng socket
                if (out != null) {
                    synchronized (clientWriters) {
                        clientWriters.remove(out);
                    }
                }
                try { socket.close(); } catch (IOException e) {}
            }
        }
    }
}