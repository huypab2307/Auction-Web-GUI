package com.mikey.auction.manager;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.mikey.auction.auction.Auction;
import com.mikey.auction.database.AuctionDAO;
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

        // Thực hiện tạo cuộc đấu giá
        assertDoesNotThrow(() -> {
            AuctionManager.getInstance().uploadItem(item, startPrice, stepPrice, startTime, endTime);
        }, "Tạo auction với thời gian kết thúc ngắn không được ném exception");       

        // Lấy phiên đấu giá vừa tạo
        var auctions = AuctionManager.getInstance().auctionList();
        AuctionInfo latestInfo = auctions.get(auctions.size() - 1);
        
        // Giả lập người dùng ID 99 đặt giá cao nhất hiện tại trước khi hết giờ
        latestInfo.setCurPrice(550000);
        AuctionDAO.getInstance().updateAuction(latestInfo);

        // 2. Ép hệ thống đợi 1.5 giây để phiên đấu giá chính thức QUÁ HẠN
        Thread.sleep(1500);

        // 3. Gọi hàm xử lý quét hoặc lấy lại thông tin phiên đấu giá từ hệ thống
        Auction actualAuction = AuctionManager.getInstance().findAuction(latestInfo.getId());

        // 4. Khẳng định: Phiên đấu giá phải được đánh dấu là đã kết thúc và giá cuối cùng là 550,000 VND
        assertEquals("CLOSED", actualAuction.getStatus(), "Phiên đấu giá phải được đánh dấu là đã kết thúc sau khi quá hạn");
        assertEquals(550000, actualAuction.getCurPrice(), "Giá cuối cùng của phiên đấu giá phải là 550,000 VND sau khi hết giờ");
    }
}