package com.mikey.auction.manager;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class SocketTimeoutAndLeakTest {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888; // Đổi lại đúng Port của AuctionServer của bạn

    @Test
    public void testServerShouldDisconnectDeadClient() throws IOException, InterruptedException {
        // 1. Giả lập một Client kết nối vào Server đấu giá
        Socket deadClient = new Socket(SERVER_HOST, SERVER_PORT);
        
        // Thiết lập timeout đọc dữ liệu trên chính client để tránh bài test bị treo vô hạn nếu server lỗi
        deadClient.setSoTimeout(3000); 

        // 2. Giả lập hành vi "Treo máy": Client kết nối thành công nhưng im lặng hoàn toàn,
        // không gửi bất kỳ gói tin nào (không Login, không đặt giá, không ngắt kết nối).
        // Chúng ta sẽ để im trong 4 giây.
        Thread.sleep(4000);

        // 3. Khẳng định (Assert): 
        // Một Server thiết kế tốt phải có cơ chế Timeout (ví dụ: sau 3 giây im lặng là tự Kick client).
        // Lúc này, nếu chúng ta cố tình gửi dữ liệu hoặc đọc dữ liệu, Server phải báo đã đóng kết nối.
        
        boolean isConnectionClosedByServer = false;
        try {
            int data = deadClient.getInputStream().read();
            if (data == -1) {
                isConnectionClosedByServer = true; // -1 nghĩa là Server đã chủ động đóng Socket này rồi
            }
        } catch (SocketTimeoutException e) {
            // Nếu bị timeout nghĩa là Server vẫn đang cắm cụi đợi -> Server chưa tự ngắt Client chết -> FAIL!
            isConnectionClosedByServer = false;
        } catch (IOException e) {
            // Nếu ném ra lỗi Connection reset nghĩa là Server đã hủy kết nối -> PASS!
            isConnectionClosedByServer = true;
        }

        assertTrue(isConnectionClosedByServer, 
            "Server phải tự động đóng kết nối (Kick) các Client treo máy sau một khoảng thời gian im lặng để tránh rò rỉ tài nguyên!");
        
        // Dọn dẹp socket test
        if (!deadClient.isClosed()) {
            deadClient.close();
        }
    }
}