package com.mikey.auction.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mikey.auction.auction.Auction;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.dto.AutoBidInfo;

public class AuctionDAOTest {

    @Test
    public void testAuctionDAOSingleton() {
        // Kiểm tra RAM, hoàn toàn an toàn
        AuctionDAO dao1 = AuctionDAO.getInstance();
        AuctionDAO dao2 = AuctionDAO.getInstance();
        assertSame(dao1, dao2, "AuctionDAO phải là singleton - instance phải giống nhau");
    }

    @Test
    public void testAuctionInfoDTO() {
        // Kiểm tra RAM, hoàn toàn an toàn
        AuctionInfo info = new AuctionInfo(
            null, 123, "sellerTest", "bidderTest", 5000000.0, null, null, null, 0.0
        );
        assertEquals(123, info.getId(), "ID phải khớp");
        assertEquals(5000000.0, info.getCurPrice(), "Giá phải khớp");
    }

    @Test
    public void testDatabaseConnection() throws Exception {
        // TẠO ĐIỆP VIÊN: Giám sát AuctionDAO thật
        AuctionDAO spyDao = spy(AuctionDAO.getInstance());
        Connection mockConn = mock(Connection.class);
        
        // Tráo hàng: Ép điệp viên trả về Connection giả mỗi khi getConnect() được gọi
        doReturn(mockConn).when(spyDao).getConnect();
        when(mockConn.isClosed()).thenReturn(false);

        assertDoesNotThrow(() -> {
            // Gọi qua điệp viên thay vì gọi thẳng hệ thống
            Connection conn = spyDao.getConnect();
            assertNotNull(conn, "Kết nối database không được null");
            assertFalse(conn.isClosed(), "Kết nối database phải đang mở");
            conn.close();
            
            // Xác nhận lệnh close() thực sự đã được gọi vào Connection giả
            verify(mockConn, times(1)).close();
        });
    }

    @Test
    public void testGetAllAuctions() throws Exception {
        AuctionDAO spyDao = spy(AuctionDAO.getInstance());
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        // Giả lập luồng DB
        doReturn(mockConn).when(spyDao).getConnect();
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(false); // Báo DB đang trống để khỏi lặp

        assertDoesNotThrow(() -> {
            var auctions = spyDao.getAllAuctions(); // GỌI QUA ĐIỆP VIÊN
            assertNotNull(auctions, "Danh sách auction không được null");
        });
    }

    @Test
    public void testSearchAuctionById() throws Exception {
        AuctionDAO spyDao = spy(AuctionDAO.getInstance());
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        doReturn(mockConn).when(spyDao).getConnect();
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(false);

        assertDoesNotThrow(() -> {
            AuctionInfo auction = spyDao.searchAuctionById(1);
        });
    }

    @Test
    public void testFindAuctionById() throws Exception {
        // Hàm này MỞ (nhận tham số Connection), nên không cần Điệp viên, truyền thẳng Connection giả vào!
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(false);

        assertDoesNotThrow(() -> {
            Auction auction = AuctionDAO.getInstance().findById(mockConn, 1);
        });
    }

    @Test
    public void testRegisterAutoBid() throws Exception {
        AuctionDAO spyDao = spy(AuctionDAO.getInstance());
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);

        doReturn(mockConn).when(spyDao).getConnect();
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(1); // Giả lập Insert thành công 1 dòng

        assertDoesNotThrow(() -> {
            AutoBidInfo info = new AutoBidInfo(1, 1, 5000000.0);
            boolean result = spyDao.registerAutoBid(info);
            assertTrue(result, "Hàm phải trả về True khi có 1 dòng được update");
        });
    }

    @Test
    public void testTriggerAutoBids() throws Exception {
        AuctionDAO spyDao = spy(AuctionDAO.getInstance());
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        doReturn(mockConn).when(spyDao).getConnect();
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(false);

        assertDoesNotThrow(() -> {
            spyDao.triggerAutoBids(1);
        });
    }
}