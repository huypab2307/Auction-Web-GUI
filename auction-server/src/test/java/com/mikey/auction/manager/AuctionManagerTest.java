package com.mikey.auction.manager;

import java.time.LocalDateTime;
import java.sql.Connection;
import java.sql.PreparedStatement;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.mikey.auction.database.AuctionDAO;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.dto.AutoBidInfo;
import com.mikey.auction.items.Electronics;
import com.mikey.auction.items.Item;
import com.mikey.auction.items.ItemType;

public class AuctionManagerTest {

    @Test
    public void testUploadItemValidPrice() throws Exception {
        Item item = new Electronics("Laptop", "High performance laptop", ItemType.ELECTRONICS, 1, -1, "path");
        double price = 10000000;
        double stepPrice = 100000;
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.now().plusHours(24);

        try (MockedStatic<AuctionDAO> mockedDao = mockStatic(AuctionDAO.class)) {
            AuctionDAO mockDaoInstance = mock(AuctionDAO.class);
            mockedDao.when(AuctionDAO::getInstance).thenReturn(mockDaoInstance);
            
            Connection mockConn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            
            when(mockDaoInstance.getConnect()).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockConn.prepareStatement(anyString(), anyInt())).thenReturn(mockPs);

            assertDoesNotThrow(() -> {
                AuctionManager.getInstance().uploadItem(item, price, stepPrice, startTime, endTime);
            });
        }
    }

    @Test
    public void testRegisterAutoBid() throws Exception {
        AutoBidInfo autoBidInfo = new AutoBidInfo(1, 99999, 1000000);

        try (MockedStatic<AuctionDAO> mockedDao = mockStatic(AuctionDAO.class)) {
            AuctionDAO mockDaoInstance = mock(AuctionDAO.class);
            mockedDao.when(AuctionDAO::getInstance).thenReturn(mockDaoInstance);
            
            // Giả lập luồng dữ liệu khi Manager gọi xuống DAO
            when(mockDaoInstance.registerAutoBid(any(AutoBidInfo.class))).thenReturn(true);
            doNothing().when(mockDaoInstance).triggerAutoBids(anyInt());
            
            AuctionInfo mockFreshAuction = new AuctionInfo(null, 99999, "seller", "bidder", 1000000, null, null, null, 50000);
            when(mockDaoInstance.searchAuctionById(anyInt())).thenReturn(mockFreshAuction);
            
            assertDoesNotThrow(() -> {
                AuctionManager.getInstance().registerAutoBid(autoBidInfo);
            });
            
            verify(mockDaoInstance, times(1)).registerAutoBid(any(AutoBidInfo.class));
        }
    }
}