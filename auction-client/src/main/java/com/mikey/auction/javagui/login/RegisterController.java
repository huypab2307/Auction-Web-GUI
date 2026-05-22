package com.mikey.auction.javagui.login;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
import com.mikey.auction.database.UserDAO;



public class RegisterController {
    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private Button registerButton;
    @FXML
    private Label status;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public void initialize(){
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
        //out.println("REGISTER|" + text1 + "|" + text2);
        //String response = in.readLine();
        //boolean success = "SUCCESS".equals(response);
        boolean success = UserDAO.getInstance().register(text1, text2);
        if (success){
            status.setText("đăng ký thành công!!");
            status.setTextFill(Paint.valueOf("green"));
            username.setStyle("-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: green");
            password.setStyle("-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: green");
            PauseTransition pause = new PauseTransition(Duration.seconds(4));
            pause.setOnFinished(event -> {
                backToLogin(); 
            });
            pause.play();
        }else{
            registerButton.setDisable(true);
            status.setText("tên đăng nhập tồn tại");
            status.setTextFill(Paint.valueOf("red"));
            username.setStyle("-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: red");
            password.setStyle("-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: red");
            username.clear();
            password.clear();
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

    }
    @FXML
    public void onKeyReleased(){
        String text1 = username.getText();
        String text2 = password.getText();
        boolean disable1 = text1.isEmpty() || text1.trim().isEmpty();
        boolean disable2 = text2.trim().isEmpty() || text2.isEmpty();
        if (!disable1 || !disable2){ 
            username.setStyle("-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: gray");
            password.setStyle("-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: gray");
        }
        registerButton.setDisable(disable1 || disable2);
    }
    @FXML
    public void backToLogin() {
        try{
            Stage stage = (Stage) registerButton.getScene().getWindow();
            Parent root = FXMLLoader.load((getClass().getResource("login.fxml")));
            stage.setScene(new Scene(root));
            stage.show();
        }catch(IOException e){
            System.out.println(e.getMessage());
        }
    }
}