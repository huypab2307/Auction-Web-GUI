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
     * Hẹn giờ 1 lần duy nhất cho phiên đấu giá (Cả Mở bán và Đóng phiên)
     */
    public void scheduleAuction(AuctionInfo auction) {
        cancelScheduledTasks(auction.getId()); // Hủy báo thức cũ nếu đây là lệnh cập nhật
        
        // Bỏ qua không hẹn giờ cho các đơn đã bị Hủy bỏ (Canceled)
        if ("CANCELED".equals(auction.getStatus().toString())) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // 1. KIỂM TRA HẾT HẠN TRƯỚC TIÊN (Ưu tiên số 1)
        // Dù trạng thái trên RAM đang là chữ gì đi nữa, cứ quá giờ là ép gọi hàm khóa Database!
        if (auction.getEndTime().isBefore(now) || auction.getEndTime().isEqual(now)) {
            executeClosing(auction.getId());
            return; // Đóng xong là thoát, kết thúc vòng đời
        }

        // 2. NẾU CHƯA TỚI GIỜ MỞ BÁN -> HẸN GIỜ MỞ BÁN
        if (auction.getStartTime().isAfter(now)) {
            long delayOpen = Duration.between(now, auction.getStartTime()).toMillis();
            System.out.println("[SCHEDULER] Đã hẹn giờ MỞ phiên ID " + auction.getId() + " sau " + (delayOpen/1000) + "s.");
            ScheduledFuture<?> openTask = scheduler.schedule(() -> executeOpening(auction.getId()), delayOpen, TimeUnit.MILLISECONDS);
            openTasks.put(auction.getId(), openTask);
        } 
        // 3. NẾU ĐÃ QUA GIỜ MỞ BÁN MÀ BỊ KẸT CHỮ PENDING (Do mất điện) -> ÉP MỞ LUÔN
        else if ("PENDING".equals(auction.getStatus().toString())) {
            executeOpening(auction.getId());
        }

        // 4. HẸN GIỜ ĐÓNG PHIÊN (Chắc chắn nằm ở tương lai vì đã vượt qua chốt chặn số 1)
        long delayClose = Duration.between(now, auction.getEndTime()).toMillis();
        System.out.println("[SCHEDULER] Đã hẹn giờ ĐÓNG phiên ID " + auction.getId() + " sau " + (delayClose/1000) + "s.");
        ScheduledFuture<?> closeTask = scheduler.schedule(() -> executeClosing(auction.getId()), delayClose, TimeUnit.MILLISECONDS);
        closeTasks.put(auction.getId(), closeTask);
    }
    

    private void executeOpening(int auctionId) {
        try {
            if (AuctionDAO.getInstance().openSingleAuction(auctionId)) {
                System.out.println("✅ [SCHEDULER] Đã MỞ BÁN phiên ID: " + auctionId);
                broadcastUpdate("UPDATE_STATUS", auctionId);
            }
        } catch (Exception e) { e.printStackTrace(); } 
        finally { openTasks.remove(auctionId); }
    }

    private void executeClosing(int auctionId) {
        try {
            System.out.println("[SCHEDULER] Hệ thống bắt đầu đóng phiên ID: " + auctionId);
            
            // Lấy thông tin TRƯỚC KHI ĐÓNG để xuất hóa đơn
            AuctionInfo closingInfo = AuctionDAO.getInstance().searchAuctionById(auctionId);
            
            if (AuctionDAO.getInstance().closeSingleAuction(auctionId)) {
                System.out.println("✅ Đã khóa phiên thành công: " + auctionId);
                
                // 👉 TÍNH NĂNG XUẤT HÓA ĐƠN CỦA BẠN:
                if (closingInfo != null && closingInfo.getLastBidderName() != null && !closingInfo.getLastBidderName().isEmpty()) {
                    com.mikey.auction.auction.Auction rawAuc = AuctionDAO.getInstance().findById(
                        AuctionDAO.getInstance().getConnect(), auctionId
                    );
                    if (rawAuc != null && rawAuc.getLastBidder() > 0) {
                        AuctionDAO.getInstance().generateInvoice(auctionId, rawAuc.getLastBidder(), rawAuc.getCurPrice());
                    }
                }
                
                // Cầm loa phát thanh thông báo
                broadcastUpdate("CLOSED_NOTIFY", auctionId);
            }
        } catch (Exception e) { e.printStackTrace(); } 
        finally { closeTasks.remove(auctionId); }
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

    public void loadActiveAuctionsOnStartup() {
        System.out.println("[SCHEDULER] Đang khởi động: Khôi phục đồng hồ báo thức từ Database...");
        ArrayList<AuctionInfo> auctions = AuctionDAO.getInstance().getAllAuctions();
        for (AuctionInfo a : auctions) {
            if (a.getEndTime() != null) {
                scheduleAuction(a);
            }
        }
    }

    // 👉 ĐÂY CHÍNH LÀ RADAR ĐỂ BẮT CÁC PHIÊN VỪA TẠO MỚI
    public void scheduleNewAuctions() {
        ArrayList<AuctionInfo> auctions = AuctionDAO.getInstance().getAllAuctions();
        for (AuctionInfo a : auctions) {
            if (!openTasks.containsKey(a.getId()) && !closeTasks.containsKey(a.getId())) {
                scheduleAuction(a);
            }
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}