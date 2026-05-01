package Socket;

import com.google.gson.Gson;
import Database.UserDAO;
import User.User;
import java.io.*;
import java.net.*;
import java.util.*;

public class AuctionServer {
    private static Set<PrintWriter> clientWriters = new HashSet<>();
    private static Gson gson = new Gson(); // Khởi tạo Gson dùng chung

    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(12345);
        System.out.println("Server is started on port 12345...");

        try {
            while (true) {
                Socket s = ss.accept();
                System.out.println("New client connected!");
                new ClientHandler(s).start();
            }
        } catch(Exception e) {} finally {
            ss.close();
        }
    }

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

                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                String message;
                while ((message = br.readLine()) != null) {
                    System.out.println("Received: " + message);

                    // Xử lý logic ĐĂNG NHẬP
                    if (message.startsWith("LOGIN|")) {
                        handleLogin(message);
                    } 
                    // Xử lý CHAT hoặc ĐẤU GIÁ (Broadcast cho tất cả)
                    //else {
                       // broadcast(message);}
                }
            } catch (IOException e) {
                System.out.println("A client disconnected.");
            } finally {
                cleanUp();
            }
        }

        private void handleLogin(String message) {
            try {
                String[] parts = message.split("\\|");
                if (parts.length >= 3) {
                    String userStr = parts[1].trim();
                    String passStr = parts[2].trim();

                    // Gọi DAO để kiểm tra Database
                    UserDAO userDAO = UserDAO.getInstance();
                    User user = userDAO.login(userStr, passStr);

                    if (user != null) {
                        out.println("SUCCESS");
                        // Gửi kèm thông tin User dưới dạng JSON
                        out.println(gson.toJson(user)); 
                        System.out.println("Login Successful for: " + userStr);
                    } else {
                        out.println("FAIL");
                        System.out.println("Login Failed for: " + userStr);
                    }
                }
            } catch (Exception e) {
                out.println("ERROR");
                e.printStackTrace();
            }
        }

//        private void broadcast(String msg) {
  //          synchronized (clientWriters) {
      //          for (PrintWriter writer : clientWriters) {
          //          writer.println(msg);
           //     }
          //  }
     //   }

        private void cleanUp() {
            if (out != null) {
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }
            }
            try { socket.close(); } catch (IOException e) {}
        }
    }
}