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
    
    // Khởi tạo ThreadPool cho các báo thức
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    
    // Quản lý báo thức Mở bán và Đóng phiên riêng biệt
    private final ConcurrentHashMap<Integer, ScheduledFuture<?>> openTasks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, ScheduledFuture<?>> closeTasks = new ConcurrentHashMap<>();

    private AuctionScheduler() {}
    public static AuctionScheduler getInstance() { return instance; }

    /**
     * Hẹn giờ 1 lần duy nhất cho phiên đấu giá (Mở và Đóng)
     */
    public void scheduleAuction(AuctionInfo auction) {
        cancelScheduledTasks(auction.getId()); // Hủy báo thức cũ nếu đây là lệnh cập nhật (Sửa giờ)
        LocalDateTime now = LocalDateTime.now();

        // 1. HẸN GIỜ MỞ BÁN (Nếu đang ở trạng thái PENDING)
        if (auction.getStatus().toString().equals("PENDING")) {
            if (auction.getStartTime().isBefore(now) || auction.getStartTime().isEqual(now)) {
                executeOpening(auction.getId()); // Đã quá giờ mở -> Mở luôn
            } else {
                long delayOpen = Duration.between(now, auction.getStartTime()).toMillis();
                System.out.println("[SCHEDULER] Đã hẹn giờ MỞ phiên ID " + auction.getId() + " sau " + (delayOpen/1000) + "s.");
                ScheduledFuture<?> openTask = scheduler.schedule(() -> executeOpening(auction.getId()), delayOpen, TimeUnit.MILLISECONDS);
                openTasks.put(auction.getId(), openTask);
            }
        }

        // 2. HẸN GIỜ ĐÓNG PHIÊN (Chỉ áp dụng nếu chưa đóng)
        if (!auction.getStatus().toString().equals("CLOSED")) {
            if (auction.getEndTime().isBefore(now) || auction.getEndTime().isEqual(now)) {
                executeClosing(auction.getId()); // Đã quá giờ đóng -> Đóng luôn
            } else {
                long delayClose = Duration.between(now, auction.getEndTime()).toMillis();
                System.out.println("[SCHEDULER] Đã hẹn giờ ĐÓNG phiên ID " + auction.getId() + " sau " + (delayClose/1000) + "s.");
                ScheduledFuture<?> closeTask = scheduler.schedule(() -> executeClosing(auction.getId()), delayClose, TimeUnit.MILLISECONDS);
                closeTasks.put(auction.getId(), closeTask);
            }
        }
    }

    private void executeOpening(int auctionId) {
        try {
            if (AuctionDAO.getInstance().openSingleAuction(auctionId)) {
                System.out.println("[SCHEDULER] Đã MỞ BÁN phiên ID: " + auctionId);
                broadcastUpdate("UPDATE_STATUS", auctionId);
            }
        } catch (Exception e) { e.printStackTrace(); } 
        finally { openTasks.remove(auctionId); }
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

    private void broadcastUpdate(String action, int auctionId) {
        AuctionInfo freshInfo = AuctionDAO.getInstance().searchAuctionById(auctionId);
        if (freshInfo != null) {
            Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (com.google.gson.JsonSerializer<LocalDateTime>) (src, t, ctx) -> new com.google.gson.JsonPrimitive(src.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .create();
            AuctionServer.broadcast("AUCTION|" + action + "|" + gson.toJson(freshInfo));
        }
    }

    public void cancelScheduledTasks(int auctionId) {
        ScheduledFuture<?> oTask = openTasks.remove(auctionId);
        if (oTask != null && !oTask.isDone()) oTask.cancel(false);
        
        ScheduledFuture<?> cTask = closeTasks.remove(auctionId);
        if (cTask != null && !cTask.isDone()) cTask.cancel(false);
    }

    /**
     * Chạy 1 lần duy nhất khi Bật Server (Để cài lại báo thức sau khi mất điện)
     */
    public void loadActiveAuctionsOnStartup() {
        System.out.println("[SCHEDULER] Đang khởi động: Khôi phục đồng hồ báo thức từ Database...");
        int count = 0;
        ArrayList<AuctionInfo> auctions = AuctionDAO.getInstance().getAllAuctions();
        for (AuctionInfo a : auctions) {
            // 👉 CHỈ CẦN KHÁC NULL LÀ CHO VÀO SCHEDULE HẾT, BẤT CHẤP TƯƠNG LAI HAY QUÁ KHỨ!
            if (a.getEndTime() != null) {
                scheduleAuctionClose(a);
            }
        }
        System.out.println("[SCHEDULER] Đã khôi phục báo thức cho " + count + " phiên đấu giá đang mở!");
    }

    public void scheduleNewAuctions() {
        ArrayList<AuctionInfo> auctions = AuctionDAO.getInstance().getAllAuctions();
        for (AuctionInfo a : auctions) {
            // Nếu phát hiện phiên nào chưa có chìa khóa trong danh sách báo thức -> Cài báo thức ngay lập tức!
            if (!openTasks.containsKey(a.getId()) && !closeTasks.containsKey(a.getId())) {
                scheduleAuction(a);
            }
        }
    }
}