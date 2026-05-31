package com.mikey.auction.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet; // BẮT BUỘC THÊM DÒNG IMPORT NÀY
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
            ResultSet mockRs = mock(ResultSet.class); // 1. TẠO RESULTSET GIẢ
            
            when(mockDaoInstance.getConnect()).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockConn.prepareStatement(anyString(), anyInt())).thenReturn(mockPs);
            
            // 2. BƠM RESULTSET GIẢ VÀO PREPARED STATEMENT (CHỐNG NULL)
            when(mockPs.getGeneratedKeys()).thenReturn(mockRs);
            when(mockPs.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true).thenReturn(false); // 3. Giả lập vòng lặp while(rs.next())
            when(mockRs.getInt(1)).thenReturn(99); // 4. Giả lập trả về ID sản phẩm
            
            ArrayList<AuctionInfo> mockList = new ArrayList<>();
            mockList.add(new AuctionInfo(null, 999, "seller", "bidder", 15000000, null, null, null, 200000));
            when(mockDaoInstance.getAllAuctions()).thenReturn(mockList);

            // Chạy luồng code thật, giờ thì DAO đã có ResultSet giả để xử lý mượt mà
            AuctionManager.getInstance().uploadItem(item, 15000000, 200000, LocalDateTime.now(), LocalDateTime.now().plusHours(5));
            var auctions = AuctionManager.getInstance().auctionList();
            
            assertTrue(auctions != null && !auctions.isEmpty(), "Luồng tích hợp phải trả về dữ liệu");
        }
    }
}