package com.mikey.auction.manager;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.mikey.auction.dto.AuctionInfo;

public class UserWalletSecurityTest {

    @Test
    public void testBidWithInsufficientBalance_ShouldFail() {
        // Giả lập: Người dùng ID 10 có số dư ví là 100,000 VND trong UserManager
        // (Đoạn này tùy thuộc vào cách bạn set balance trong UserManager của bạn)
        // UserManager.getInstance().getUser(10).setBalance(100000);

        // Tạo một thông tin nâng giá lên tận 2,000,000 VND (Vượt quá số dư)
        AuctionInfo info = new AuctionInfo();
        info.setId(123);
        info.setCurPrice(2000000);
        // info.setBidderId(10); // Giả định DTO của bạn có trường lưu ai là người đặt

        // Khẳng định: Hệ thống phải từ chối hoặc ném lỗi, không cho phép đặt giá
        assertThrows(IllegalArgumentException.class, () -> {
            AuctionManager.getInstance().updateAuction(info);
        }, "Hệ thống phải chặn đứng lượt đặt giá nếu ví người dùng không đủ tiền");
    }

    @Test
    public void testRefundToPreviousBidderWhenOutbid() {
        // Kịch bản: 
        // Người A đang giữ giá 500,000 VND (Ví của A đang bị tạm khóa 500,000 VND)
        // Người B nhảy vào đặt giá 600,000 VND và thành công.
        
        AuctionInfo infoFromUserB = new AuctionInfo();
        infoFromUserB.setId(123);
        infoFromUserB.setCurPrice(600000);
        // infoFromUserB.setBidderId(UserB_ID);

        // Kích hoạt lượt đặt giá của người B
        AuctionManager.getInstance().updateAuction(infoFromUserB);

        // Khẳng định: Ví của người A phải được tự động cộng trả lại 500,000 VND ngay lập tức
        // double walletA = UserManager.getInstance().getUser(UserA_ID).getBalance();
        // assertEquals(expectedBalanceAfterRefund, walletA, "Người dùng cũ phải được hoàn tiền khi có người trả giá cao hơn");
    }
}