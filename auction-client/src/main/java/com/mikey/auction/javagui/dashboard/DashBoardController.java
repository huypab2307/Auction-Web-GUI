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
import com.mikey.auction.javagui.topbar.SearchListener;
import com.mikey.auction.javagui.topbar.TopBarController;
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

public class DashBoardController implements SocketListener, SearchListener {
    // 1. Dành cho danh sách sản phẩm theo dõi / tìm kiếm
    @FXML private FlowPane myInterestedAuction;
    @FXML private VBox activityList; 

    // 2. Dành cho 7 con số thống kê
    @FXML private Label totalSpentValue;   
    @FXML private Label activeBidsValue;   
    @FXML private Label winsValue;         
    @FXML private Label winRateValue;      
    @FXML private Label outbidCount;       
    @FXML private Label watchCount;        
    @FXML private Label soldCount;         
    @FXML private TopBarController topBarController; 

    private User user;
    
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> 
                new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> 
                LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .create();

    public void setUser(User user) {
        this.user = user;
        
        // Đăng ký làm SocketListener chính thức để nhận phản hồi từ server
        SocketClient.getInstance().setListener(this);
        
        // Gửi các yêu cầu đồng bộ dữ liệu ban đầu
        RequestHandler.getInstance().requestFollowedAuctions(user.getId());
        RequestHandler.getInstance().requestDashboardStats(user.getId());
        RequestHandler.getInstance().requestRecentActivities(user.getId());
    }

    public void initialize() {
        // Gán listener kết nối luồng sự kiện tìm kiếm từ thanh TopBar sang Dashboard
        if (topBarController != null) {
            topBarController.setListener(this);
            System.out.println("✅ Đã gán thành công bộ lắng nghe tìm kiếm cho TopBar!");
        } else {
            // Đề phòng trường hợp fx:include khởi tạo muộn, thử gán lại qua Instance (nếu có)
            Platform.runLater(() -> {
                if (TopBarController.getInstance() != null) {
                    TopBarController.getInstance().setListener(this);
                    System.out.println("✅ Đã gán thành công Listener cho TopBar qua SingleInstance!");
                } else {
                    System.err.println("❌ Không tìm thấy TopBarController! Vui lòng kiểm tra lại fx:id.");
                }
            });
        }
    }

