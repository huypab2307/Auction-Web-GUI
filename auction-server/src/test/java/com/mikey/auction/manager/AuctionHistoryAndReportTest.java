package com.mikey.auction.manager;

import org.junit.jupiter.api.Test;

import com.mikey.auction.dto.AuctionInfo;

public class AuctionHistoryAndReportTest {

    @Test
    public void testAuctionHistoryTracking() {
        int auctionId = 123;
        
        // 1. Giả lập 2 lượt đặt giá liên tiếp từ 2 người dùng khác nhau cho phòng 123
        AuctionInfo bid1 = new AuctionInfo();
        bid1.setId(auctionId);
        bid1.setCurPrice(500000);
        bid1.setBidderId(1); // Người dùng 1 đặt 500k
        AuctionManager.getInstance().updateAuction(bid1);

        AuctionInfo bid2 = new AuctionInfo();
        bid2.setId(auctionId);
        bid2.setCurPrice(600000);
        bid2.setBidderId(2); // Người dùng 2 đặt 600k
        AuctionManager.getInstance().updateAuction(bid2);

        // 2. Gọi hàm lấy lịch sử đấu giá của phòng này từ AuctionManager
        // (Tên hàm tùy thuộc vào cách bạn thiết kế, ví dụ: getHistory, getLogs...)
        List<BidLog> history = AuctionManager.getInstance().getAuctionHistory(auctionId);

        // 3. Khẳng định (Assert) các điều kiện:
        // - Lịch sử không được null và phải ghi nhận đủ 2 lượt đặt giá.
        // - Lượt đặt giá sau phải có số tiền lớn hơn lượt trước.
        assertNotNull(history, "Lịch sử đấu giá không được null");
        assertEquals(2, history.size(), "Phải ghi nhận đúng 2 lượt đặt giá trong lịch sử");
        assertTrue(history.get(1).getPrice() > history.get(0).getPrice(), "Lịch sử phải sắp xếp theo thứ tự tăng dần của giá");
    }
}