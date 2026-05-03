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
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.Duration;



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
            while (true) { 
                try {
                    // Kết nối đến Server (đảm bảo Server chạy port 12345)
                    socket = new Socket("localhost", 12345);
                    out = new PrintWriter(socket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    
                    Platform.runLater(() -> {
                        status.setText("Đã kết nối server");
                        registerButton.setDisable(false);
                    });
                    break; 
                } catch (IOException e) {
                    Platform.runLater(() -> status.setText("Không thể kết nối server, thử lại..."));
                    try {
                        Thread.sleep(3000); 
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }).start();
    }
    @FXML
    public void onHandleRegister(ActionEvent e) throws IOException {
        String text1 = username.getText();
        String text2 = password.getText();
        out.println("REGISTER|" + text1 + "|" + text2);
        String response = in.readLine();
        boolean success = "SUCCESS".equals(response);
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