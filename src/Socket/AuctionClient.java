package Socket;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class AuctionClient {
    public static void main(String[] args) {
        String ip = "10.11.221.99"; 
        int port = 12345;

        try {
            Socket socket = new Socket(ip, port);
            System.out.println("Connected to server!");

            // CThread riêng để nhận dữ liệu từ Server bất cứ lúc nào
            new Thread(() -> {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String response;
                    while ((response = in.readLine()) != null) {
                        // Xóa dòng "Client: " đang gõ dở để in tin nhắn mới cho đẹp (tùy chọn)
                        System.out.println("\n[Message from Server/Others]: " + response);
                        System.out.print("Client: "); 
                    }
                } catch (IOException e) {
                    System.out.println("Lost connection to server.");
                }
            }).start();

            // --- PHẦN 2: LUỒNG GỬI TIN NHẮN (Luồng chính) ---
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in);
            
            while (true) {
                // Nhập tin nhắn từ bàn phím
                String message = scanner.nextLine(); 
                
                out.println(message); // Gửi đi

                if (message.equalsIgnoreCase("exit")) {
                    socket.close();
                    break;
                }
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}