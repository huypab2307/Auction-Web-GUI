package com.mikey.auction.javagui.seller;

// THÊM: Các thư viện hỗ trợ JSON và Socket
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mikey.auction.socket.RequestHandler;
import com.mikey.auction.socket.SocketClient;
import com.mikey.auction.socket.SocketListener;
import javafx.application.Platform;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.javagui.topbar.TopBarController;
import com.mikey.auction.user.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.FlowPane;

// 1. THÊM: implements SocketListener
public class SellerListController implements SocketListener {
    @FXML private FlowPane sellerItemContainer;
    @FXML private TopBarController topBarController;
    private User user;
    private final Gson gson = new Gson(); // Khởi tạo Gson

    // Đã xóa hàm setUser(int userId) sử dụng UserDAO để cắt đứt DB ở Client

    public void loadMyProducts(User user) {
        this.user = user;
        
        // Cập nhật user cho TopBar nếu có
        if (topBarController != null) {
            topBarController.setUser(this.user);
        }

        // 2. Đăng ký nhận dữ liệu và gửi Request lên Server
        SocketClient.getInstance().setListener(this);
        RequestHandler.getInstance().requestUserAuctions(user.getId());
    }

    private void renderAuctions(FlowPane flowPane, ArrayList<AuctionInfo> list, int userId) throws IOException {
        flowPane.getChildren().clear(); 
        for (AuctionInfo i : list) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("seller_item.fxml"));
            Parent root = loader.load();
            
            SellerItemController itemController = loader.getController();
            itemController.setData(i);
            itemController.setUser(userId);
            
            flowPane.getChildren().add(root);
        }
    }

    // 3. THÊM: Hàm hứng kết quả danh sách sản phẩm từ Server
    @Override
    public void onResponseReceived(String category, String action, String jsonData) {
        if ("AUCTION".equals(category) && "USER".equals(action)) {
            // Dùng GSON để dịch JSON sang dạng ArrayList<AuctionInfo>
            Type listType = new TypeToken<ArrayList<AuctionInfo>>(){}.getType();
            ArrayList<AuctionInfo> myProducts = gson.fromJson(jsonData, listType);

            // Đưa việc vẽ giao diện vào luồng Platform.runLater
            Platform.runLater(() -> {
                try {
                    if (myProducts != null) {
                        renderAuctions(sellerItemContainer, myProducts, user.getId());
                    }
                } catch (IOException e) {
                    System.err.println("Lỗi render danh sách sản phẩm của Seller: " + e.getMessage());
                }
            });
        }
    }
}