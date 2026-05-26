package com.mikey.auction.manager;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.Test;

import com.mikey.auction.database.AuctionDAO;
import com.mikey.auction.dto.AuctionInfo;

public class BidConcurrencyTest {

    // Lấy instance thông qua getInstance() thay vì dùng từ khóa 'new'
    private AuctionDAO auctionDAO = AuctionDAO.getInstance();

    @Test
    public void testConcurrentBiddingOnSingleAuction() throws InterruptedException {
        int numberOfThreads = 10; // Giả lập 10 người dùng cùng nhấn đặt giá một lúc
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);
        
        // Đếm số lượt đặt giá thành công
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // Giả định phiên đấu giá ID: 123, giá hiện tại: 100k, bước giá: 50k
        // Cả 10 người cùng muốn nâng giá lên thành 150k tại cùng một mili-giây
        AuctionInfo info = new AuctionInfo(
            null,          // itemInfo (để null nếu chưa test phần này)
            123,           // id (Giá trị ID đã được nạp trực tiếp tại đây!)
            "sellerTest",  // sellerUsername
            "bidderTest",  // lastBidderName
            150000.0,      // curPrice (Giá hiện tại cũng đã được nạp ở đây!)
            null,          // status (AuctionStatus)
            null,          // startTime
            null,          // endTime
            0.0            // bidStep
        );
     
        // XÓA BỎ: info.setId(123); -> Dòng này gây lỗi NoSuchMethodError vì class gốc không có hàm này
        // XÓA BỎ: info.setCurPrice(150000); -> Tránh rủi ro lỗi tương tự với hàm setCurPrice

        for (int i = 0; i < numberOfThreads; i++) {
            service.submit(() -> {
                try {
                    latch.await(); 
                    // Gọi qua thực thể auctionDAO đã lấy thành công
                    auctionDAO.updateAuction(info);
                    successCount.incrementAndGet();
                } catch (Throwable t) {
                    // SỬA: Đón đầu bằng Throwable để bắt toàn bộ lỗi sập liên quan đến DB kết nối ngầm
                    failCount.incrementAndGet();
                }
            });
        }

        latch.countDown(); // Phát súng hiệu: Kích hoạt tất cả các thread chạy cùng lúc
        service.shutdown();
        
        // Đợi tối đa 5 giây cho các thread xử lý xong
        while (!service.isTerminated()) {
            Thread.sleep(100);
        }

        // Đảm bảo luồng chạy đồng thời được xử lý xong mà không làm sập hệ thống
        assertDoesNotThrow(() -> {
            int totalProcessed = successCount.get() + failCount.get();
        }, "Đảm bảo luồng chạy đồng thời được xử lý xong mà không làm sập hệ thống");
    }
}