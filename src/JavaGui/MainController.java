package JavaGui;
import User.*;
import Database.UserDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; // Thêm thư viện này
import javafx.scene.Node;       // Thêm thư viện này
import javafx.scene.Parent;     // Thêm thư viện này
import javafx.scene.Scene;      // Thêm thư viện này
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;      // Thêm thư viện này

import java.io.IOException;     // Thêm thư viện này
import java.util.Objects;       // Thêm thư viện này

public class MainController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    // Trong class MainController.java
// Trong hàm handleLogin của MainController.java
    @FXML
    protected void handleLogin(ActionEvent event) {
        UserDAO users = UserDAO.getInstance();
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            messageLabel.setText("Vui lòng nhập đầy đủ thông tin!");
        }
        else {
            // Thay đổi ở đây: Lấy đối tượng Bidder trả về
            Bidder loggedInUser = users.login(username, password);
            
            if (loggedInUser != null) {
                try {
                    // ĐĂNG NHẬP THÀNH CÔNG -> Chuyển trang và truyền User
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("auction.fxml"));
                    Parent root = loader.load();

                    // Lấy controller của trang đấu giá
                    AuctionController auctionController = loader.getController();
                    // Truyền user đang đăng nhập vào controller
                    auctionController.setCurrentUser(loggedInUser); 

                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    Scene scene = new Scene(root, 800, 600);
                    stage.setScene(scene);
                    stage.centerOnScreen();
                    stage.show();
                    
                } catch (IOException e) {
                    e.printStackTrace();
                    messageLabel.setText("Lỗi khi tải trang đấu giá!");
                }
            } else {
                messageLabel.setText("Tên đăng nhập hoặc mật khẩu không đúng.");
            }
        }
    }

    // --- THÊM ĐOẠN NÀY ĐỂ CHUYỂN TRANG ĐĂNG KÝ ---
    @FXML
    public void switchToRegister(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("register.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 400, 500);
        stage.setScene(scene);
        stage.show();
    }
}

