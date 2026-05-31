package com.mikey.auction.manager;

import java.time.LocalDateTime;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays; // THÊM IMPORT NÀY ĐỂ TỐI ƯU MẢNG

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
        
        // 1. GỌI MOCKITO LÀM VIỆC TRƯỚC KHI RAM BỊ CHIẾM DỤNG
        try (MockedStatic<AuctionDAO> mockedDao = mockStatic(AuctionDAO.class)) {
            AuctionDAO mockDaoInstance = mock(AuctionDAO.class);
            mockedDao.when(AuctionDAO::getInstance).thenReturn(mockDaoInstance);
            
            Connection mockConn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class); // TẠO RESULTSET GIẢ (CHỐNG NULL)
            
            when(mockDaoInstance.getConnect()).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockConn.prepareStatement(anyString(), anyInt())).thenReturn(mockPs);
            
            // BƠM RESULTSET CHỐNG LỖI NULL
            when(mockPs.getGeneratedKeys()).thenReturn(mockRs);
            when(mockPs.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true).thenReturn(false);
            when(mockRs.getInt(1)).thenReturn(99);

            // 2. TẠO PAYLOAD KHỔNG LỒ MỘT CÁCH THÔNG MINH
            // Cách này tạo ra 500,000 ký tự 'A' ngay lập tức mà không sinh rác RAM
            char[] chars = new char[500000];
            Arrays.fill(chars, 'A');
            String hugeDescription = new String(chars);
            
            Item heavyItem = new Electronics("Laptop", hugeDescription, ItemType.ELECTRONICS, 1, -1, "path");

            // 3. THỰC THI TEST
            assertDoesNotThrow(() -> {
                AuctionManager.getInstance().uploadItem(heavyItem, 100000, 10000, LocalDateTime.now(), LocalDateTime.now().plusHours(5));
            }, "Hệ thống phải xử lý được payload lớn trên RAM mà không crash");
        }
    }
}