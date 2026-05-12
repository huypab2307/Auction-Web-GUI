package com.mikey.auction.javagui.topbar;

import com.google.gson.Gson;
import com.mikey.auction.auction.Notifications;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.javagui.SceneChanger;
import com.mikey.auction.socket.RequestHandler;
import com.mikey.auction.socket.SocketClient;
import com.mikey.auction.socket.SocketListener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

import java.time.format.DateTimeFormatter;

/**
 * Controller xử lý từng thẻ thông báo riêng lẻ trong danh sách thông báo.
 * Loại bỏ hoàn toàn NotificationDAO để giao tiếp qua Socket.
 */
public class NotificationController implements SocketListener {
    @FXML private Label messageLabel;
    @FXML private Label dateLabel;

    private int auctionId;
    private int notificationId;
    private int userId;
    private final Gson gson = new Gson();

    /**
     * Thiết lập nội dung hiển thị cho thẻ thông báo.
     * @param notification Đối tượng thông báo nhận từ Server.
     */
    public void setContent(Notifications notification) {
        this.auctionId = notification.getAuctionId(); //
        this.notificationId = notification.getId(); //
        this.userId = notification.getUserId(); //

        messageLabel.setText(notification.getMessage()); //
        
        // Định dạng thời gian hiển thị
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
        dateLabel.setText(notification.getCreatedAt().format(formatter)); //
    }

    /**
     * Xử lý sự kiện khi người dùng click vào thẻ thông báo.
     * Đánh dấu đã đọc và chuyển hướng đến trang chi tiết đấu giá.
     */
    @FXML
    public void handleCardClick(MouseEvent mouseEvent) {
        // Đăng ký nhận phản hồi cho hành động click này
        SocketClient.getInstance().setListener(this);

        // 1. Gửi yêu cầu đánh dấu thông báo này là đã đọc lên Server
        RequestHandler.getInstance().requestMarkAsRead(userId, notificationId);

        // 2. Gửi yêu cầu lấy thông tin chi tiết phiên đấu giá để chuyển cảnh
        // Lưu ý: Đảm bảo RequestHandler của bạn có phương thức requestSearchById
        RequestHandler.getInstance().requestFindItem(null, auctionId); 
        // Hoặc dùng requestSearch nếu bạn muốn Server trả về AuctionInfo
    }

    /**
     * Hứng dữ liệu AuctionInfo trả về từ Server để thực hiện chuyển cảnh.
     */
    @Override
    public void onResponseReceived(String category, String action, String jsonData) {
        // Hứng thông tin phiên đấu giá dựa trên auctionId
        if ("ITEM".equals(category) && "FIND".equals(action)) {
            AuctionInfo auctionInfo = gson.fromJson(jsonData, AuctionInfo.class);

            Platform.runLater(() -> {
                try {
                    if (auctionInfo != null) {
                        // Chuyển người dùng đến màn hình chi tiết phiên đấu giá
                        SceneChanger.getInstance().toAuction(auctionInfo, userId);
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi khi chuyển cảnh từ thông báo: " + e.getMessage());
                }
            });
        }
    }
}