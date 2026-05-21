package com.mikey.auction.manager;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.mikey.auction.items.Electronics;
import com.mikey.auction.items.Item;
import com.mikey.auction.items.ItemType;

public class AuctionValidationTest {

    @Test
    public void testUploadItemWithNegativePrice_ShouldThrowException() {
        Item item = new Electronics("Laptop", "Description", ItemType.ELECTRONICS, 1, -1, "path");
        double invalidPrice = -5000; // Giá âm
        double stepPrice = 10000;
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.now().plusHours(24);

        // Đúng chuẩn: Hệ thống phải chặn ngay từ vòng gửi xe và ném ra Exception chống phá hoại dữ liệu
        assertThrows(IllegalArgumentException.class, () -> {
            AuctionManager.getInstance().uploadItem(item, invalidPrice, stepPrice, startTime, endTime);
        }, "Hệ thống phải ném IllegalArgumentException khi giá khởi điểm bị âm");
    }

    @Test
    public void testUploadItemWithEndTimeBeforeStartTime_ShouldThrowException() {
        Item item = new Electronics("Phone", "Description", ItemType.ELECTRONICS, 1, -1, "path");
        double price = 500000;
        double stepPrice = 50000;
        
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime invalidEndTime = LocalDateTime.now().minusHours(2); // Thời gian kết thúc lại trước thời gian bắt đầu!

        assertThrows(IllegalArgumentException.class, () -> {
            AuctionManager.getInstance().uploadItem(item, price, stepPrice, startTime, invalidEndTime);
        }, "Hệ thống phải báo lỗi khi thời gian kết thúc trước thời gian bắt đầu");
    }
}