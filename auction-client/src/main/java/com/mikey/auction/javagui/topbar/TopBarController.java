package com.mikey.auction.javagui.topbar;


import com.mikey.auction.auction.Notifications;
import com.mikey.auction.database.AuctionDAO;
import com.mikey.auction.database.NotificationDAO;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.javagui.SceneChanger;
import com.mikey.auction.manager.NotificationManager;
import com.mikey.auction.user.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.management.Notification;



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
            SceneChanger.getInstance().toMainMenu(user);
        } else {
            // no user info available: fallback to login
            SceneChanger.getInstance().toLogin();
        }
    }

    @FXML
    public void userGuiHandle(ActionEvent actionEvent) {
        if (user != null) {
            SceneChanger.getInstance().toUserGui(user);
        } else {
            SceneChanger.getInstance().toLogin();
        }
    }
    @FXML
    public void sellerGuiHandle(ActionEvent actionEvent) {
        if (user != null) {
            SceneChanger.getInstance().toSellerGui(user);
        } else {
            SceneChanger.getInstance().toLogin();
        }
    }

    @FXML
    public void showNotification() throws IOException {    
        System.out.println("huy"); 
        List<Notifications> list = NotificationManager.getInstance().findNotififications(user.getId());
        for (Notifications notifications : list){
            FXMLLoader loader = new FXMLLoader(getClass().getResource("notificationCard.fxml"));
            Parent root = loader.load();
            NotificationController notificationController = loader.getController();
            notificationController.setContent(notifications);
            mainContainer.getChildren().add(root);
        }
    }
}
