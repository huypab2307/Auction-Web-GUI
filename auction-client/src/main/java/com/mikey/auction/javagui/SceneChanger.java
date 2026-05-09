package com.mikey.auction.javagui;

import com.mikey.auction.javagui.bidder.AuctionHubController;
import com.mikey.auction.javagui.seller.SellerController;
import com.mikey.auction.javagui.seller.SellerHubController;
import com.mikey.auction.javagui.auction.AuctionItemController;

import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.javagui.user.UserController;
import com.mikey.auction.user.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ArrayList;


public class SceneChanger {
    private Stage mainStage;
    private static final SceneChanger instance = new SceneChanger();

    private SceneChanger() {}

    public static SceneChanger getInstance() { return instance; }


    public void init(Stage stage) {
        this.mainStage = stage;
    }

    public void toLogin() {
        navigate("login/login.fxml", "Login", loader -> {
            mainStage.setFullScreen(false);
            mainStage.setResizable(false);
        });
    }

    public void toBidder(User user) {
        navigate("bidder/auctionhub.fxml", "Auction Hub - " + user.getUsername(), loader -> {
            AuctionHubController controller = loader.getController();
            controller.setUser(user);
            controller.loadDashBoard();
            mainStage.setResizable(true);
        });
    }

public void toBidder(User user, ArrayList<AuctionInfo> results) {
    // Navigate đến cái vỏ (Hub)
    navigate("bidder/auctionhub.fxml", "Auction Hub - " + user.getUsername(), loader -> {
        AuctionHubController controller = loader.getController();

        controller.setUser(user);
        
        if (results == null) {
            controller.handleCategoryClick(null); 
        } else {
            controller.onSearchPerformed(results);
        }
    });
}
    public void toAuction(AuctionInfo auctionInfo, int userId){
        navigate("auction/auctionitem.fxml", "Auction: " + auctionInfo.getItemInfo().getTitle(), loader -> {
            AuctionItemController controller = loader.getController();
            controller.setUser(userId);
            controller.setAuctionInfo(auctionInfo);
            controller.renderStaticInfo();
            controller.updateDynamicInfo();
        });
    }


    public void toSellerGui(User user){
        if (user == null) {
            toLogin();
            return;
        }
        navigate("seller/Seller.fxml", "Seller Dashboard - " + user.getUsername(), loader -> {
            SellerController controller = loader.getController();
            controller.setUser(user);
            controller.loadSellerAuctions();
        });
    }

    public void toSellerHubGui(User user){
        if (user == null) {
            toLogin();
            return;
        }
        navigate("seller/sellerhub.fxml", "Seller - " + user.getUsername(), loader -> {
            SellerHubController controller = loader.getController();
            controller.setUser(user.getId());
        });
    }

    public void openSettings(Stage stage, User user){
        if (user == null) {
            toLogin();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mikey/auction/javagui/user/User.fxml"));
            Parent root = loader.load();
            UserController controller = loader.getController();
            controller.setUser(user);
            controller.setStage(stage);
            stage.setTitle("User");
            stage.setResizable(false);
            stage.setAlwaysOnTop(true);
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void navigate(String fxmlPath, String title, ControllerConsumer consumer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();


            mainStage.setTitle(title);
            if (mainStage.getScene() == null) {
                mainStage.setScene(new Scene(root));
            } else {
                mainStage.getScene().setRoot(root);
            }
            mainStage.show();
            if (consumer != null) {
                consumer.prepare(loader);
            }
        } catch (IOException e) {
            System.err.println("Lỗi chuyển cảnh: " + fxmlPath);
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    private interface ControllerConsumer {
        void prepare(FXMLLoader loader) throws IOException;
    }

}
