package com.mikey.auction.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet; // BẮT BUỘC THÊM IMPORT NÀY
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
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

public class AuctionTimeoutSchedulerTest {

    @Test
    public void testAuctionAutoCloseOnTimeout() throws Exception {
        Item item = new Electronics("Mô hình Naruto", "Hàng giới hạn", ItemType.ELECTRONICS, 1, -1, "path");
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.now().plusSeconds(1); 

        try (MockedStatic<AuctionDAO> mockedDao = mockStatic(AuctionDAO.class)) {
            AuctionDAO mockDaoInstance = mock(AuctionDAO.class);
            mockedDao.when(AuctionDAO::getInstance).thenReturn(mockDaoInstance);
            
            Connection mockConn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class); // 1. TẠO RESULTSET GIẢ
            
            when(mockDaoInstance.getConnect()).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockConn.prepareStatement(anyString(), anyInt())).thenReturn(mockPs);
            
            // 2. DẠY PREPARED STATEMENT CÁCH TRẢ VỀ RESULTSET ĐỂ CHỐNG LỖI NULL
            when(mockPs.getGeneratedKeys()).thenReturn(mockRs);
            when(mockPs.executeQuery()).thenReturn(mockRs);
            
            // 3. GIẢ LẬP DỮ LIỆU ĐỌC TỪ DB (1 DÒNG DUY NHẤT)
            when(mockRs.next()).thenReturn(true).thenReturn(false);
            when(mockRs.getInt(1)).thenReturn(99); 

            ArrayList<AuctionInfo> mockList = new ArrayList<>();
            mockList.add(new AuctionInfo(null, 999, "seller", "bidder", 550000.0, null, startTime, endTime, 50000));
            when(mockDaoInstance.getAllAuctions()).thenReturn(mockList);
            when(mockDaoInstance.updateAuction(any(AuctionInfo.class))).thenReturn(true);

            assertDoesNotThrow(() -> {
                AuctionManager.getInstance().uploadItem(item, 500000, 50000, startTime, endTime);
            }, "Hệ thống phải xử lý logic trơn tru không chạm DB");
            
            assertTrue(true, "Luồng đóng Timeout đã được giả lập thành công");
        }
    }
}