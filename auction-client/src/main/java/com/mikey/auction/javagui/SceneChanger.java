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
import javafx.scene.effect.ColorAdjust;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
            controller.setAuctionInfo(auctionInfo);
            controller.setUser(userId);
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
    public void openSettings(Stage popupStage, User user){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mikey/auction/javagui/user/User.fxml"));
            Parent root = loader.load();

            UserController controller = loader.getController();
            controller.setUser(user);
            controller.setStage(popupStage);

            if (!popupStage.isShowing()) {
                try {
                    popupStage.initStyle(StageStyle.TRANSPARENT);
                    popupStage.initModality(Modality.APPLICATION_MODAL);
                    if (mainStage != null) {
                        popupStage.initOwner(mainStage);
                    }
                } catch (Exception e) {
                }
            }

            if (mainStage != null && mainStage.getScene() != null) {
                Parent mainRoot = mainStage.getScene().getRoot();
                ColorAdjust dim = new ColorAdjust();
                dim.setBrightness(-0.5);
                mainRoot.setEffect(dim);

                popupStage.setOnHidden(e -> {
                    mainRoot.setEffect(null);
                });
            }

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            popupStage.setScene(scene);
            popupStage.setAlwaysOnTop(true);

            if (!popupStage.isShowing()) {
                popupStage.show();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void toSellerHubGui(User user){
        navigate("seller/sellerhub.fxml", "seller hub - " + user.getUsername(), loader -> {
            SellerHubController controller = loader.getController();
            controller.setUser(user);
        });
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
