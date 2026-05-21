package com.mikey.auction.manager;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.Test;

public class SocketPerformanceTest {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888; // Đổi lại đúng Port mà AuctionServer của bạn đang chạy

    @Test
    public void testServerSpamConnections() {
        List<Socket> activeConnections = new ArrayList<>();
        int maxConnectionsToTest = 50; // Giả lập 50 thiết bị mở ứng dụng kết nối vào Server cùng lúc

        assertDoesNotThrow(() -> {
            try {
                for (int i = 0; i < maxConnectionsToTest; i++) {
                    // Mở kết nối Socket thẳng tới Server thật/hoặc Server đang chạy test
                    Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
                    activeConnections.add(socket);
                }
            } finally {
                // Đóng toàn bộ socket sau khi test xong để tránh rò rỉ (leak) tài nguyên của máy
                for (Socket s : activeConnections) {
                    if (s != null && !s.isClosed()) {
                        s.close();
                    }
                }
            }
        }, "Server phải chịu được 50 kết nối đồng thời mà không được ném ra lỗi 'Connection refused'");
    }
}