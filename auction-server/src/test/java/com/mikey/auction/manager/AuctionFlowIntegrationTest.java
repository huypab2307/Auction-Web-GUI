package com.mikey.auction.manager;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.mikey.auction.auction.Auction;
import com.mikey.auction.dto.AutoBidInfo;
import com.mikey.auction.items.Electronics;
import com.mikey.auction.items.Item;
import com.mikey.auction.items.ItemType;

public class AuctionFlowIntegrationTest {

    @Test
    public void testCompleteAuctionAndAutoBidFlow() {
        // 1. Chuẩn bị dữ liệu và Upload một Item thật lên hệ thống
        Item item = new Electronics("iPad Pro", "M2 Chip", ItemType.ELECTRONICS, 1, -1, "path");
        double price = 15000000;
        double stepPrice = 200000;
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.now().plusHours(5);

        // Thực hiện tạo cuộc đấu giá
        assertDoesNotThrow(() -> {
            AuctionManager.getInstance().uploadItem(item, price, stepPrice, startTime, endTime);
        });

        // 2. Lấy danh sách để tìm cuộc đấu giá vừa tạo (Giả định nó nằm cuối danh sách)
        var auctions = AuctionManager.getInstance().auctionList();
        assertNotNull(auctions);
        assertFalse(auctions.isEmpty(), "Danh sách không được rỗng sau khi đã tạo auction");
        
        // Lấy auction vừa được thêm vào hệ thống
        Auction latestAuction = auctions.get(auctions.size() - 1);
        int auctionId = latestAuction.getId(); // Lấy ID thật do hệ thống sinh ra thay vì dùng số lụi 99999

        // 3. Tiến hành đăng ký Auto-Bid trực tiếp lên cuộc đấu giá thật này
        // Giả lập Người dùng ID 5, đặt Auto-bid tối đa 18 triệu cho Auction vừa tìm thấy
        AutoBidInfo autoBidInfo = new AutoBidInfo(5, auctionId, 18000000);
        
        assertDoesNotThrow(() -> {
            AuctionManager.getInstance().registerAutoBid(autoBidInfo);
        }, "Đăng ký Auto-bid trên một Auction có thật trong hệ thống thì không được phép lỗi");
    }
}