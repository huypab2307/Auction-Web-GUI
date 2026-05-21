package com.mikey.auction.manager;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.mikey.auction.database.AuctionDAO;
import com.mikey.auction.dto.AuctionInfo;

public class BidConcurrencyTest {

    private Object AuctionDAO;

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
        AuctionInfo info = new AuctionInfo();
        info.setId(123);
        info.setCurPrice(150000); 

        for (int i = 0; i < numberOfThreads; i++) {
            service.submit(() -> {
                try {
                    latch.await(); 
                    ((AuctionDAO) AuctionDAO).updateAuction(info);
                    successCount.incrementAndGet();
                } catch (Exception e) {
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

        // Khẳng định (Assert): Hệ thống chuẩn thì chỉ ĐƯỢC PHÉP có đúng 1 người đặt giá thành công,
        // 9 người còn lại phải thất bại vì giá đã bị thay đổi/khóa.
        assertEquals(1, successCount.get(), "Chỉ được phép có duy nhất 1 người đặt giá thành công tại một mức giá");
        assertEquals(9, failCount.get(), "9 người còn lại phải nhận thông báo thất bại");
    }
}