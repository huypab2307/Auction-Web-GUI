package com.mikey.auction.database;

import java.sql.Connection;

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
    public void testAuctionInfoDTO() {
        // Khởi tạo AuctionInfo bằng constructor có tham số thật của hệ thống
        AuctionInfo info = new AuctionInfo(
            null,          // itemInfo
            123,           // id
            "sellerTest",  // sellerUsername
            "bidderTest",  // lastBidderName
            5000000.0,     // SỬA TẠI ĐÂY: Đổi từ 500000.0 thành 5000000.0 để đồng bộ với dòng kiểm tra bên dưới
            null,          // status
            null,          // startTime
            null,          // endTime
            0.0            // bidStep
        );
        
        // Khẳng định giá trị kiểm thử
        assertEquals(123, info.getId(), "ID phải khớp");
        assertEquals(5000000.0, info.getCurPrice(), "Giá phải khớp");
    }


    @Test
    public void testFindAuctionById() {
        // Kiểm tra tìm kiếm với Connection parameter
        assertDoesNotThrow(() -> {
            Connection conn = AuctionDAO.getInstance().getConnect();
            Auction auction = AuctionDAO.getInstance().findById(conn, 1);
            conn.close();
        }, "Find by ID không được ném exception");
    }

    @Test
    public void testRegisterAutoBid() {
        // Kiểm tra đăng ký auto-bid
        assertDoesNotThrow(() -> {
            AutoBidInfo info = new AutoBidInfo(1, 1, 5000000.0);
            boolean result = AuctionDAO.getInstance().registerAutoBid(info);
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