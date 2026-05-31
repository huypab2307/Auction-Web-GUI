package com.mikey.auction.javagui.user;

import com.mikey.auction.javagui.SceneChanger;
import com.mikey.auction.socket.RequestHandler;
import com.mikey.auction.socket.SocketClient;
import com.mikey.auction.socket.SocketListener;
import com.mikey.auction.user.User;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class ChangePasswordController implements SocketListener {

    @FXML public PasswordField oldPasswordField;
    @FXML public PasswordField newPasswordField;
    public User user;
    public Stage stage;

    @FXML
    public void initialize() {
        // Đợi giao diện load vào Scene xong mới gắn phím tắt
        Platform.runLater(() -> {
            // Lấy Scene thông qua oldPasswordField (hoặc newPasswordField đều được)
            if (oldPasswordField != null && oldPasswordField.getScene() != null) {
                oldPasswordField.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    // Nếu ấn ESCAPE -> Thoát
                    if (event.getCode() == KeyCode.ESCAPE) {
                        handleCancel(null);
                        event.consume(); // Ngăn sự kiện phím lan truyền
                    }
                    // Nếu ấn ENTER -> Cập nhật mật khẩu
                    else if (event.getCode() == KeyCode.ENTER) {
                        handleUpdatePassword(null);
                        event.consume();
                    }
                });
            }
        });
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void handleCancel(ActionEvent actionEvent) {
        SceneChanger.getInstance().openSettings(stage, user);
    }

    public void handleUpdatePassword(ActionEvent actionEvent) {
        // Kiểm tra xem ô nhập có bị bỏ trống không trước khi gửi
        if (oldPasswordField.getText().trim().isEmpty() || newPasswordField.getText().trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Vui lòng nhập đầy đủ mật khẩu cũ và mới!");
            alert.showAndWait();
            return;
        }

        // 1. Đăng ký hứng kết quả đổi mật khẩu từ Server
        SocketClient.getInstance().setListener(this);
        
        // 2. Gửi yêu cầu qua Socket thay vì gọi UserDAO trực tiếp
        RequestHandler.getInstance().requestChangePassword(
            user.getId(), 
            oldPasswordField.getText(), 
            newPasswordField.getText()
        );
    }

    // 3. Hứng dữ liệu trả về và hiển thị thông báo
    @Override
    public void onResponseReceived(String category, String action, String jsonData) {
        if ("USER".equals(category) && "CHANGE_PASSWORD".equals(action)) {
            if ("true".equals(jsonData)) {
                javafx.application.Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Đổi mật khẩu thành công!");
                    if (this.stage != null) {
                        alert.initOwner(this.stage);
                    }
                    alert.showAndWait();
                    try {
                        SceneChanger.getInstance().openSettings(this.stage, user);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {
                javafx.application.Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Mật khẩu cũ không chính xác!");
                    if (this.stage != null) {
                        alert.initOwner(this.stage);
                    }
                    alert.showAndWait();
                });
            }
        }
    }
}