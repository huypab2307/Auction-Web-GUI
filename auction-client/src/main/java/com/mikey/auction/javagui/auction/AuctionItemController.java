package com.mikey.auction.javagui.auction;

import com.mikey.auction.auction.AuctionStatus;
import com.mikey.auction.items.Arts;
import com.mikey.auction.items.Electronics;
import com.mikey.auction.items.ItemType;
import com.mikey.auction.items.Vehicle;
import com.mikey.auction.javagui.bidder.AutoBidDialogController;
import com.mikey.auction.javagui.login.LoginController;
import com.mikey.auction.javagui.Helper;
import com.mikey.auction.javagui.SceneChanger;
import com.mikey.auction.socket.RequestHandler;
import com.mikey.auction.socket.SocketClient;
import com.mikey.auction.socket.SocketListener;
import com.mikey.auction.user.User;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;

import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.dto.ItemSummary;
import com.mikey.auction.javagui.topbar.TopBarController;
import com.mikey.auction.javagui.topbar.SearchListener;

// THÊM THƯ VIỆN GSON CHO NGÀY THÁNG
import java.time.LocalDateTime;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

public class AuctionItemController implements SearchListener, SocketListener {
    public Button follow;
    public Button unfollow;
    @FXML private StackPane mainStackPane;
    @FXML private Label startTime, sellerName, description, curPrice, curBidder, datetime, bidStep, title, type;
    @FXML private ImageView image;
    @FXML private TilePane attributeBox;
    @FXML private TopBarController topBarController;
    @FXML private Button bidButton;
    @FXML private Pane pane;

// Đổi toàn bộ String thành Number
    @FXML 
    private LineChart<Number, Number> priceChart;
    private XYChart.Series<Number, Number> priceSeries;
    private int bidCounter = 1; // Đếm số lượt đấu để gán nhãn trục X

    private User user;
    private AuctionInfo auctionInfo;
    