    /**
     * 🔥 HÀM HIỂN THỊ KẾT QUẢ TÌM KIẾM
     * Triển khai từ Interface SearchListener để vẽ lại toàn bộ thẻ sản phẩm lên FlowPane công khai.
     */
    @Override
    public void onSearchPerformed(ArrayList<AuctionInfo> results) {
        Platform.runLater(() -> {
            // 1. Lưu lại danh sách ID của các sản phẩm đang được theo dõi hiện tại (nếu có)
            // trước khi xóa sạch FlowPane để render lại kết quả lọc
            List<Integer> followedItemIds = new ArrayList<>();
            myInterestedAuction.getChildren().forEach(node -> {
                if (node.getUserData() instanceof Integer) {
                    followedItemIds.add((Integer) node.getUserData());
                }
            });

            // 2. Dọn sạch giao diện danh sách cũ trên FlowPane
            myInterestedAuction.getChildren().clear();
            
            if (results == null || results.isEmpty()) {
                showNoResultPlaceholder();
                return;
            }

            boolean hasVisibleItems = false;

            // 3. Duyệt và lọc danh sách kết quả tìm kiếm
            for (AuctionInfo info : results) {
                String currentStatus = String.valueOf(info.getStatus());
                
                // Điều kiện 1: Loại bỏ các phiên đấu giá đã đóng hoặc bị hủy
                if ("CLOSED".equalsIgnoreCase(currentStatus) || "CANCELED".equalsIgnoreCase(currentStatus)) {
                    continue; 
                }

                // 🔥 ĐIỀU KIỆN QUAN TRỌNG: Nếu danh sách ID theo dõi không trống, 
                // chỉ giữ lại sản phẩm nào khớp với danh sách đang theo dõi của User
                if (!followedItemIds.isEmpty() && !followedItemIds.contains(info.getId())) {
                    continue; // Bỏ qua sản phẩm không theo dõi
                }
                
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mikey/auction/javagui/bidder/item.fxml"));
                    Parent itemCard = loader.load();
                    
                    // Gán ID sản phẩm vào UserData của Card để phục vụ cho việc lọc ở lần tìm kiếm tiếp theo
                    itemCard.setUserData(info.getId());
                    
                    ItemController controller = loader.getController();
                    controller.setData(info);
                    controller.setUser(user.getId()); 
                    
                    myInterestedAuction.getChildren().add(itemCard);
                    hasVisibleItems = true;
                } catch (IOException e) {
                    System.err.println("Lỗi nạp thẻ sản phẩm: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // Nếu sau khi lọc không có sản phẩm nào thỏa mãn thì hiện thông báo trống
            if (!hasVisibleItems) {
                showNoResultPlaceholder();
            }
        });
    }

    // Hàm hỗ trợ hiển thị chữ thông báo trống khi lọc không ra kết quả
    private void showNoResultPlaceholder() {
        Label noResultLabel = new Label("Không tìm thấy phiên đấu giá nào trong danh sách theo dõi.");
        noResultLabel.setTextFill(Color.GRAY);
        noResultLabel.setFont(Font.font("System", 14));
        noResultLabel.setPadding(new Insets(20));
        myInterestedAuction.getChildren().add(noResultLabel);
    }

    // Hàm hỗ trợ tìm kiếm Label an toàn trong các tệp FXML rời (<fx:include>)
    private void updateLabelText(Label label, String id, String text) {
        if (label != null) {
            label.setText(text);
        } else if (myInterestedAuction != null && myInterestedAuction.getScene() != null) {
            Label foundLabel = (Label) myInterestedAuction.getScene().lookup(id);
            if (foundLabel != null) foundLabel.setText(text);
        }
    }

    @Override
    public void onResponseReceived(String category, String action, String jsonData) {
        // NHÁNH 1: Load danh sách thẻ sản phẩm đang theo dõi ban đầu
        if ("AUCTION".equals(category) && "FOLLOWED_LIST".equals(action)) {
            Type listType = new TypeToken<ArrayList<AuctionInfo>>(){}.getType();
            ArrayList<AuctionInfo> followedList = gson.fromJson(jsonData, listType);
            this.onSearchPerformed(followedList);
        }
        
        // 🔥 NHÁNH BỔ SUNG: Trực tiếp nhận gói tin SEARCH từ Server trả về qua Socket mạng
        else if ("AUCTION".equals(category) && ("SEARCH".equals(action) || "GET_ALL".equals(action))) {
            try {
                Type listType = new TypeToken<ArrayList<AuctionInfo>>(){}.getType();
                ArrayList<AuctionInfo> searchResults = gson.fromJson(jsonData, listType);
                this.onSearchPerformed(searchResults);
            } catch (Exception e) {
                System.err.println("Lỗi xử lý parse chuỗi JSON tìm kiếm: " + e.getMessage());
            }
        }
        
        // NHÁNH 2: Tải danh sách hoạt động gần đây
        else if ("AUCTION".equals(category) && "GET_ACTIVITIES".equals(action)) { 
            Type stringListType = new TypeToken<ArrayList<String>>(){}.getType();
            ArrayList<String> activitiesList = gson.fromJson(jsonData, stringListType);
            this.loadRecentActivities(activitiesList);
        }
        
        // NHÁNH 3: Gán số liệu thống kê cho 7 Card
        else if ("AUCTION".equals(category) && "DASHBOARD".equals(action)) {
            if (jsonData != null && !jsonData.equals("null")) {
                DashboardStats stats = gson.fromJson(jsonData, DashboardStats.class);

                Platform.runLater(() -> {
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
        
        // NHÁNH 4: Cập nhật hoạt động trả giá thời gian thực
        else if ("AUCTION".equals(category) && "PLACEBID".equals(action)) {
            String message = gson.fromJson(jsonData, String.class); 
            this.addSingleActivity(message);
        }
        
        // NHÁNH 5: Cập nhật hoạt động theo dõi thời gian thực
        else if ("NOTIFICATION".equals(category) && "FOLLOW".equals(action)) {
            String message = gson.fromJson(jsonData, String.class);
            this.addSingleActivity(message);
        }
    }

    public void loadRecentActivities(List<String> activities) {
        Platform.runLater(() -> {
            activityList.getChildren().clear();
            activityList.setSpacing(10); 
            activityList.setPadding(new Insets(15)); 

            if (activities == null || activities.isEmpty()) {
                Label noActivity = new Label("Chưa có hoạt động nào gần đây.");
                noActivity.setTextFill(Color.GRAY);
                noActivity.setFont(Font.font("System", 14));
                activityList.getChildren().add(noActivity);
                return;
            }

            for (String activityText : activities) {
                HBox activityRow = new HBox();
                activityRow.setAlignment(Pos.CENTER_LEFT);
                activityRow.setSpacing(12);
                activityRow.setPadding(new Insets(8, 12, 8, 12));
                
                activityRow.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 6;");
                activityRow.setOnMouseEntered(e -> activityRow.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 6;"));
                activityRow.setOnMouseExited(e -> activityRow.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 6;"));

                Label icon = new Label("•"); 
                icon.setTextFill(Color.web("#6366f1")); 
                icon.setFont(Font.font("System", 20));

                Label content = new Label(activityText);
                content.setTextFill(Color.web("#334155")); 
                content.setFont(Font.font("System", 14));

                activityRow.getChildren().addAll(icon, content);
                activityList.getChildren().add(activityRow);
            }
        });
    }

    public void addSingleActivity(String activityText) {
        if (activityText == null || activityText.trim().isEmpty() || "null".equalsIgnoreCase(activityText.trim())) {
            return;
        }
        
        Platform.runLater(() -> {
            try {
                if (!activityList.getChildren().isEmpty()) {
                    javafx.scene.Node firstChild = activityList.getChildren().get(0);
                    if (firstChild instanceof Label) {
                        Label labelPlaceholder = (Label) firstChild;
                        if ("Chưa có hoạt động nào gần đây.".equals(labelPlaceholder.getText())) {
                            activityList.getChildren().clear();
                        }
                    }
                }

                HBox activityRow = new HBox();
                activityRow.setAlignment(Pos.CENTER_LEFT);
                activityRow.setSpacing(12);
                activityRow.setPadding(new Insets(8, 12, 8, 12));
                
                activityRow.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 6;");
                activityRow.setOnMouseEntered(e -> activityRow.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 6;"));
                activityRow.setOnMouseExited(e -> activityRow.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 6;"));

                Label icon = new Label("•"); 
                icon.setTextFill(Color.web("#6366f1")); 
                icon.setFont(Font.font("System", 20));

                Label content = new Label(activityText);
                content.setTextFill(Color.web("#334155")); 
                content.setFont(Font.font("System", 14));
                content.setWrapText(true); 

                activityRow.getChildren().addAll(icon, content);
                activityList.getChildren().add(0, activityRow);

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