package com.mikey.auction.javagui.dashboard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.javagui.bidder.ItemController;
import com.mikey.auction.socket.RequestHandler;
import com.mikey.auction.socket.SocketClient;
import com.mikey.auction.socket.SocketListener;
import com.mikey.auction.user.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Controller hiển thị danh sách các phiên đấu giá mà người dùng quan tâm.
 */
public class DashBoardController implements SocketListener {
    @FXML
    private FlowPane myInterestedAuction; 

    private User user;
    
    // ĐIỂM SỬA DUY NHẤT: Thêm cấu hình đọc/ghi LocalDateTime cho Gson để tránh lỗi InaccessibleObjectException
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> 
                new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> 
                LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .create();

    /**
     * Thiết lập thông tin người dùng và bắt đầu quá trình tải dữ liệu.
     */
    public void setUser(User user) {
        this.user = user;
        // Đăng ký nhận dữ liệu Socket cho màn hình Dashboard
        SocketClient.getInstance().setListener(this);
        loadFollowedAuctions();
    }

    /**
     * Gửi yêu cầu lấy danh sách đấu giá quan tâm qua Socket.
     */
    private void loadFollowedAuctions() {
        if (user == null) return;
        
        // Yêu cầu Server trả về danh sách các phiên đấu giá của User thông qua RequestHandler
        RequestHandler.getInstance().requestUserAuctions(user.getId());
    }

    /**
     * Hàm hứng dữ liệu mảng AuctionInfo từ Server trả về.
     */
    @Override
    public void onResponseReceived(String category, String action, String jsonData) {
        // Kiểm tra đúng danh mục AUCTION và hành động USER dành cho Dashboard
        if ("AUCTION".equals(category) && "USER".equals(action)) {
            
            // Giải mã mảng JSON thành danh sách ArrayList<AuctionInfo> bằng GSON
            Type listType = new TypeToken<ArrayList<AuctionInfo>>(){}.getType();
            ArrayList<AuctionInfo> followedList = gson.fromJson(jsonData, listType);

            // Cập nhật giao diện trên luồng Platform.runLater để tránh lỗi JavaFX
            Platform.runLater(() -> {
                myInterestedAuction.getChildren().clear();

                if (followedList == null || followedList.isEmpty()) {
                    System.out.println("User chưa theo dõi phiên nào.");
                    return;
                }

                // Hiển thị từng phiên đấu giá lên giao diện
                for (AuctionInfo info : followedList) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mikey/auction/javagui/bidder/item.fxml"));
                        Parent itemCard = loader.load();
                        
                        ItemController controller = loader.getController();
                        controller.setData(info);
                        controller.setUser(user.getId()); // Giữ nguyên cách truyền ID của bạn
                        
                        myInterestedAuction.getChildren().add(itemCard);
                    } catch (IOException e) {
                        System.err.println("Lỗi khi nạp item card trong Dashboard: " + e.getMessage());
                    }
                }
            });
        }
    }
}