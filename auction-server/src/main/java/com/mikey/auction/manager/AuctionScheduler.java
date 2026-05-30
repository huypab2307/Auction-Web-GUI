package com.mikey.auction.manager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mikey.auction.database.AuctionDAO;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.socket.AuctionServer;

public class AuctionScheduler {
    private static final AuctionScheduler instance = new AuctionScheduler();
    
    // Khởi tạo ThreadPool với 4 luồng xử lý đồng thời cho các tác vụ đóng phiên
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    
    // Lưu trữ các Task đang chờ chạy để có thể hủy khi thời gian kết thúc thay đổi
    private final ConcurrentHashMap<Integer, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    private AuctionScheduler() {}
    public static AuctionScheduler getInstance() { return instance; }

    /**
     * Lập lịch đếm ngược đóng phiên cho một Auction
     */
    public void scheduleAuctionClose(AuctionInfo auction) {
        // Hủy tác vụ cũ nếu phiên này từng được lập lịch trước đó (tránh trùng lặp khi update)
        cancelScheduledClose(auction.getId());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = auction.getEndTime();

        // Nếu đã quá giờ kết thúc, tiến hành đóng ngay lập tức
        if (endTime.isBefore(now)) {
            executeClosing(auction.getId());
            return;
        }

        // Tính toán khoảng thời gian delay chính xác theo mili-giây
        long delay = Duration.between(now, endTime).toMillis();
        System.out.println("[SCHEDULER] Đã lên lịch đóng phiên ID " + auction.getId() + " sau " + (delay / 1000) + " giây.");

        // Đưa vào ScheduledThreadPool để chờ thực thi
        ScheduledFuture<?> futureTask = scheduler.schedule(() -> {
            executeClosing(auction.getId());
        }, delay, TimeUnit.MILLISECONDS);

        scheduledTasks.put(auction.getId(), futureTask);
    }

    /**
     * Logic thực hiện khóa phiên dưới DB, sinh HÓA ĐƠN và phát tín hiệu cho Client
     */
    private void executeClosing(int auctionId) {
        try {
            System.out.println("[SCHEDULER] Hệ thống bắt đầu đóng phiên ID: " + auctionId);
            
            // 1. Lấy thông tin TRƯỚC KHI ĐÓNG để biết ai đang top 1
            com.mikey.auction.dto.AuctionInfo closingInfo = com.mikey.auction.database.AuctionDAO.getInstance().searchAuctionById(auctionId);
            
            // 2. Chốt sổ đổi status thành CLOSED dưới Database
            boolean isClosed = com.mikey.auction.database.AuctionDAO.getInstance().closeSingleAuction(auctionId);
            
            if (isClosed) {
                System.out.println("✅ Đã khóa phiên thành công: " + auctionId);
                
                // 3. 👉 MA THUẬT NẰM Ở ĐÂY: Nếu có người thắng, tự động xuất hóa đơn!
                if (closingInfo != null && closingInfo.getLastBidderName() != null && !closingInfo.getLastBidderName().isEmpty()) {
                    // Truy vấn lại ID của người thắng bằng hàm findById
                    com.mikey.auction.auction.Auction rawAuc = com.mikey.auction.database.AuctionDAO.getInstance().findById(
                        com.mikey.auction.database.AuctionDAO.getInstance().getConnect(), auctionId
                    );
                    
                    if (rawAuc != null && rawAuc.getLastBidder() > 0) {
                        // Gọi hàm tạo hóa đơn trong DAO
                        com.mikey.auction.database.AuctionDAO.getInstance().generateInvoice(auctionId, rawAuc.getLastBidder(), rawAuc.getCurPrice());
                    }
                }

                // 4. Lấy gói dữ liệu mới nhất (đã cập nhật trạng thái) để gửi về Client
                com.mikey.auction.dto.AuctionInfo freshInfo = com.mikey.auction.database.AuctionDAO.getInstance().searchAuctionById(auctionId);
                if (freshInfo != null) {
                    com.google.gson.Gson gson = new com.google.gson.GsonBuilder()
                        .registerTypeAdapter(java.time.LocalDateTime.class, (com.google.gson.JsonSerializer<java.time.LocalDateTime>) (src, t, ctx) -> new com.google.gson.JsonPrimitive(src.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                        .create();
                    
                    // 5. Cầm loa phát thanh thông báo "ĐÃ ĐÓNG PHIÊN" cho toàn bộ UI nhảy số
                    com.mikey.auction.socket.AuctionServer.broadcast("AUCTION|CLOSED_NOTIFY|" + gson.toJson(freshInfo));
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi thực thi đóng phiên ID " + auctionId + ": " + e.getMessage());
        } finally {
            // Đóng xong thì xóa báo thức để giải phóng RAM
            scheduledTasks.remove(auctionId);
        }
    }

    /**
     * Hủy lịch trình đóng phiên
     */
    public void cancelScheduledClose(int auctionId) {
        ScheduledFuture<?> task = scheduledTasks.remove(auctionId);
        if (task != null && !task.isDone()) {
            task.cancel(false);
        }
    }

    /**
     * Quét và nạp lại tất cả các phiên đấu giá hợp lệ từ DB khi Server khởi động
     */
    public void loadActiveAuctionsOnStartup() {
        System.out.println("[SCHEDULER] Đang nạp lại lịch trình các phiên đấu giá từ Database...");
        ArrayList<AuctionInfo> auctions = AuctionDAO.getInstance().getAllAuctions();
        
        for (AuctionInfo a : auctions) {
            // 👉 CHỈ CẦN KHÁC NULL LÀ CHO VÀO SCHEDULE HẾT, BẤT CHẤP TƯƠNG LAI HAY QUÁ KHỨ!
            if (a.getEndTime() != null) {
                scheduleAuctionClose(a);
            }
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}