    // ĐÃ FIX LỖI GSON CRASH APP
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, t, ctx) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, t, ctx) -> LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .create();

    @FXML
    public void initialize() {
        if (topBarController != null) topBarController.setListener(this);
    }

    public void setAuctionInfo(AuctionInfo auctionInfo) { this.auctionInfo = auctionInfo; }

    public void setUser(int userId) {
        this.user = LoginController.currentUser;
        if (topBarController != null) { topBarController.setUser(this.user); }
        SocketClient.getInstance().setListener(this);
        RequestHandler.getInstance().requestCheckSubscription(userId, auctionInfo.getId());
    }

    @Override
    public void onSearchPerformed(ArrayList<AuctionInfo> results) {
        SceneChanger.getInstance().toBidder(user, results);
    }

    public void renderStaticInfo() {
        try {
            ItemSummary itemSummary = auctionInfo.getItemInfo();
            title.setText(itemSummary.getTitle());
            description.setText(itemSummary.getDescription());
            sellerName.setText("Người bán: " + auctionInfo.getSellerUsername());
            bidStep.setText("• Bước giá: " + auctionInfo.getBidStep() + "đ");
            startTime.setText(auctionInfo.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            type.setText("Loại: " + itemSummary.getItemType().name());

            String path = itemSummary.getImagePath();
            if (path != null && path.startsWith("http")) image.setImage(new Image(path, true));
            else {
                URL src = getClass().getResource(path != null ? path : "/images/earth.png");
                image.setImage(src != null ? new Image(src.toExternalForm()) : new Image("/images/earth.png"));
            }
            
            pane.setStyle("-fx-padding: 40 400 40 100;" + Helper.randomColorPicker());
            RequestHandler.getInstance().requestFindItem(itemSummary.getItemType(), itemSummary.getItemId());

            String statusText = auctionInfo.getStatus().name();
            datetime.setText("Trạng thái: " + statusText + " | Kết thúc: " + auctionInfo.getEndTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            
            if (user != null && user.getUsername().equals(auctionInfo.getSellerUsername())) {
                bidButton.setVisible(false);
                bidButton.setManaged(false); 
                return;
            } else {
                bidButton.setVisible(true);
                bidButton.setManaged(true);
            }

            AuctionStatus status = auctionInfo.getStatus();
            if (status == AuctionStatus.OPEN) {
                bidButton.setDisable(false); bidButton.setText("ĐẤU GIÁ NGAY");
                bidButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;"); 
            } else if (status == AuctionStatus.PENDING) {
                bidButton.setDisable(true); bidButton.setText("PHIÊN ĐẤU GIÁ CHƯA MỞ");
                bidButton.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-font-weight: bold;");
            } else {
                bidButton.setDisable(true); bidButton.setText("PHIÊN ĐẤU GIÁ ĐÃ KẾT THÚC");
                bidButton.setStyle("-fx-background-color: #808080; -fx-text-fill: white; -fx-font-weight: bold;");
            }

            // KÉO XUỐNG CUỐI HÀM renderStaticInfo(), XÓA ĐOẠN CODE KHỞI TẠO BIỂU ĐỒ CŨ VÀ THAY BẰNG DÒNG NÀY:
            RequestHandler.getInstance().requestBidHistory(auctionInfo.getId());
        } catch (Exception e) { System.err.println(e.getMessage()); }
    }

public void updateDynamicInfo() {
        curPrice.setText(auctionInfo.getCurPrice() + "đ");
        curBidder.setText(auctionInfo.getLastBidderName() != null ? "người giữ giá: " + auctionInfo.getLastBidderName() : "Chưa có người ra giá");
        
        if (priceSeries != null) {
            String uname = auctionInfo.getLastBidderName() != null ? auctionInfo.getLastBidderName() : "Ẩn danh";
            double amt = auctionInfo.getCurPrice();
            
            // XÓA BỎ đoạn code if (priceSeries.getData().size() > 15) đi để giữ full 100% lịch sử!
            
            XYChart.Data<Number, Number> newDataPoint = new XYChart.Data<>(bidCounter++, amt);
            
            // Cài ma thuật TRƯỚC
            newDataPoint.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-background-color: transparent;");
                    javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip("Người đặt: " + uname + "\nGiá: " + amt + "đ");
                    tooltip.setStyle("-fx-font-size: 13px; -fx-background-color: #2b2b2b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
                    tooltip.setShowDelay(javafx.util.Duration.millis(100));

                    newNode.setOnMouseEntered(event -> {
                        newNode.setStyle("-fx-background-color: #2575fc, white; -fx-background-insets: 0, 2; -fx-background-radius: 5px; -fx-padding: 5px;");
                        newNode.setCursor(javafx.scene.Cursor.HAND);
                        javafx.scene.control.Tooltip.install(newNode, tooltip);
                        newNode.toFront();
                    });

                    newNode.setOnMouseExited(event -> {
                        newNode.setStyle("-fx-background-color: transparent;");
                        javafx.scene.control.Tooltip.uninstall(newNode, tooltip);
                    });
                }
            });
            
            // VẼ LÊN BIỂU ĐỒ SAU CÙNG
            priceSeries.getData().add(newDataPoint);
        }
    }

    @FXML
    public void onBidHandle(ActionEvent actionEvent) {
        bidButton.setDisable(true);
        bidButton.setText("Đang xử lý...");
        RequestHandler.getInstance().requestPlaceBid(auctionInfo.getId(), user.getId());
        
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(e -> { bidButton.setDisable(false); bidButton.setText("ĐẤU GIÁ NGAY"); });
        pause.play();
    }

    @Override
    public void onResponseReceived(String category, String action, String jsonData) {
        Platform.runLater(() -> {
            try {
                if ("ITEM".equals(category) && "FIND".equals(action)) {
                    if (!"null".equals(jsonData)) {
                        ItemType currentType = auctionInfo.getItemInfo().getItemType();
                        Map<String, String> specificInfo = null;

                        if (currentType == ItemType.ARTS) specificInfo = gson.fromJson(jsonData, Arts.class).getSpecificInfo();
                        else if (currentType == ItemType.VEHICLE) specificInfo = gson.fromJson(jsonData, Vehicle.class).getSpecificInfo();
                        else if (currentType == ItemType.ELECTRONICS) specificInfo = gson.fromJson(jsonData, Electronics.class).getSpecificInfo();

                        if (specificInfo != null) {
                            attributeBox.getChildren().clear();
                            specificInfo.forEach((lbl, val) -> attributeBox.getChildren().add(new Label(lbl + ": " + val)));
                        }
                    }
                } else if ("NOTIFICATION".equals(category) && "CHECK".equals(action)) {
                    boolean isSubscribed = gson.fromJson(jsonData, Boolean.class);
                    if (isSubscribed) handleFollow(follow, unfollow);
                    else handleFollow(unfollow, follow);
                } else if ("NOTIFICATION".equals(category) && "FOLLOW".equals(action)) {
                    if (gson.fromJson(jsonData, Boolean.class)) {
                        showCongratulationEffect(2.5); handleFollow(follow, unfollow);
                    }
                } else if ("NOTIFICATION".equals(category) && "UNFOLLOW".equals(action)) {
                    if (gson.fromJson(jsonData, Boolean.class)) {
                        showCongratulationEffect(2.5); handleFollow(unfollow, follow);
                    }
                } else if ("AUCTION".equals(category) && ("PLACEBID".equals(action) || "AUTOBID".equals(action))) {
                    if (jsonData != null && !jsonData.equals("null") && !jsonData.equals("false")) {
                        try {
                            // 1. Phân tích gói JSON trả về từ Server thành Object mang giá mới nhất
                            this.auctionInfo = gson.fromJson(jsonData, AuctionInfo.class);
                            
                            // 2. Ép các Label (nhãn) trên màn hình đổi số lập tức!
                            updateDynamicInfo(); 
                            showCongratulationEffect(2.5);
                            
                        } catch (Exception e) {
                            // Dự phòng nếu lỗi mạng, ép tải lại toàn bộ thông qua ID sản phẩm
                            RequestHandler.getInstance().requestSearchById(auctionInfo.getId());
                        }
                    } else {
                        showAlert("Lỗi", "Thao tác thất bại! Có thể giá đã bị thay đổi.");
                    }
                    
                } else if ("AUCTION".equals(category) && "SEARCH_BY_ID".equals(action)) {
                    // 👉 KHỐI NÀY VẪN GIỮ LẠI ĐỂ HỨNG DỮ LIỆU DỰ PHÒNG TỪ CÚ CATCH PHÍA TRÊN
                    if (jsonData != null && !jsonData.equals("null")) {
                        this.auctionInfo = gson.fromJson(jsonData, AuctionInfo.class);
                        updateDynamicInfo(); 
                    }
                } else if ("AUCTION".equals(category) && "UPDATE_PRICE".equals(action)) {
                    // 👉 LUỒNG HỨNG DỮ LIỆU BROADCAST TỪ SERVER KHI CÓ NGƯỜI KHÁC ĐẤU GIÁ
                    if (jsonData != null && !jsonData.equals("null")) {
                        try {
                            AuctionInfo incomingInfo = gson.fromJson(jsonData, AuctionInfo.class);
                            
                            // KIỂM TRA QUAN TRỌNG: Chỉ nhảy số nếu người này ĐANG XEM đúng cái sản phẩm bị đổi giá
                            if (this.auctionInfo != null && this.auctionInfo.getId() == incomingInfo.getId()) {
                                this.auctionInfo = incomingInfo; // Lấy giá trị mới
                                updateDynamicInfo();             // Ép giao diện nhảy số tiền
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                // CHÈN THÊM VÀO CHUỖI if-else TRONG onResponseReceived()
                } else if ("AUCTION".equals(category) && "HISTORY".equals(action)) {
                    java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<ArrayList<String>>(){}.getType();
                    ArrayList<String> historyList = gson.fromJson(jsonData, listType);

                    Platform.runLater(() -> {
                        if (priceChart != null) {
                            priceChart.getData().clear();
                            priceSeries = new XYChart.Series<>();
                            priceSeries.setName("Lịch sử giá");
                            priceChart.getData().add(priceSeries);
                            bidCounter = 1;

                            if (historyList != null && !historyList.isEmpty()) {
                                for (String record : historyList) {
                                    String[] parts = record.split("\\|");
                                    String uname = parts[0];
                                    double amt = Double.parseDouble(parts[1]);
                                    
// 1. Tạo điểm tọa độ
                                    XYChart.Data<Number, Number> dataPoint = new XYChart.Data<>(bidCounter++, amt);
                                    
                                    // 2. 👉 ÉP MA THUẬT VÀO TRƯỚC KHI VẼ
                                    dataPoint.nodeProperty().addListener((obs, oldNode, newNode) -> {
                                        if (newNode != null) {
                                            // Tàng hình
                                            newNode.setStyle("-fx-background-color: transparent;");

                                            // Setup bong bóng
                                            javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip("Người đặt: " + uname + "\nGiá: " + amt + "đ");
                                            tooltip.setStyle("-fx-font-size: 13px; -fx-background-color: #2b2b2b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
                                            tooltip.setShowDelay(javafx.util.Duration.millis(100));

                                            // Khi rê chuột vào: Hiện viền xanh dương, lõm trắng ở giữa
                                            newNode.setOnMouseEntered(event -> {
                                                newNode.setStyle("-fx-background-color: #2575fc, white; -fx-background-insets: 0, 2; -fx-background-radius: 5px; -fx-padding: 5px;");
                                                newNode.setCursor(javafx.scene.Cursor.HAND);
                                                javafx.scene.control.Tooltip.install(newNode, tooltip);
                                                newNode.toFront(); // Nổi lên trên cùng để không bị vạch kẻ đè
                                            });

                                            // Khi chuột rời đi: Tàng hình lại
                                            newNode.setOnMouseExited(event -> {
                                                newNode.setStyle("-fx-background-color: transparent;");
                                                javafx.scene.control.Tooltip.uninstall(newNode, tooltip);
                                            });
                                        }
                                    });
                                    
                                    // 3. 👉 CÀI ĐẶT XONG XUÔI MỚI NÉM LÊN BIỂU ĐỒ
                                    priceSeries.getData().add(dataPoint);
                                }
                            }
                        }
                    });
                }
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    public void followButton(ActionEvent actionEvent) { RequestHandler.getInstance().requestFollow(user.getId(), auctionInfo.getId()); }
    public void unFollowButton(ActionEvent actionEvent) { RequestHandler.getInstance().requestUnfollow(user.getId(), auctionInfo.getId()); }
    public void handleFollow(Button first, Button second){ first.setVisible(false); first.setManaged(false); second.setVisible(true); second.setManaged(true); }

    @FXML
    public void onAutoBidHandle(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mikey/auction/javagui/bidder/auto_bid_dialog.fxml"));
        Parent root = loader.load();

        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initStyle(StageStyle.TRANSPARENT);

        AutoBidDialogController controller = loader.getController();
        controller.setStage(popupStage);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        popupStage.setScene(scene);
        popupStage.showAndWait(); 

        double maxLimit = controller.getMaxPrice();
        if (maxLimit > 0) {
            // Rào lỗi logic: Không được nhập giá Max thấp hơn giá hợp lệ tiếp theo
            double requiredMin = auctionInfo.getCurPrice() + auctionInfo.getBidStep();
            if (maxLimit < requiredMin) {
                showAlert("Lỗi", "Giá tối đa phải từ " + requiredMin + "đ trở lên!");
                return;
            }

            // Gói dữ liệu để gửi đi
            com.mikey.auction.dto.AutoBidInfo autoData = new com.mikey.auction.dto.AutoBidInfo(
                    user.getId(), auctionInfo.getId(), maxLimit
            );
            
            // Gửi qua mạng lên Server kích hoạt Auto Bidding
            RequestHandler.getInstance().requestSetAutoBid(autoData);
            System.out.println("Đang gửi yêu cầu cài Auto Bid với Max Price: " + maxLimit);
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait();
    }

    private void showCongratulationEffect(double seconds) {
        ImageView animImg = new ImageView();
        try {
            URL imgUrl = getClass().getResource("/images/congratulation.gif");
            if (imgUrl != null) animImg.setImage(new Image(imgUrl.toExternalForm()));
        } catch (Exception e) { return; }

        animImg.setFitWidth(900); animImg.setPreserveRatio(true); animImg.setMouseTransparent(true);
        mainStackPane.getChildren().add(animImg);

        PauseTransition cleanup = new PauseTransition(Duration.seconds(seconds));
        cleanup.setOnFinished(event -> mainStackPane.getChildren().remove(animImg));
        cleanup.play();
    }
}