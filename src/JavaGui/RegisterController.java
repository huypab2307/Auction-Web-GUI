package JavaGui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import User.*;
import Database.UserDAO;

public class RegisterController {

    @FXML
    private TextField regUserField;

    @FXML
    private PasswordField regPassField;

    @FXML
    private PasswordField regConfirmPassField; // Thêm trường xác nhận mật khẩu

    @FXML
    private Label regMessageLabel;

    @FXML
    protected void handleRegister(ActionEvent event) {
        String username = regUserField.getText();
        String password = regPassField.getText();
        String confirmPassword = regConfirmPassField.getText();

        // 1. Kiểm tra xem có bỏ trống trường nào không
        if (username.trim().isEmpty() || password.trim().isEmpty() || confirmPassword.trim().isEmpty()) {
            regMessageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            regMessageLabel.setText("Vui lòng nhập đầy đủ thông tin!");
        } 
        // 2. Kiểm tra mật khẩu nhập lại có khớp không
        else if (!password.equals(confirmPassword)) {
            regMessageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            regMessageLabel.setText("Mật khẩu nhập lại không khớp!");
        } 
        // 3. Nếu mọi thứ hợp lệ
        else {
            UserDAO users = UserDAO.getInstance();
            users.register(username, password); 
            
            regMessageLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
            regMessageLabel.setText("Đăng ký thành công! Hãy đăng nhập.");
        }
    }

    @FXML
    public void switchToLogin(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("main.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 400, 500); 
        stage.setScene(scene);
        stage.show();
    }
}