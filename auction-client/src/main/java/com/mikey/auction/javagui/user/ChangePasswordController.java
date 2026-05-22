package com.mikey.auction.javagui.user;

import com.mikey.auction.javagui.SceneChanger;
import com.mikey.auction.socket.RequestHandler;
import com.mikey.auction.socket.SocketClient;
import com.mikey.auction.socket.SocketListener;
import com.mikey.auction.user.User;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class ChangePasswordController implements SocketListener {

    public PasswordField oldPasswordField;
    public PasswordField newPasswordField;
    public User user;
    public Stage stage;

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
            // Bắt buộc dùng Platform.runLater khi hiển thị Alert/UI
            Platform.runLater(() -> {
                // Server của chúng ta trả về "SUCCESS" nếu đổi thành công
                if ("true".equals(jsonData)) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Đổi mật khẩu thành công!");
                    alert.showAndWait();
                    // Quay lại màn hình cài đặt
                    SceneChanger.getInstance().openSettings(stage, user);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Mật khẩu cũ không chính xác!");
                    alert.showAndWait();
                }
            });
        }
    }
}