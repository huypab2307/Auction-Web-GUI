package com.mikey.auction.manager;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.mikey.auction.auction.Auction;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.items.Electronics;
import com.mikey.auction.items.Item;
import com.mikey.auction.items.ItemType;

public class AuctionTimeoutSchedulerTest {

    @Test
    public void testAuctionAutoCloseOnTimeout() throws InterruptedException {
        // 1. Tạo một món đồ đấu giá kết thúc cực nhanh (sau 1 giây kể từ bây giờ)
        Item item = new Electronics("Mô hình Naruto", "Hàng giới hạn", ItemType.ELECTRONICS, 1, -1, "path");
        double startPrice = 500000;
        double stepPrice = 50000;
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.now().plusSeconds(1); // Kết thúc sau 1 giây

        AuctionManager.getInstance().uploadItem(item, startPrice, stepPrice, startTime, endTime);

        // Lấy phiên đấu giá vừa tạo
        var auctions = AuctionManager.getInstance().auctionList();
        AuctionInfo latestInfo = auctions.get(auctions.size() - 1);
        
        // Giả lập người dùng ID 99 đặt giá cao nhất hiện tại trước khi hết giờ
        latestInfo.setCurPrice(550000);
        AuctionManager.getInstance().updateAuction(latestInfo);

        // 2. Ép hệ thống đợi 1.5 giây để phiên đấu giá chính thức QUÁ HẠN
        Thread.sleep(1500);

        // 3. Gọi hàm xử lý quét hoặc lấy lại thông tin phiên đấu giá từ hệ thống
        Auction actualAuction = AuctionManager.getInstance().findAuction(latestInfo.getId());

        // 4. Khẳng định (Assert): 
        // Hệ thống của bạn cần có cơ chế (hoặc biến trạng thái như isFinished, status...) 
        // để chứng minh phiên này đã đóng và không cho đặt giá nữa.
        assertTrue(actualAuction.isFinished(), "Phiên đấu giá đáng lẽ phải tự động kết thúc sau khi hết giờ");
        assertEquals(99, actualAuction.getHighestBidderId(), "Người dùng ID 99 phải là người chiến thắng chung cuộc");
    }
}