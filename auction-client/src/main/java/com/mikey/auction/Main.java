package com.mikey.auction;

import com.mikey.auction.javagui.SceneChanger;
import com.mikey.auction.manager.AuctionBot;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application{
    private AuctionBot auctionBot;
    
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = new FXMLLoader(getClass().getResource("/com/mikey/auction/javagui/login/login.fxml")).load();
        primaryStage.setTitle("login");
        primaryStage.setScene(new Scene(root));
        primaryStage.setFullScreen(false);
        primaryStage.setResizable(false);
        primaryStage.setMinHeight(600);
        primaryStage.setMinWidth(1100);
        SceneChanger.getInstance().init(primaryStage);
        primaryStage.show();



        auctionBot = new AuctionBot();
        auctionBot.start();
    }

    @Override
    public void stop() {
        if (auctionBot != null) {
            auctionBot.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    

}