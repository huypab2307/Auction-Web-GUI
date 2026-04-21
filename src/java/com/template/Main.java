package com.template;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Trỏ đường dẫn tới file main.fxml trong thư mục resources
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/template/main.fxml")));

        // Tạo Scene với kích thước 400x500
        Scene scene = new Scene(root, 400, 500);

        primaryStage.setTitle("Hệ Thống Đấu Giá Trực Tuyến - Đăng Nhập");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // Khóa kích thước cửa sổ để giao diện không bị lệch
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}