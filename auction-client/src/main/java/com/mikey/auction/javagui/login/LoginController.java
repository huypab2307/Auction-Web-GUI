package com.mikey.auction.javagui.login;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.InputStreamReader;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.mikey.auction.javagui.SceneChanger;
import com.mikey.auction.user.User;
import com.mikey.auction.user.Admin;
import com.mikey.auction.user.Bidder;
import com.mikey.auction.user.Seller;

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
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private Button loginButton;
    @FXML private Label status;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Gson gson = new Gson();

    public void initialize() {
        loginButton.setDisable(true); 
        new Thread(() -> {
            while (true) { 
                try {
                    // Kết nối đến Server (đảm bảo Server chạy port 12345)
                    socket = new Socket("localhost", 12345);
                    out = new PrintWriter(socket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    
                    Platform.runLater(() -> {
                        status.setText("Đã kết nối server");
                        loginButton.setDisable(false);
                    });
                    break; 
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        status.setText("Không thể kết nối Server! Đang thử lại...");
                        loginButton.setDisable(true);
                    });
                    try {
                        Thread.sleep(5000); 
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }).start();
    }

    @FXML
    public void onHandleLogin(ActionEvent e) {
        String text1 = username.getText();
        String text2 = password.getText();

        if (socket == null || socket.isClosed() || out == null || in == null) {
            status.setText("Chưa kết nối server hoặc kết nối đã mất.");
            loginButton.setDisable(true);
            return;
        }

        if (text1.length() < 5) {
            status.setText("Tên đăng nhập phải có ít nhất 5 ký tự!");
            status.setTextFill(javafx.scene.paint.Color.RED);
            status.setVisible(true);
            
            String errorStyle = "-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: red;";
            username.setStyle(errorStyle);
            password.setStyle(errorStyle);
            return;
        }

        loginButton.setDisable(true);
        
        // Gửi lệnh LOGIN sang Server
        out.println("LOGIN|" + text1 + "|" + text2); 

        new Thread(() -> {
            try {
                String response = in.readLine(); 

                if ("SUCCESS".equals(response)) {
                    String userJson = in.readLine();
                    
                    // Dùng GSON để bóc tách Role
                    JsonObject jsonObject = JsonParser.parseString(userJson).getAsJsonObject();
                    String roleStr = jsonObject.get("role").getAsString();
                    System.out.println("JSON nhận được: " + userJson);
                    User user = null;
                    if ("ADMIN".equals(roleStr)) {
                        user = gson.fromJson(userJson, Admin.class); 
                    } else if ("SELLER".equals(roleStr)) {
                        user = gson.fromJson(userJson, Seller.class);
                    } else if ("BIDDER".equals(roleStr)) {
                        user = gson.fromJson(userJson, Bidder.class);
                    }

                    final User finalUser = user; 

                    Platform.runLater(() -> {
                        try {
                            if (finalUser != null) {
                                SceneChanger.getInstance().toMainMenu(finalUser);
                            }
                        } catch (Exception ex) {
                            System.out.println("Lỗi chuyển cảnh: " + ex.getMessage());
                        }
                    });

                } else {
                    Platform.runLater(() -> {
                        loginButton.setDisable(false); 
                        username.clear();
                        password.clear();
                        status.setText("Sai tài khoản hoặc mật khẩu!");
                        status.setVisible(true);
                        
                        String errorStyle = "-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: red";
                        username.setStyle(errorStyle);
                        password.setStyle(errorStyle);
                        status.setTextFill(javafx.scene.paint.Color.RED); 
                    });
                }
            } catch (Exception ex) {
                Platform.runLater(() -> status.setText("Lỗi kết nối hoặc xử lý dữ liệu!"));
                ex.printStackTrace();
            }
        }).start();

        if (username.getText().length() < 5) {
            applyErrorStyle("Độ dài phải lớn hơn 5");
        }
    }

    @FXML
    public void onKeyReleased() {
        String userText = username.getText();
        String passText = password.getText();

        boolean isUserEmpty = userText.trim().isEmpty();
        boolean isPassEmpty = passText.trim().isEmpty();
        loginButton.setDisable(isUserEmpty || isPassEmpty);

        boolean isUserValid = validateASCII(userText);
        boolean isPassValid = validateASCII(passText);

        if (!isUserValid) {
            username.setStyle("-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: red; -fx-border-width: 1px;");
        } else {
            username.setStyle("-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: gray;");
        }

        if (!isPassValid) {
            password.setStyle("-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: red; -fx-border-width: 1px;");
        } else {
            password.setStyle("-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: gray;");
        }

        if (!isUserValid || !isPassValid) {
            applyErrorStyle("Kí tự không hợp lệ");
        } else {
            status.setVisible(false);
        }
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
        // Lưu ý: register.fxml phải nằm cùng package hoặc đúng đường dẫn resources
        Parent root = FXMLLoader.load((getClass().getResource("register.fxml")));
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}