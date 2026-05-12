package com.mikey.auction.javagui.bidder;

// THÊM: Các thư viện GSON và Platform
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;

import com.mikey.auction.user.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.socket.RequestHandler;
import com.mikey.auction.socket.SocketClient;
import com.mikey.auction.socket.SocketListener;

public class MyAuctionController implements SocketListener {
    @FXML
    private FlowPane myAuction;
    @FXML
    private FlowPane myInterestedAuction;

    private User user; // Thêm biến cục bộ để lưu lại thông tin user
    private final Gson gson = new Gson();

    public void initialize(){
    }

    public void loadMyAuction(User user) {
        this.user = user;
        
        // 1. Đăng ký hứng dữ liệu cho trang này
        SocketClient.getInstance().setListener(this);
        
        // 2. Gửi Request lấy dữ liệu thay vì gọi trực tiếp DAO
        RequestHandler.getInstance().requestUserAuctions(user.getId());
    }

    private void renderAuctions(FlowPane flowPane, ArrayList<AuctionInfo> list, int userId) throws IOException {
        flowPane.getChildren().clear(); // Đã sửa lại để dùng đúng parameter `flowPane`
        for (AuctionInfo i : list) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mikey/auction/javagui/bidder/item.fxml"));
            Parent root = loader.load();
            ItemController itemController = loader.getController();
            itemController.setData(i);
            itemController.setUser(userId);
            flowPane.getChildren().add(root);
        }
    }

    // 3. HỨNG DỮ LIỆU TỪ SERVER TRẢ VỀ
    @Override
    public void onResponseReceived(String category, String action, String jsonData) {
        if ("AUCTION".equals(category) && "USER".equals(action)) {
            
            // Dùng GSON để dịch JSON sang dạng ArrayList<AuctionInfo>
            Type listType = new TypeToken<ArrayList<AuctionInfo>>(){}.getType();
            ArrayList<AuctionInfo> list = gson.fromJson(jsonData, listType);

            // Bắt buộc đẩy việc render giao diện (UI) vào luồng của JavaFX
            Platform.runLater(() -> {
                try {
                    if (list != null) {
                        renderAuctions(myAuction, list, user.getId());
                    }
                } catch (IOException e) {
                    System.err.println("Lỗi khi render Item trong MyAuctionController:");
                    e.printStackTrace();
                }
            });
        }
    }
}