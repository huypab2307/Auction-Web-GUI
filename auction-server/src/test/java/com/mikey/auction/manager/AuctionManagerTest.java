package com.mikey.auction.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime; // BẮT BUỘC THÊM IMPORT NÀY

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
            ResultSet mockRs = mock(ResultSet.class); // 1. TẠO RESULTSET GIẢ
            
            when(mockDaoInstance.getConnect()).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockConn.prepareStatement(anyString(), anyInt())).thenReturn(mockPs);
            
            // 2. BƠM RESULTSET VÀO PREPARED STATEMENT
            when(mockPs.getGeneratedKeys()).thenReturn(mockRs);
            when(mockPs.executeQuery()).thenReturn(mockRs);
            // 3. GIẢ LẬP ĐỌC DATA TỪ DB
            when(mockRs.next()).thenReturn(true).thenReturn(false);
            when(mockRs.getInt(1)).thenReturn(99); 

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