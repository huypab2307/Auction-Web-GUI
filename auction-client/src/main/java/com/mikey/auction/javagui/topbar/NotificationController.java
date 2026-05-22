package com.mikey.auction.javagui.topbar;

import java.time.format.DateTimeFormatter;

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
import javafx.scene.input.MouseEvent; // THÊM IMPORT NÀY
import javafx.scene.text.Text;

public class NotificationController implements SocketListener {
    // ĐÃ SỬA: Đổi tên và kiểu dữ liệu cho khớp chính xác với notificationCard.fxml
    @FXML private Text mainContent; 
    @FXML private Label timeLabel;

    private int auctionId;
    private int notificationId;
    private int userId;
    private final Gson gson = new Gson();

    public void setContent(Notifications notification) {
        this.auctionId = notification.getAuctionId(); 
        this.notificationId = notification.getId(); 
        this.userId = notification.getUserId(); 

        // ĐÃ SỬA: Gọi đúng tên biến giao diện
        mainContent.setText(notification.getMessage()); 
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
        timeLabel.setText(notification.getCreatedAt().format(formatter)); 
    }

    @FXML
    public void handleCardClick(MouseEvent mouseEvent) {
        SocketClient.getInstance().setListener(this);
        RequestHandler.getInstance().requestMarkAsRead(userId, notificationId);
        RequestHandler.getInstance().requestFindItem(null, auctionId); 
    }

    @Override
    public void onResponseReceived(String category, String action, String jsonData) {
        if ("ITEM".equals(category) && "FIND".equals(action)) {
            AuctionInfo auctionInfo = gson.fromJson(jsonData, AuctionInfo.class);

            Platform.runLater(() -> {
                try {
                    if (auctionInfo != null) {
                        SceneChanger.getInstance().toAuction(auctionInfo, userId);
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi khi chuyển cảnh từ thông báo: " + e.getMessage());
                }
            });
        }
    }
}