package com.mikey.auction.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime; // BẮT BUỘC THÊM IMPORT NÀY

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mikey.auction.database.AuctionDAO;
import com.mikey.auction.items.Electronics;
import com.mikey.auction.items.Item;
import com.mikey.auction.items.ItemType;

public class AuctionValidationTest {

    @Test
    public void testUploadItemWithNegativePrice_ShouldBeBlocked() throws Exception {
        Item item = new Electronics("Laptop", "Description", ItemType.ELECTRONICS, 1, -1, "path");
        double invalidPrice = -5000; // Giá âm
        double stepPrice = 10000;
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.now().plusHours(24);

        try (MockedStatic<AuctionDAO> mockedDao = mockStatic(AuctionDAO.class)) {
            AuctionDAO mockDaoInstance = mock(AuctionDAO.class);
            mockedDao.when(AuctionDAO::getInstance).thenReturn(mockDaoInstance);

            AuctionManager.getInstance().uploadItem(item, invalidPrice, stepPrice, startTime, endTime);
            verify(mockDaoInstance, never()).getConnect();
        }
    }

    @Test
    public void testUploadItemWithEndTimeBeforeStartTime_ShouldBeHandledSafely() throws Exception {
        Item item = new Electronics("Phone", "Description", ItemType.ELECTRONICS, 1, -1, "path");
        double price = 500000;
        double stepPrice = 50000;
        
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime invalidEndTime = LocalDateTime.now().minusHours(2);

        try (MockedStatic<AuctionDAO> mockedDao = mockStatic(AuctionDAO.class)) {
            AuctionDAO mockDaoInstance = mock(AuctionDAO.class);
            mockedDao.when(AuctionDAO::getInstance).thenReturn(mockDaoInstance);

            Connection mockConn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class); // TẠO RESULTSET GIẢ

            when(mockDaoInstance.getConnect()).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockConn.prepareStatement(anyString(), anyInt())).thenReturn(mockPs);
            
            // BƠM RESULTSET CHỐNG NULL
            when(mockPs.getGeneratedKeys()).thenReturn(mockRs);
            when(mockPs.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true).thenReturn(false);
            when(mockRs.getInt(1)).thenReturn(99);

            assertDoesNotThrow(() -> {
                AuctionManager.getInstance().uploadItem(item, price, stepPrice, startTime, invalidEndTime);
            }, "Hệ thống phải xử lý an toàn dữ liệu ngày tháng sai mà không bị văng lỗi (Crash)");
        }
    }
}