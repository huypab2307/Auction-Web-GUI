package com.mikey.auction.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import com.mikey.auction.auction.Auction;

public class DatabaseCrashRecoveryTest {

    @Test
    public void testSystemRecoveryAfterCrash() {
        // 1. Giả lập tình huống: Trước khi sập, hệ thống đang có một phiên đấu giá ID 555 hoạt động.
        // Ở đây ta gọi hàm khởi tạo lại hệ thống của AuctionManager (giống như lúc server vừa restart)
        // Ví dụ: AuctionManager.getInstance().initializeFromDatabase();
        
        // 2. Lấy thông tin phiên đấu giá ID 555 ra sau khi hệ thống vừa "hồi sinh"
        Auction recoveredAuction = AuctionManager.getInstance().findAuction(555);

        // 3. Khẳng định: Phiên đấu giá phải tồn tại và giữ nguyên các trạng thái quan trọng
        assertNotNull(recoveredAuction, "Hệ thống phải khôi phục được phiên đấu giá ID 555 từ DB sau khi khởi động lại");
        
        // Giả sử trước khi sập giá hiện tại là 1,000,000 VND
        assertEquals(1000000, recoveredAuction.getCurPrice(), "Giá hiện tại của phiên đấu giá phải được giữ nguyên");
        assertEquals("OPEN",recoveredAuction.getStatus(), "Phiên đấu giá chưa hết hạn trước khi sập thì sau khi restart vẫn phải tiếp tục chạy");
    }
}