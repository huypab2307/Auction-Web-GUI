package com.template;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class MainController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    protected void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // Màu đỏ
            messageLabel.setText("Vui lòng nhập đầy đủ thông tin!");
        }
        else if ("admin".equals(username) && "123456".equals(password)) {
            messageLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;"); // Màu xanh lá
            messageLabel.setText("Đăng nhập thành công!");
            System.out.println("Chuyển hướng vào trang Sàn Đấu Giá...");
        }
        else {
            messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            messageLabel.setText("Tên đăng nhập hoặc mật khẩu không đúng.");
        }
    }
}