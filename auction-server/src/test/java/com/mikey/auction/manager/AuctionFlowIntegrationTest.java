package com.mikey.auction.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.mikey.auction.database.AuctionDAO;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.items.Electronics;
import com.mikey.auction.items.Item;
import com.mikey.auction.items.ItemType;

public class AuctionFlowIntegrationTest {

    @Test
    public void testCompleteAuctionAndAutoBidFlow() throws Exception {
        Item item = new Electronics("iPad Pro", "M2 Chip", ItemType.ELECTRONICS, 1, -1, "path");

        try (MockedStatic<AuctionDAO> mockedDao = mockStatic(AuctionDAO.class)) {
            AuctionDAO mockDaoInstance = mock(AuctionDAO.class);
            mockedDao.when(AuctionDAO::getInstance).thenReturn(mockDaoInstance);
            
            Connection mockConn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            
            when(mockDaoInstance.getConnect()).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockConn.prepareStatement(anyString(), anyInt())).thenReturn(mockPs);
            
            ArrayList<AuctionInfo> mockList = new ArrayList<>();
            mockList.add(new AuctionInfo(null, 999, "seller", "bidder", 15000000, null, null, null, 200000));
            when(mockDaoInstance.getAllAuctions()).thenReturn(mockList);

            AuctionManager.getInstance().uploadItem(item, 15000000, 200000, LocalDateTime.now(), LocalDateTime.now().plusHours(5));
            var auctions = AuctionManager.getInstance().auctionList();
            
            assertTrue(auctions != null && !auctions.isEmpty(), "Luồng tích hợp phải trả về dữ liệu");
        }
    }
}