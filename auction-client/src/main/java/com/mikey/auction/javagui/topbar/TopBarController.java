package com.mikey.auction.javagui.topbar;


import com.mikey.auction.auction.Notifications;
import com.mikey.auction.database.AuctionDAO;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.javagui.SceneChanger;
import com.mikey.auction.javagui.user.UserController;
import com.mikey.auction.manager.NotificationManager;
import com.mikey.auction.user.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.scene.control.MenuButton;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class TopBarController {
    @FXML private TextField searchField;
    @FXML private ToggleButton searchButton;
    @FXML private MenuButton notification;
    @FXML private VBox mainContainer;

    private SearchListener listener;
    private User user;

    public void setListener(SearchListener listener) {
        this.listener = listener;
    }

    public void searchHandle() throws IOException {
        String keyword = searchField.getText().toUpperCase();
        ArrayList<AuctionInfo> results = AuctionDAO.getInstance().searchAuction(keyword);

        if (listener != null) {
            listener.onSearchPerformed(results);
        }
    }
    public void initialize(){
        searchButton.setDisable(true);
    }
    @FXML
    public void onKeySearchHandle(){
        String text = searchField.getText();
        boolean disable = text.isEmpty() || text.trim().isEmpty();
        searchButton.setDisable(disable);
    }
    @FXML
    public void logoutHandle(){
        SceneChanger.getInstance().toLogin();
    }
    public void setUser(User user){
        this.user = user;
    }
    public void toHubHandle(ActionEvent actionEvent) {
        if (user != null) {
            SceneChanger.getInstance().toBidder(user);
        } else {
            // no user info available: fallback to login
            SceneChanger.getInstance().toLogin();
        }
    }

    @FXML
    public void userGuiHandle(ActionEvent actionEvent) {
        SceneChanger.getInstance().openSettings(new Stage(), user);
//        Stage stage = new Stage();
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mikey/auction/javagui/user/User.fxml"));
//            Parent root = loader.load();
//            UserController controller = loader.getController();
//            controller.setUser(user);
//            stage.setTitle("User");
//            stage.setResizable(false);
//            stage.setAlwaysOnTop(true);
//            stage.setScene(new javafx.scene.Scene(root));
//            stage.show();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
    @FXML
    public void sellerGuiHandle(ActionEvent actionEvent) {
        if (user != null) {
            SceneChanger.getInstance().toSellerHubGui(user);
        } else {
            SceneChanger.getInstance().toLogin();
        }
    }

    @FXML
    public void showNotification() throws IOException {
        List<Notifications> list = NotificationManager.getInstance().findNotififications(user.getId());
        mainContainer.getChildren().clear();
        for (Notifications notification : list){
            FXMLLoader loader = new FXMLLoader(getClass().getResource("notificationCard.fxml"));
            Parent root = loader.load();
            String color = (notification.isRead()) ? "white" : "#c9efc9";
            root.setStyle("-fx-background-color: " + color);
            NotificationController notificationController = loader.getController();
            notificationController.setContent(notification);
            mainContainer.getChildren().add(root);
        }
    }

    public void userConfigHandle(ActionEvent actionEvent) {
    }
}
