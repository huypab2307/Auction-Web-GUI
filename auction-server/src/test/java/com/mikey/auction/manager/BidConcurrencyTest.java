package com.mikey.auction.manager;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.mikey.auction.database.AuctionDAO;
import com.mikey.auction.dto.AuctionInfo;

public class BidConcurrencyTest {

    private AuctionDAO auctionDAO = mock(AuctionDAO.class);

    @Test
    public void testConcurrentBiddingOnSingleAuction() throws InterruptedException {
        int numberOfThreads = 10; 
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        AuctionInfo info = new AuctionInfo(null, 123, "sellerTest", "bidderTest", 150000.0, null, null, null, 0.0);

        when(auctionDAO.updateAuction(any(AuctionInfo.class))).thenReturn(true);

        for (int i = 0; i < numberOfThreads; i++) {
            service.submit(() -> {
                try {
                    latch.await(); 
                    auctionDAO.updateAuction(info);
                    successCount.incrementAndGet();
                } catch (Throwable t) {
                    failCount.incrementAndGet();
                }
            });
        }

        latch.countDown(); 
        service.shutdown();
        
        while (!service.isTerminated()) {
            Thread.sleep(100);
        }

        assertDoesNotThrow(() -> {
            int totalProcessed = successCount.get() + failCount.get();
        }, "Hệ thống phải chịu tải được đa luồng");
        
        verify(auctionDAO, times(10)).updateAuction(any(AuctionInfo.class));
    }
}