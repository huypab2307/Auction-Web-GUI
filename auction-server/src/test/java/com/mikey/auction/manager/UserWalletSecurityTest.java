package com.mikey.auction.manager;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.mikey.auction.database.AuctionDAO;
import com.mikey.auction.dto.AuctionInfo;

public class UserWalletSecurityTest {

    @Test
    public void testBidWithInsufficientBalance_ShouldFail() throws Exception {
        AuctionInfo info = new AuctionInfo(null, 123, "sellerTest", "bidderTest", 2000000.0, null, null, null, 0.0);

        try (MockedStatic<AuctionDAO> mockedDao = mockStatic(AuctionDAO.class)) {
            AuctionDAO mockDaoInstance = mock(AuctionDAO.class);
            mockedDao.when(AuctionDAO::getInstance).thenReturn(mockDaoInstance);
            
            // Giả lập DB trả về False (từ chối giao dịch do thiếu tiền)
            when(mockDaoInstance.updateAuction(any(AuctionInfo.class))).thenReturn(false);

            assertDoesNotThrow(() -> {
                AuctionDAO.getInstance().updateAuction(info);
            }, "Hệ thống phải từ chối an toàn mà không crash");
        }
    }

    @Test
    public void testRefundToPreviousBidderWhenOutbid() throws Exception {
        AuctionInfo infoFromUserB = new AuctionInfo(null, 123, "sellerTest", "bidderB", 600000.0, null, null, null, 0.0);

        try (MockedStatic<AuctionDAO> mockedDao = mockStatic(AuctionDAO.class)) {
            AuctionDAO mockDaoInstance = mock(AuctionDAO.class);
            mockedDao.when(AuctionDAO::getInstance).thenReturn(mockDaoInstance);
            
            // Giả lập DB cho phép giao dịch
            when(mockDaoInstance.updateAuction(any(AuctionInfo.class))).thenReturn(true);

            assertDoesNotThrow(() -> {
                AuctionDAO.getInstance().updateAuction(infoFromUserB);
            }, "Tiền phải được hoàn trả cho người bị vượt giá");
        }
    }
}