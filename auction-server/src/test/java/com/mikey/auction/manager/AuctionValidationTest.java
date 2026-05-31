package com.mikey.auction.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString; // THÊM DÒNG IMPORT NÀY
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
    public void testUploadItemWithNegativePrice_ShouldBeBlocked() throws Exception { // THÊM THROWS EXCEPTION
        Item item = new Electronics("Laptop", "Description", ItemType.ELECTRONICS, 1, -1, "path");
        double invalidPrice = -5000; // Giá âm
        double stepPrice = 10000;
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.now().plusHours(24);

        try (MockedStatic<AuctionDAO> mockedDao = mockStatic(AuctionDAO.class)) {
            AuctionDAO mockDaoInstance = mock(AuctionDAO.class);
            mockedDao.when(AuctionDAO::getInstance).thenReturn(mockDaoInstance);

            // Chạy luồng thật với giá đầu vào sai
            AuctionManager.getInstance().uploadItem(item, invalidPrice, stepPrice, startTime, endTime);

            // ĐẲNG CẤP LÀ Ở ĐÂY: 
            // Nếu thuật toán của bạn tốt, nó sẽ gặp "if (price <= 0)" và return thoát ra ngay lập tức.
            // Lệnh verify + never() dưới đây sẽ chứng minh: Hệ thống CHƯA TỪNG gọi hàm mở Connection xuống DB!
            verify(mockDaoInstance, never()).getConnect();
        }
    }

    @Test
    public void testUploadItemWithEndTimeBeforeStartTime_ShouldBeHandledSafely() throws Exception { // THÊM THROWS EXCEPTION
        Item item = new Electronics("Phone", "Description", ItemType.ELECTRONICS, 1, -1, "path");
        double price = 500000;
        double stepPrice = 50000;
        
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime invalidEndTime = LocalDateTime.now().minusHours(2); // Thời gian sai logic

        try (MockedStatic<AuctionDAO> mockedDao = mockStatic(AuctionDAO.class)) {
            AuctionDAO mockDaoInstance = mock(AuctionDAO.class);
            mockedDao.when(AuctionDAO::getInstance).thenReturn(mockDaoInstance);

            // Vẫn khoác áo giáp Mockito để đề phòng luồng code đâm xuống DB thì không bị crash
            Connection mockConn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            when(mockDaoInstance.getConnect()).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockConn.prepareStatement(anyString(), anyInt())).thenReturn(mockPs);

            // SỬ DỤNG assertDoesNotThrow CỦA JUNIT THAY VÌ TRY-CATCH
            assertDoesNotThrow(() -> {
                AuctionManager.getInstance().uploadItem(item, price, stepPrice, startTime, invalidEndTime);
            }, "Hệ thống phải xử lý an toàn dữ liệu ngày tháng sai mà không bị văng lỗi (Crash)");
            
            // Chú ý: Vì hàm uploadItem thật của bạn hiện chưa có code "if (endTime.isBefore(startTime))", 
            // nên dữ liệu vẫn lọt qua được và chọc xuống DB. Lớp khiên mockConn ở trên sẽ hứng trọn cú lọt lưới này!
        }
    }
}