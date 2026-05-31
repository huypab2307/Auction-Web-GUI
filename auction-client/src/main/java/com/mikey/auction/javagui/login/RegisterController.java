package com.mikey.auction.javagui.login;

import java.io.IOException;

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
import javafx.stage.Stage;
import javafx.util.Duration;

import com.mikey.auction.socket.RequestHandler;
import com.mikey.auction.socket.SocketClient;
import com.mikey.auction.socket.SocketListener;

public class RegisterController implements SocketListener {
    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private Button registerButton;
    @FXML private Label status;

    public void initialize() {
        // Chỉ đăng ký Listener, không gọi connect() để tránh đụng độ
        SocketClient.getInstance().setListener(this);
        
        status.setText("Sẵn sàng đăng ký");
        status.setTextFill(javafx.scene.paint.Color.GRAY);
        status.setVisible(true);

        registerButton.setDisable(true);

        password.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !registerButton.isDisabled()) {
                registerButton.fire(); 
            }
        });
    }

    @FXML
    public void onHandleRegister(ActionEvent e) throws IOException {
        String text1 = username.getText();
        String text2 = password.getText();

        // Chặn độ dài tối thiểu y hệt Login
        if (text1.length() < 5) {
            applyErrorStyle("Tên đăng nhập phải có ít nhất 5 ký tự!");
            String errorStyle = "-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: red;";
            username.setStyle(errorStyle);
            password.setStyle(errorStyle);
            return;
        }

        status.setText("Đang xử lý...");
        status.setTextFill(javafx.scene.paint.Color.BLACK);
        status.setVisible(true);
        registerButton.setDisable(true);
        
        RequestHandler.getInstance().requestRegister(text1, text2);
    }

    @Override
    public void onResponseReceived(String category, String action, String jsonData) {
        if ("AUTH".equals(category)) {
            Platform.runLater(() -> {
                if ("SUCCESS".equals(action)) {
                    status.setText("Đăng ký thành công!! Đang về màn hình Login...");
                    status.setVisible(true);
                    status.setTextFill(javafx.scene.paint.Color.GREEN);
                    
                    String successStyle = "-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: green;";
                    username.setStyle(successStyle);
                    password.setStyle(successStyle);
                    
                    PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
                    pause.setOnFinished(event -> backToLogin());
                    pause.play();
                } else {
                    registerButton.setDisable(false);
                    username.clear();
                    password.clear();
                    
                    applyErrorStyle("Tên đăng nhập đã tồn tại, vui lòng chọn tên khác!");
                    
                    String errorStyle = "-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: red;";
                    username.setStyle(errorStyle);
                    password.setStyle(errorStyle);
                }
            });
        }
    }

    @FXML
    public void onKeyReleased() {
        String userText = username.getText();
        String passText = password.getText();

        registerButton.setDisable(userText.trim().isEmpty() || passText.trim().isEmpty());

        boolean isUserValid = validateASCII(userText);
        boolean isPassValid = validateASCII(passText);

        username.setStyle(isUserValid ? "-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: gray;" 
                                      : "-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: red; -fx-border-width: 1px;");
        
        password.setStyle(isPassValid ? "-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: gray;" 
                                      : "-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: red; -fx-border-width: 1px;");

        if (!isUserValid || !isPassValid) {
            applyErrorStyle("Kí tự không hợp lệ");
        } else {
            if ("Kí tự không hợp lệ".equals(status.getText())) {
                status.setVisible(false);
            }
        }
    }

    private boolean validateASCII(String content) {
        for (char c : content.toCharArray()) {
            if (c < 33 || c > 126) return false;
        }
        return true;
    }

    // ĐÃ CHỈNH SỬA: Bê nguyên hàm applyErrorStyle từ LoginController
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
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}