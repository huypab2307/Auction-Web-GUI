package com.mikey.auction.javagui.auction;

import com.google.gson.Gson;
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

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;

import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.dto.ItemSummary;
import com.mikey.auction.javagui.topbar.TopBarController;
import com.mikey.auction.javagui.topbar.SearchListener;

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

    private User user;
    private AuctionInfo auctionInfo;
    private final Gson gson = new Gson();

    @FXML
    public void initialize() {
        if (topBarController != null) topBarController.setListener(this);
    }

    public void setAuctionInfo(AuctionInfo auctionInfo) {
        this.auctionInfo = auctionInfo;
    }

    public void setUser(int userId) {
        // Lấy thông tin đối tượng User từ biến toàn cục lúc đăng nhập
        this.user = LoginController.currentUser;
        
        if (topBarController != null) {
            topBarController.setUser(this.user);
        }
        
        // Đăng ký Listener và kiểm tra xem User này đã Follow phiên đấu giá chưa
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
            
            // Yêu cầu lấy thông số kỹ thuật (Specific Info) từ Server
            RequestHandler.getInstance().requestFindItem(itemSummary.getItemType(), itemSummary.getItemId());

            String statusText = auctionInfo.getStatus().name();
            datetime.setText("Trạng thái: " + statusText + " | Kết thúc: " + auctionInfo.getEndTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            
            // So sánh Tên người bán và Tên User hiện tại để ẩn nút Bid nếu là người bán
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
                bidButton.setDisable(false);
                bidButton.setText("ĐẤU GIÁ NGAY");
                bidButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;"); 
            } else if (status == AuctionStatus.PENDING) {
                bidButton.setDisable(true);
                bidButton.setText("PHIÊN ĐẤU GIÁ CHƯA MỞ");
                bidButton.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-font-weight: bold;");
            } else {
                bidButton.setDisable(true);
                bidButton.setText("PHIÊN ĐẤU GIÁ ĐÃ KẾT THÚC");
                bidButton.setStyle("-fx-background-color: #808080; -fx-text-fill: white; -fx-font-weight: bold;");
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void updateDynamicInfo() {
        curPrice.setText(auctionInfo.getCurPrice() + "đ");
        curBidder.setText(auctionInfo.getLastBidderName() != null ? "người giữ giá: " + auctionInfo.getLastBidderName() : "Chưa có người ra giá");
    }

    @FXML
    public void onBidHandle(ActionEvent actionEvent) {
        bidButton.setDisable(true);
        bidButton.setText("Đang xử lý...");

        // Gửi Request đặt giá thay vì gọi Manager
        RequestHandler.getInstance().requestPlaceBid(auctionInfo.getId(), user.getId());
        
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(e -> {
            bidButton.setDisable(false);
            bidButton.setText("ĐẤU GIÁ NGAY");
        });
        pause.play();
    }

    // ==== HÀM HỨNG TẤT CẢ DỮ LIỆU TỪ SERVER ====
    @Override
    public void onResponseReceived(String category, String action, String jsonData) {
        Platform.runLater(() -> {
            try {
                // Hứng chi tiết cấu hình Item
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
                }
                // Hứng kết quả kiểm tra Follow lúc mới mở màn hình
                else if ("NOTIFICATION".equals(category) && "CHECK".equals(action)) {
                    boolean isSubscribed = gson.fromJson(jsonData, Boolean.class);
                    if (isSubscribed) handleFollow(follow, unfollow);
                    else handleFollow(unfollow, follow);
                }
                // Hứng kết quả bấm nút Follow
                else if ("NOTIFICATION".equals(category) && "FOLLOW".equals(action)) {
                    if (gson.fromJson(jsonData, Boolean.class)) {
                        showCongratulationEffect(2.5);
                        handleFollow(follow, unfollow);
                    }
                }
                // Hứng kết quả bấm nút Unfollow
                else if ("NOTIFICATION".equals(category) && "UNFOLLOW".equals(action)) {
                    if (gson.fromJson(jsonData, Boolean.class)) {
                        showCongratulationEffect(2.5);
                        handleFollow(unfollow, follow);
                    }
                }
                // Hứng kết quả Đặt giá
                else if ("AUCTION".equals(category) && "PLACEBID".equals(action)) {
                    boolean success = gson.fromJson(jsonData, Boolean.class);
                    if (success) {
                        showCongratulationEffect(2.5);
                        // Lấy lại thông tin giá mới nhất để cập nhật màn hình
                        RequestHandler.getInstance().requestSearchById(auctionInfo.getId());
                    } else {
                        showAlert("Lỗi", "Đặt giá thất bại! Vui lòng thử lại.");
                    }
                }
                // Hứng thông tin Auction mới nhất (sau khi đặt giá thành công)
                else if ("AUCTION".equals(category) && "SEARCH_BY_ID".equals(action)) {
                    this.auctionInfo = gson.fromJson(jsonData, AuctionInfo.class);
                    updateDynamicInfo(); // Cập nhật lại giá tiền và tên người giữ giá
                }
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    public void followButton(ActionEvent actionEvent) {
        RequestHandler.getInstance().requestFollow(user.getId(), auctionInfo.getId());
    }

    public void unFollowButton(ActionEvent actionEvent) {
        RequestHandler.getInstance().requestUnfollow(user.getId(), auctionInfo.getId());
    }

    public void handleFollow(Button first, Button second){
        first.setVisible(false);
        first.setManaged(false);
        second.setVisible(true);
        second.setManaged(true);
    }

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

        double result = controller.getMaxPrice();
        if (result > 0) {
            System.out.println("Kích hoạt Max Bid: " + result);
            showCongratulationEffect(1.5);
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showCongratulationEffect(double seconds) {
        ImageView animImg = new ImageView();
        try {
            URL imgUrl = getClass().getResource("/images/congratulation.gif");
            if (imgUrl != null) animImg.setImage(new Image(imgUrl.toExternalForm()));
        } catch (Exception e) { return; }

        animImg.setFitWidth(900);
        animImg.setPreserveRatio(true);
        animImg.setMouseTransparent(true);
        mainStackPane.getChildren().add(animImg);

        PauseTransition cleanup = new PauseTransition(Duration.seconds(seconds));
        cleanup.setOnFinished(event -> mainStackPane.getChildren().remove(animImg));
        cleanup.play();
    }
}