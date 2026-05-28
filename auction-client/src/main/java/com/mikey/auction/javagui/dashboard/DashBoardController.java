package com.mikey.auction.javagui.dashboard;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class DashBoardController implements SocketListener {
    // 1. Dành cho danh sách sản phẩm theo dõi
    @FXML private FlowPane myInterestedAuction;
    @FXML private VBox activityList; // Cái hộp trắng bo góc chứa danh sách hoạt động

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
        //RequestHandler.getInstance().requestRecentActivities(user.getId());
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
        // -----------------------------------------------------------------
    // 🔥 NHÁNH 2: THÊM VÀO ĐÂY - Load danh sách hoạt động gần đây
    // -----------------------------------------------------------------
    else if ("AUCTION".equals(category) && "HISTORY".equals(action)) { 
        // 1. Phân rã chuỗi JSON nhận từ Server thành List<String>
        Type stringListType = new TypeToken<ArrayList<String>>(){}.getType();
        ArrayList<String> activitiesList = gson.fromJson(jsonData, stringListType);

        // 2. Gọi hàm render giao diện cho thanh màu trắng dưới cùng
        // (Hàm này đã tự bọc Platform.runLater() ở bước trước nên gọi trực tiếp luôn)
        this.loadRecentActivities(activitiesList);
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
        else if ("AUCTION".equals(category) && "PLACEBID".equals(action)) {
            // Dùng Gson giải mã chuỗi chuẩn nguyên bản (Tránh lỗi ký tự đặc biệt)
            String message = gson.fromJson(jsonData, String.class); 
            // Gọi hàm để đẩy ngay lên giao diện
            this.addSingleActivity(message);
        }
        
        // 🔥 NHÁNH BỔ SUNG 2: Khi Server báo Theo dõi (Follow) thành công
        else if ("NOTIFICATION".equals(category) && "FOLLOW".equals(action)) {
            // Dùng Gson giải mã chuỗi chuẩn nguyên bản
            String message = gson.fromJson(jsonData, String.class);
            this.addSingleActivity(message);
        }
    }
    public void loadRecentActivities(List<String> activities) {
    // Ép chạy trên luồng UI của JavaFX để tránh lỗi văng app khi nhận dữ liệu từ Socket
        Platform.runLater(() -> {
        // 1. Xóa sạch các hoạt động cũ trước đó
        activityList.getChildren().clear();
        activityList.setSpacing(10); // Khoảng cách giữa các dòng hoạt động
        activityList.setPadding(new Insets(15)); // Lề bên trong hộp trắng

        if (activities == null || activities.isEmpty()) {
            // Nếu không có hoạt động nào, hiển thị một dòng thông báo mờ
            Label noActivity = new Label("Chưa có hoạt động nào gần đây.");
            noActivity.setTextFill(Color.GRAY);
            noActivity.setFont(Font.font("System", 14));
            activityList.getChildren().add(noActivity);
            return;
        }

        // 2. Duyệt qua danh sách để tạo giao diện cho từng dòng
        for (String activityText : activities) {
            // Tạo một HBox để chứa Icon + Chữ cho mỗi dòng hoạt động
            HBox activityRow = new HBox();
            activityRow.setAlignment(Pos.CENTER_LEFT);
            activityRow.setSpacing(12);
            activityRow.setPadding(new Insets(8, 12, 8, 12));
            
            // Hiệu ứng hover cho từng dòng giống các app hiện đại (tùy chọn)
            activityRow.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 6;");
            activityRow.setOnMouseEntered(e -> activityRow.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 6;"));
            activityRow.setOnMouseExited(e -> activityRow.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 6;"));

            // Tạo icon giả lập bằng text ký tự đặc biệt (hoặc dấu chấm tròn màu tím thương hiệu)
            Label icon = new Label("•"); 
            icon.setTextFill(Color.web("#6366f1")); // Màu tím Cosmic
            icon.setFont(Font.font("System", 20));

            // Tạo Label chứa nội dung hoạt động thực tế
            Label content = new Label(activityText);
            content.setTextFill(Color.web("#334155")); // Màu chữ xám đen sang trọng
            content.setFont(Font.font("System", 14));

            // Nhét icon và nội dung vào dòng, rồi nhét dòng đó vào hộp tổng
            activityRow.getChildren().addAll(icon, content);
            activityList.getChildren().add(activityRow);
        }
    });
}
    public void addSingleActivity(String activityText) {
    // 1. Kiểm tra dữ liệu đầu vào dữ dội hơn để tránh chuỗi rỗng
    if (activityText == null || activityText.trim().isEmpty() || "null".equalsIgnoreCase(activityText.trim())) {
        return;
    }
    
    // Ép chạy trên luồng UI của JavaFX để tránh lỗi văng app
    Platform.runLater(() -> {
        try {
            // 2. Xóa dòng chữ mờ "Chưa có hoạt động nào gần đây." một cách an toàn
            if (!activityList.getChildren().isEmpty()) {
                javafx.scene.Node firstChild = activityList.getChildren().get(0);
                
                // Nếu phần tử đầu tiên là một Label (chính là dòng chữ mờ ban đầu) thì xóa sạch
                if (firstChild instanceof Label) {
                    Label labelPlaceholder = (Label) firstChild;
                    if ("Chưa có hoạt động nào gần đây.".equals(labelPlaceholder.getText())) {
                        activityList.getChildren().clear();
                    }
                }
            }

            // 3. Tạo giao diện dòng hoạt động mới (HBox chứa Icon + Chữ)
            HBox activityRow = new HBox();
            activityRow.setAlignment(Pos.CENTER_LEFT);
            activityRow.setSpacing(12);
            activityRow.setPadding(new Insets(8, 12, 8, 12));
            
            // Hiệu ứng hover đồng bộ với các dòng khác
            activityRow.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 6;");
            activityRow.setOnMouseEntered(e -> activityRow.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 6;"));
            activityRow.setOnMouseExited(e -> activityRow.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 6;"));

            // Tạo icon dấu chấm tròn màu tím Cosmic
            Label icon = new Label("•"); 
            icon.setTextFill(Color.web("#6366f1")); 
            icon.setFont(Font.font("System", 20));

            // Tạo Label chứa nội dung hoạt động
            Label content = new Label(activityText);
            content.setTextFill(Color.web("#334155")); 
            content.setFont(Font.font("System", 14));
            content.setWrapText(true); // Tự động xuống dòng nếu text quá dài không lo bị che mất

            activityRow.getChildren().addAll(icon, content);

            // 4. CHÈN VÀO VỊ TRÍ ĐẦU TIÊN (Chỉ số 0) để hoạt động mới nhất luôn nằm trên cùng
            activityList.getChildren().add(0, activityRow);

            // 5. Giới hạn tối đa hiển thị 10 hoạt động cho đỡ chật màn hình
            while (activityList.getChildren().size() > 10) {
                activityList.getChildren().remove(activityList.getChildren().size() - 1);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi render addSingleActivity: " + e.getMessage());
            e.printStackTrace();
        }
    });
}
}