package com.mikey.auction.javagui.topbar;


import java.io.IOException;
import java.util.List;

import com.mikey.auction.auction.Notifications;
import com.mikey.auction.javagui.SceneChanger;
import com.mikey.auction.manager.NotificationManager;
import com.mikey.auction.socket.RequestHandler;
import com.mikey.auction.user.User;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;


public class TopBarController {
    @FXML private TextField searchField;
    @FXML private ToggleButton searchButton;
    @FXML private MenuButton notification;
    @FXML private VBox mainContainer;
    @FXML private ImageView avatar;

    private SearchListener listener;
    private User user;
    private static TopBarController instance;
    public static TopBarController getInstance() { return instance; }
    
     public void setListener(SearchListener listener) {
        this.listener = listener;
    }

    @FXML
    public void searchHandle() {
        String keyword = searchField.getText().trim().toUpperCase();
        if (keyword.isEmpty()) {
            RequestHandler.getInstance().requestAllAuctions();
        } else {
            RequestHandler.getInstance().requestSearch(keyword);
        }
    }
    public void initialize(){
        instance = this;
        avatar.setPreserveRatio(false);
        if (searchButton != null) {
            searchButton.setDisable(true);
        }
        
        // Tạo một hình tròn (Clip) có tâm (20,20) và bán kính 20 
        // Vừa khít với fitHeight="40.0" fitWidth="40.0" trong file fxml của bạn
        Circle clip = new Circle(20, 20, 20);
        avatar.setClip(clip);

        if (searchField != null) {
            searchField.setOnKeyPressed(event -> {
                // Nếu phím được ấn là phím ENTER
                if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                    try {
                        searchHandle(); // Gọi thẳng hàm xử lý tìm kiếm
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
    @FXML
    public void onKeySearchHandle(){
        String text = searchField.getText();
        boolean disable = text.isEmpty() || text.trim().isEmpty();
        searchButton.setDisable(disable);
    }
    @FXML
    public void logoutHandle(){
        SceneChanger.getInstance().toLogin();
    }
  public void setUser(User user) {
    this.user = user;
    
    if (user != null && user.getUsername() != null && avatar != null) {
        // 1. Tự động dựng URL Cloudinary từ username + phá cache bằng timestamp (?t=...)
        String avatarUrl = "https://res.cloudinary.com/devnd8ndw/image/upload/v1/auction_avatars/avatar_" 
                            + user.getUsername() + ".jpg?t=" + System.currentTimeMillis();
        
        try {
            // 2. Tải ảnh bất đồng bộ (true) - Không làm khựng giao diện thanh TopBar
            Image img = new Image(avatarUrl, true);
            
            // 3. Theo dõi tiến trình tải ảnh (Khi hoàn thành tiến trình đạt 1.0)
            img.progressProperty().addListener((observable, oldProgress, newProgress) -> {
                if (newProgress.doubleValue() == 1.0 && !img.isError()) {
                    // ĐÃ TẢI XONG THÀNH CÔNG: Chuyển về luồng UI chính để hiển thị ảnh
                    javafx.application.Platform.runLater(() -> {
                        avatar.setImage(img);
                    });
                }
            });
            
            // 4. Theo dõi nếu xảy ra lỗi (User mới chưa up ảnh, lỗi mạng 404...)
            img.errorProperty().addListener((observable, oldHasError, newHasError) -> {
                if (newHasError) {
                    System.out.println("TopBar: Không tìm thấy avatar trên Cloudinary cho user '" + user.getUsername() + "'. Hiển thị ảnh mặc định.");
                }
            });
            
        } catch (Exception e) {
            System.err.println("Lỗi khởi tạo tiến trình Image ở TopBar: " + e.getMessage());
        }
    }
}
    public void toHubHandle(ActionEvent actionEvent) {
        if (user != null) {
            SceneChanger.getInstance().toBidder(user);
        } else {
            // no user info available: fallback to login
            SceneChanger.getInstance().toLogin();
        }
    }

    @FXML
    public void userGuiHandle(ActionEvent actionEvent) {
        SceneChanger.getInstance().openSettings(new Stage(), user);
//        Stage stage = new Stage();
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mikey/auction/javagui/user/User.fxml"));
//            Parent root = loader.load();
//            UserController controller = loader.getController();
//            controller.setUser(user);
//            stage.setTitle("User");
//            stage.setResizable(false);
//            stage.setAlwaysOnTop(true);
//            stage.setScene(new javafx.scene.Scene(root));
//            stage.show();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
    @FXML
    public void sellerGuiHandle(ActionEvent actionEvent) {
        if (user != null) {
            SceneChanger.getInstance().toSellerHubGui(user);
        } else {
            SceneChanger.getInstance().toLogin();
        }
    }

    @FXML
    public void showNotification() throws IOException {
        List<Notifications> list = NotificationManager.getInstance().findNotififications(user.getId());
        mainContainer.getChildren().clear();
        for (Notifications notification : list){
            FXMLLoader loader = new FXMLLoader(getClass().getResource("notificationCard.fxml"));
            Parent root = loader.load();
            String color = (notification.isRead()) ? "white" : "#c9efc9";
            root.setStyle("-fx-background-color: " + color);
            NotificationController notificationController = loader.getController();
            notificationController.setContent(notification);
            mainContainer.getChildren().add(root);
        }
    }

    public void userConfigHandle(ActionEvent actionEvent) {
    }

    @FXML
    public void handleOpenMyInvoices(ActionEvent actionEvent) {
        if (user != null) {
            // Gọi SceneChanger để chuyển cảnh
            SceneChanger.getInstance().toMyInvoices(user);
        } else {
            SceneChanger.getInstance().toLogin();
        }
    }
    
    // 🔥 THÊM HÀM NÀY: Để nhận trực tiếp ảnh từ UserController truyền sang
    public void updateAvatarImmediately(Image img) {
        if (avatar != null && img != null) {
            javafx.application.Platform.runLater(() -> {
                avatar.setImage(img);
            });
        }
    }
}
