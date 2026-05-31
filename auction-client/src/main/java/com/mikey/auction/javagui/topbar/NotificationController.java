package com.mikey.auction.javagui.topbar;

import java.time.format.DateTimeFormatter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
        RequestHandler.getInstance().requestSearchById(auctionId);
    }

    @Override
    public void onResponseReceived(String category, String action, String jsonData) {
        if ("AUCTION".equals(category) && "SEARCH_BY_ID".equals(action)) {
            if (jsonData == null || "null".equals(jsonData)) {
                System.err.println(" Không tìm thấy cuộc đấu giá tương ứng!");
                return;
            }

            try {
                // Sử dụng Gson xịn bẻ khóa LocalDateTime để không bị crash ngày tháng
                Gson customGson = new GsonBuilder()
                        .registerTypeAdapter(java.time.LocalDateTime.class, (com.google.gson.JsonDeserializer<java.time.LocalDateTime>) (json, typeOfT, context) ->
                                java.time.LocalDateTime.parse(json.getAsString(), java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .create();

                // Dịch ngược JSON thẳng về Object AuctionInfo nguyên vẹn
                AuctionInfo auctionInfo = customGson.fromJson(jsonData, AuctionInfo.class);

                if (auctionInfo != null) {
                    // Bàn giao luồng đồ họa JavaFX để bốc đầu bay màn hình
                    javafx.application.Platform.runLater(() -> {
                        try {
                            System.out.println("Notification: Đang chuyển hướng sang phiên đấu giá ID: " + auctionInfo.getId());
                            SceneChanger.getInstance().toAuction(auctionInfo, userId);
                        } catch (Exception e) {
                            System.err.println(" Lỗi gọi SceneChanger: " + e.getMessage());
                        }
                    });
                }
            } catch (Exception e) {
                System.err.println(" Lỗi bóc tách JSON ngày tháng tại Card thông báo: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}