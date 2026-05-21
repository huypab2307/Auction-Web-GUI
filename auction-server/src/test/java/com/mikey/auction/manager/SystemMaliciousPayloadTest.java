package com.mikey.auction.manager;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.mikey.auction.items.Electronics;
import com.mikey.auction.items.Item;
import com.mikey.auction.items.ItemType;

public class SystemMaliciousPayloadTest {

    @Test
    public void testUploadItemWithNullAndEmptyData_ShouldBeBlocked() {
        // 1. Tấn công bằng tên sản phẩm bị Null hoặc để trống rỗng
        Item badItem = new Electronics("", null, ItemType.ELECTRONICS, 1, -1, "path");
        double price = 100000;
        double stepPrice = 10000;
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.now().plusHours(5);

        // Hệ thống chuẩn bắt buộc phải chặn đứng hành vi này bằng Exception, không cho lưu đồ không tên vào hệ thống
        assertThrows(IllegalArgumentException.class, () -> {
            AuctionManager.getInstance().uploadItem(badItem, price, stepPrice, startTime, endTime);
        }, "Hệ thống phải chặn đứng việc tạo sản phẩm không có tên hoặc mô tả");
    }

    @Test
    public void testUploadItemWithHugeDataPayload() {
        // 2. Tấn công tràn bộ nhớ: Tạo một chuỗi mô tả siêu dài (ví dụ: 1 triệu ký tự 'A')
        StringBuilder hugeDescription = new StringBuilder();
        for (int i = 0; i < 1000000; i++) {
            hugeDescription.append("A");
        }

        Item heavyItem = new Electronics("Laptop", hugeDescription.toString(), ItemType.ELECTRONICS, 1, -1, "path");
        
        // Hệ thống cần phải ném lỗi giới hạn ký tự (ví dụ: tối đa mô tả chỉ được 1000 ký tự)
        assertThrows(IllegalArgumentException.class, () -> {
            AuctionManager.getInstance().uploadItem(heavyItem, 100000, 10000, LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        }, "Hệ thống phải giới hạn độ dài ký tự để chống tấn công tràn bộ nhớ (DDoS dữ liệu)");
    }
}