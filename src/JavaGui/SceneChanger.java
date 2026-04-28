package JavaGui;

import JavaGui.MainMenu.AuctionHubController;
import JavaGui.Auction.AuctionItemController;
import User.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ArrayList;

import DTO.*;

public class SceneChanger {
    private Stage mainStage;
    private static final SceneChanger instance = new SceneChanger();

    private SceneChanger() {}

    public static SceneChanger getInstance() { return instance; }


    public void init(Stage stage) {
        this.mainStage = stage;
    }

    public void toLogin() {
        navigate("/JavaGui/Login/login.fxml", "Login", null);
    }

    public void toMainMenu(User user) {
        navigate("/JavaGui/MainMenu/auctionhub.fxml", "Auction Hub - " + user.getUsername(), loader -> {
            AuctionHubController controller = loader.getController();
            controller.setUser(user);
            controller.loadAuction();
        });
    }

    public void toMainMenu(User user, ArrayList<AuctionInfo> results) {
        navigate("/JavaGui/MainMenu/auctionhub.fxml", "Auction Hub - " + user.getUsername(), loader -> {
            AuctionHubController controller = loader.getController();
            controller.setUser(user);
            if (results == null) {
                controller.loadAuction();
            } else {
                controller.onSearchPerformed(results);
            }
        });
    }
    public void toAuction(AuctionInfo auctionInfo, int userId){
        navigate("/JavaGui/Auction/auctionitem.fxml", "Auction: " + auctionInfo.getItemInfo().getTitle(), loader -> {
            AuctionItemController controller = loader.getController();
            controller.setUser(userId);
            controller.setAuctionInfo(auctionInfo);
            controller.renderStaticInfo();
            controller.updateDynamicInfo();
        });
    }
//    public void toAuction(AuctionInfo auctionInfo, User user){
//        navigate("/JavaGui/Auction/auctionitem.fxml", "Auction: " + auctionInfo.getItemInfo().getTitle(), loader -> {
//            AuctionItemController controller = loader.getController();
//            controller.setUser(user);
//            controller.setAuctionInfo(auctionInfo);
//            controller.renderAuction();
//        });
//    }

    private void navigate(String fxmlPath, String title, ControllerConsumer consumer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if (consumer != null) {
                consumer.prepare(loader);
            }

            mainStage.setTitle(title);
            if (mainStage.getScene() == null) {
                mainStage.setScene(new Scene(root));
            } else {
                mainStage.getScene().setRoot(root);
            }
            mainStage.show();
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