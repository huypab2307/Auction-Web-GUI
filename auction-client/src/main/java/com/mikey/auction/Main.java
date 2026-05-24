package com.mikey.auction;

import com.mikey.auction.javagui.SceneChanger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = new FXMLLoader(getClass().getResource("/com/mikey/auction/javagui/login/login.fxml")).load();
        primaryStage.setTitle("login");
        primaryStage.setScene(new Scene(root));
        primaryStage.setFullScreen(false);
        primaryStage.setResizable(false);
        primaryStage.setMinHeight(600);
        primaryStage.setMinWidth(1100);
        SceneChanger.getInstance().init(primaryStage);
        primaryStage.show();

        // ❌ XOÁ HOÀN TOÀN dòng khởi tạo và start bot tại đây
    }

    @Override
    public void stop() {
        // ❌ XOÁ HOÀN TOÀN lệnh stop bot tại đây vì Client không còn giữ bot nữa
    }

    public static void main(String[] args) {
        launch(args);
    }
}