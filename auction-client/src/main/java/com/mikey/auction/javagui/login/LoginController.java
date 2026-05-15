package com.mikey.auction.javagui.login;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mikey.auction.javagui.SceneChanger;
import com.mikey.auction.user.User;
import com.mikey.auction.user.Admin;
import com.mikey.auction.user.Bidder;
import com.mikey.auction.user.Seller;
import com.mikey.auction.socket.SocketClient;
import com.mikey.auction.socket.SocketListener;
import com.mikey.auction.socket.RequestHandler;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController implements SocketListener {
    // Đã thêm biến toàn cục ở đây
    public static User currentUser; 

    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private Button loginButton;
    @FXML private Label status;

    private Gson gson = new Gson();

    public void initialize() {
        loginButton.setDisable(true); 
        new Thread(() -> {
            try {
                SocketClient.getInstance().connect("localhost", 12345);
                SocketClient.getInstance().setListener(this);
                
                Platform.runLater(() -> {
                    status.setText("Đã kết nối server");
                    loginButton.setDisable(false);
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    status.setText("Không thể kết nối Server! Vui lòng kiểm tra lại...");
                    loginButton.setDisable(true);
                });
            }
        }).start();
    }

    @FXML
    public void onHandleLogin(ActionEvent e) {
        String uName = username.getText();
        String pass = password.getText();

        if (uName.length() < 5) {
            applyErrorStyle("Tên đăng nhập phải có ít nhất 5 ký tự!");
            username.setStyle("-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: red;");
            return;
        }

        loginButton.setDisable(true);
        RequestHandler.getInstance().requestLogin(uName, pass); 
    }

    @Override
    public void onResponseReceived(String category, String action, String jsonData) {
        if ("AUTH".equals(category)) {
            if ("LOGIN_SUCCESS".equals(action)) {
                JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
                String roleStr = jsonObject.get("role").getAsString();
                
                User user = null;
                if ("ADMIN".equals(roleStr)) user = gson.fromJson(jsonData, Admin.class); 
                else if ("SELLER".equals(roleStr)) user = gson.fromJson(jsonData, Seller.class);
                else if ("BIDDER".equals(roleStr)) user = gson.fromJson(jsonData, Bidder.class);

                final User finalUser = user; 
                // Đã lưu thông tin vào biến toàn cục trước khi chuyển trang
                LoginController.currentUser = finalUser; 
                
                Platform.runLater(() -> {
                    try {
                        if (finalUser != null) {
                            SceneChanger.getInstance().toBidder(finalUser);
                        }
                    } catch (Exception ex) {
                        System.err.println("Lỗi chuyển cảnh: " + ex.getMessage());
                    }
                });
            } else if ("FAIL".equals(action) || "ERROR".equals(action)) {
                Platform.runLater(() -> {
                    loginButton.setDisable(false); 
                    username.clear();
                    password.clear();
                    applyErrorStyle("Sai tài khoản hoặc mật khẩu!");
                    
                    String errorStyle = "-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: red";
                    username.setStyle(errorStyle);
                    password.setStyle(errorStyle);
                });
            }
        }
    }

    @FXML
    public void onKeyReleased() {
        String userText = username.getText();
        String passText = password.getText();

        loginButton.setDisable(userText.trim().isEmpty() || passText.trim().isEmpty());

        boolean isUserValid = validateASCII(userText);
        boolean isPassValid = validateASCII(passText);

        username.setStyle(isUserValid ? "-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: gray;" 
                                      : "-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: red; -fx-border-width: 1px;");
        
        password.setStyle(isPassValid ? "-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: gray;" 
                                      : "-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: red; -fx-border-width: 1px;");

        if (!isUserValid || !isPassValid) applyErrorStyle("Kí tự không hợp lệ");
        else status.setVisible(false);
    }

    private boolean validateASCII(String content) {
        for (char c : content.toCharArray()) {
            if (c < 33 || c > 126) return false;
        }
        return true;
    }

    private void applyErrorStyle(String msg) {
        status.setText(msg);
        status.setTextFill(javafx.scene.paint.Color.RED);
        status.setVisible(true);
    }

    public void onRegisterHandle() throws IOException {
        Parent root = FXMLLoader.load((getClass().getResource("register.fxml")));
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}