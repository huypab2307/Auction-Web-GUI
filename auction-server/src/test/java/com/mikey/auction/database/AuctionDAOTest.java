package com.mikey.auction.database;

import java.sql.Connection;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;

import com.mikey.auction.auction.Auction;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.dto.AutoBidInfo;

public class AuctionDAOTest {

    @Test
    public void testDatabaseConnection() {
        // Kiểm tra kết nối database thông qua AuctionDAO
        assertDoesNotThrow(() -> {
            Connection conn = AuctionDAO.getInstance().getConnect();
            assertNotNull(conn, "Kết nối database không được null");
            assertFalse(conn.isClosed(), "Kết nối database phải đang mở");
            conn.close();
        }, "Kết nối database không được ném exception");
    }

    @Test
    public void testAuctionDAOSingleton() {
        // Kiểm tra AuctionDAO là singleton
        AuctionDAO dao1 = AuctionDAO.getInstance();
        AuctionDAO dao2 = AuctionDAO.getInstance();

        assertSame(dao1, dao2, "AuctionDAO phải là singleton - instance phải giống nhau");
    }

    @Test
    public void testGetAllAuctions() {
        // Kiểm tra lấy tất cả auction
        assertDoesNotThrow(() -> {
            var auctions = AuctionDAO.getInstance().getAllAuctions();
            assertNotNull(auctions, "Danh sách auction không được null");
        }, "Get all auctions không được ném exception");
    }

    @Test
    public void testSearchAuctionById() {
        // Kiểm tra tìm auction theo ID
        assertDoesNotThrow(() -> {
            AuctionInfo auction = AuctionDAO.getInstance().searchAuctionById(1);
            // Kết quả có thể null nếu ID không tồn tại
        }, "Search auction by ID không được ném exception");
    }

    @Test
    public void testUpdateAuction() {
        // Kiểm tra update auction
        AuctionInfo info = new AuctionInfo();
        info.setId(1);
        info.setCurPrice(1000000);

        assertDoesNotThrow(() -> {
            boolean result = AuctionDAO.getInstance().updateAuction(info);
            // Result phụ thuộc vào database
        }, "Update auction không được ném exception");
    }

    @Test
    public void testAuctionInfoDTO() {
        // Kiểm tra DTO AuctionInfo
        AuctionInfo info = new AuctionInfo();
        info.setId(123);
        info.setCurPrice(5000000.0);
        info.setItemName("Test Item");

        assertEquals(123, info.getId(), "ID phải khớp");
        assertEquals(5000000.0, info.getCurPrice(), "Giá phải khớp");
        assertEquals("Test Item", info.getItemName(), "Tên item phải khớp");
    }

    @Test
    public void testAuctionInfoSettersGetters() {
        // Kiểm tra setter/getter của AuctionInfo
        AuctionInfo info = new AuctionInfo();

        info.setId(99);
        info.setCurPrice(2500000);
        info.setItemName("Laptop");
        info.setHighestBidderId("A");

        assertEquals(99, info.getId());
        assertEquals(2500000, info.getCurPrice());
        assertEquals("Laptop", info.getItemName());
        assertEquals(5, info.getHighestBidderId());
    }

    @Test
    public void testCreateAuction() {
        // Kiểm tra tạo auction
        assertDoesNotThrow(() -> {
            Connection conn = AuctionDAO.getInstance().getConnect();
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = LocalDateTime.now().plusHours(24);

            // Chỉ kiểm tra không ném exception
            // Kết quả thực tế phụ thuộc vào database setup
            AuctionDAO.getInstance().createAuction(conn, 1, 2, 1000000, 50000, start, end);

            conn.close();
        }, "Create auction không được ném exception");
    }

    @Test
    public void testFindAuctionById() {
        // Kiểm tra tìm kiếm với Connection parameter
        assertDoesNotThrow(() -> {
            Connection conn = AuctionDAO.getInstance().getConnect();
            Auction auction = AuctionDAO.getInstance().findById(conn, 1);
            // Kết quả có thể null
            conn.close();
        }, "Find by ID không được ném exception");
    }

    @Test
    public void testRegisterAutoBid() {
        // Kiểm tra đăng ký auto-bid
        assertDoesNotThrow(() -> {
        AutoBidInfo info = new AutoBidInfo(1, 1, 5000000.0);

            boolean result = AuctionDAO.getInstance().registerAutoBid(info);
            // Result phụ thuộc vào database
        }, "Register auto-bid không được ném exception");
    }

    @Test
    public void testTriggerAutoBids() {
        // Kiểm tra trigger auto-bid
        assertDoesNotThrow(() -> {
            AuctionDAO.getInstance().triggerAutoBids(1);
        }, "Trigger auto-bids không được ném exception");
    }
}
