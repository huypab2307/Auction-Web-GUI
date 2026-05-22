package com.mikey.auction.javagui.login;

import com.mikey.auction.socket.RequestHandler;
import com.mikey.auction.socket.SocketClient;
import com.mikey.auction.socket.SocketListener;
import javafx.animation.PauseTransition;
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
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Controller xử lý đăng ký tài khoản.
 * Thực hiện SocketListener để nhận phản hồi bất đồng bộ từ Server.
 */
public class RegisterController implements SocketListener {
    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private Button registerButton;
    @FXML private Label status;

    public void initialize() {
        registerButton.setDisable(true);
        new Thread(() -> {
            try {
                // Sử dụng kết nối tập trung từ SocketClient
                SocketClient.getInstance().connect("localhost", 12345);
                // Đăng ký nhận phản hồi tại đây
                SocketClient.getInstance().setListener(this);
                
                Platform.runLater(() -> {
                    status.setText("Đã kết nối server");
                    registerButton.setDisable(false);
                });

                password.setOnKeyPressed(event -> {
                    // Nếu phím vừa gõ là phím ENTER
                    if (event.getCode() == KeyCode.ENTER) {
                        // Tự động kích hoạt (bóp cò) nút Đăng nhập
                        registerButton.fire(); 
                    }
                });

            } catch (IOException e) {
                Platform.runLater(() -> status.setText("Không thể kết nối server, thử lại..."));
            }
        }).start();
    }

    @FXML
    public void onHandleRegister(ActionEvent e) throws IOException {
        String text1 = username.getText();
        String text2 = password.getText();

        if (text1.length() < 5) {
            status.setText("Tên đăng nhập phải có ít nhất 5 ký tự!");
            status.setTextFill(javafx.scene.paint.Color.RED);
            status.setVisible(true);
            
            String errorStyle = "-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: red;";
            username.setStyle(errorStyle);
            password.setStyle(errorStyle);
            return;
        }

        registerButton.setDisable(true);
        // Gửi yêu cầu đăng ký qua RequestHandler thay vì dùng PrintWriter trực tiếp
        RequestHandler.getInstance().requestRegister(text1, text2);
    }

    /**
     * Hứng phản hồi từ Server trả về sau khi gửi lệnh REGISTER.
     */
    @Override
    public void onResponseReceived(String category, String action, String jsonData) {
        if ("AUTH".equals(category)) {
            Platform.runLater(() -> {
                if ("SUCCESS".equals(action)) {
                    status.setText("đăng ký thành công!!");
                    status.setVisible(true);
                    status.setTextFill(Paint.valueOf("green"));
                    username.setStyle("-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: green");
                    password.setStyle("-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: green");
                    
                    // Đợi 4 giây rồi quay lại màn hình Login
                    PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
                    pause.setOnFinished(event -> backToLogin());
                    pause.play();
                } else {
                    registerButton.setDisable(false);
                    status.setText("tên đăng nhập tồn tại");
                    status.setVisible(true);
                    status.setTextFill(Paint.valueOf("red"));
                    username.setStyle("-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: red");
                    password.setStyle("-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: red");
                    username.clear();
                    password.clear();
                }
            });
        }
    }

    @FXML
    public void onKeyReleased() {
        String text1 = username.getText();
        String text2 = password.getText();
        boolean disable1 = text1.isEmpty() || text1.trim().isEmpty();
        boolean disable2 = text2.trim().isEmpty() || text2.isEmpty();
        
        if (!disable1 || !disable2) { 
            username.setStyle("-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: gray");
            password.setStyle("-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: gray");
        }
        registerButton.setDisable(disable1 || disable2);

        boolean isUserValid = validateASCII(text1);
        boolean isPassValid = validateASCII(text2);

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

    @FXML
    public void backToLogin() {
        try {
            Stage stage = (Stage) registerButton.getScene().getWindow();
            Parent root = FXMLLoader.load((getClass().getResource("login.fxml")));
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Lỗi chuyển cảnh về Login: " + e.getMessage());
        }
    }
}