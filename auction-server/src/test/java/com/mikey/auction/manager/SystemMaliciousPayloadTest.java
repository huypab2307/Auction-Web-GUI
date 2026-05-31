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
import com.mikey.auction.items.Electronics;
import com.mikey.auction.items.Item;
import com.mikey.auction.items.ItemType;

public class SystemMaliciousPayloadTest {

    @Test
    public void testUploadItemWithHugeDataPayload() throws Exception {
        StringBuilder hugeDescription = new StringBuilder();
        for (int i = 0; i < 1000000; i++) {
            hugeDescription.append("A"); 
        }
        Item heavyItem = new Electronics("Laptop", hugeDescription.toString(), ItemType.ELECTRONICS, 1, -1, "path");

        try (MockedStatic<AuctionDAO> mockedDao = mockStatic(AuctionDAO.class)) {
            AuctionDAO mockDaoInstance = mock(AuctionDAO.class);
            mockedDao.when(AuctionDAO::getInstance).thenReturn(mockDaoInstance);
            
            Connection mockConn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            
            when(mockDaoInstance.getConnect()).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockConn.prepareStatement(anyString(), anyInt())).thenReturn(mockPs);

            assertDoesNotThrow(() -> {
                AuctionManager.getInstance().uploadItem(heavyItem, 100000, 10000, LocalDateTime.now(), LocalDateTime.now().plusHours(5));
            }, "Hệ thống phải xử lý được payload lớn trên RAM mà không crash");
        }
    }
}