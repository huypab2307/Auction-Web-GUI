package com.mikey.auction.javagui.dashboard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.dto.DashboardStats;
import com.mikey.auction.javagui.bidder.ItemController;
import com.mikey.auction.socket.RequestHandler;
import com.mikey.auction.socket.SocketClient;
import com.mikey.auction.socket.SocketListener;
import com.mikey.auction.user.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class DashBoardController implements SocketListener {
    // 1. Dành cho danh sách sản phẩm theo dõi
    @FXML private FlowPane myInterestedAuction; 

    // 2. Dành cho 7 con số thống kê (Tên biến đã khớp 100% với 7 file FXML của bạn)
    @FXML private Label totalSpentValue;   // File StatCard_TotalSpent.fxml
    @FXML private Label activeBidsValue;   // File StatCard_ActiveBids.fxml
    @FXML private Label winsValue;         // File StatCard_Wins.fxml
    @FXML private Label winRateValue;      // File StatCard_WinRate.fxml
    @FXML private Label outbidCount;       // File StatCard_Outbid.fxml
    @FXML private Label watchCount;        // File StatCard_Watchlist.fxml
    @FXML private Label soldCount;         // File StatCard_Sold.fxml

    private User user;
    
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> 
                new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> 
                LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .create();

    public void setUser(User user) {
        this.user = user;
        SocketClient.getInstance().setListener(this);
        
        // Gửi 2 yêu cầu lên Server
        RequestHandler.getInstance().requestUserAuctions(user.getId());
        RequestHandler.getInstance().requestDashboardStats(user.getId());
    }

    // Hàm hỗ trợ tìm kiếm Label an toàn trong trường hợp dùng FXML rời (<fx:include>)
    private void updateLabelText(Label label, String id, String text) {
        if (label != null) {
            label.setText(text);
        } else if (myInterestedAuction != null && myInterestedAuction.getScene() != null) {
            // Quét toàn bộ giao diện để tìm ID nếu nó nằm trong file FXML con
            Label foundLabel = (Label) myInterestedAuction.getScene().lookup(id);
            if (foundLabel != null) foundLabel.setText(text);
        }
    }

    @Override
    public void onResponseReceived(String category, String action, String jsonData) {
        // NHÁNH 1: Load danh sách thẻ sản phẩm
        if ("AUCTION".equals(category) && "USER".equals(action)) {
            Type listType = new TypeToken<ArrayList<AuctionInfo>>(){}.getType();
            ArrayList<AuctionInfo> followedList = gson.fromJson(jsonData, listType);

            Platform.runLater(() -> {
                myInterestedAuction.getChildren().clear();
                if (followedList == null || followedList.isEmpty()) return;

                for (AuctionInfo info : followedList) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mikey/auction/javagui/bidder/item.fxml"));
                        Parent itemCard = loader.load();
                        ItemController controller = loader.getController();
                        controller.setData(info);
                        controller.setUser(user.getId()); 
                        myInterestedAuction.getChildren().add(itemCard);
                    } catch (IOException e) { e.printStackTrace(); }
                }
            });
        }
        
        // NHÁNH 2: Gán số liệu thống kê cho 7 Card
        else if ("AUCTION".equals(category) && "DASHBOARD".equals(action)) {
            if (jsonData != null && !jsonData.equals("null")) {
                DashboardStats stats = gson.fromJson(jsonData, DashboardStats.class);

                Platform.runLater(() -> {
                    // Dùng hàm updateLabelText để "vượt rào" các file FXML rời rạc
                    updateLabelText(totalSpentValue, "#totalSpentValue", String.format("%,.0f", stats.getTotalSpent()));
                    updateLabelText(activeBidsValue, "#activeBidsValue", String.format("%02d", stats.getActiveBids()));
                    updateLabelText(winsValue, "#winsValue", String.format("%02d", stats.getWonItems()));
                    updateLabelText(winRateValue, "#winRateValue", stats.getWinRate() + "%");
                    updateLabelText(outbidCount, "#outbidCount", String.format("%02d", stats.getOutbidCount()));
                    updateLabelText(watchCount, "#watchCount", String.format("%02d", stats.getFollowingCount()));
                    updateLabelText(soldCount, "#soldCount", String.format("%02d", stats.getSoldItems()));
                });
            }
        }
    }
}