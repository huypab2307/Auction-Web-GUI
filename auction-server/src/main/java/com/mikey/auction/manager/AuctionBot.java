package com.mikey.auction.manager;

import com.mikey.auction.database.AuctionDAO;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionBot {
    // Tạo một luồng chạy ngầm duy nhất
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void start() {
        // Lên lịch chạy hàm autoUpdateStatuses mỗi 1 phút (60 giây)
        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("[BOT] Đang kiểm tra và cập nhật trạng thái phiên đấu giá...");
                // Gọi hàm cập nhật SQL đã viết trong AuctionDAO
                AuctionDAO.getInstance().autoUpdateStatuses();
            } catch (Exception e) {
                System.err.println("[BOT] Lỗi khi cập nhật trạng thái: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    public void stop() {
        scheduler.shutdown();
    }
}