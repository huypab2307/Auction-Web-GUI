package com.mikey.auction.javagui.bidder;

import com.mikey.auction.database.AuctionDAO;
import com.mikey.auction.javagui.Helper;
import com.mikey.auction.javagui.SceneChanger;
import com.mikey.auction.javagui.dashboard.DashBoardController;
import com.mikey.auction.javagui.topbar.SearchListener;
import com.mikey.auction.javagui.topbar.TopBarController;
import com.mikey.auction.user.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.util.ArrayList;

import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.items.ItemType;

import javafx.scene.layout.StackPane;

public class AuctionHubController implements SearchListener {
    @FXML public ScrollPane scrollPane;
    @FXML private ToggleButton allButton, artButton, electronicButton, vehicleButton;
    @FXML private TopBarController topBarController;

    private User user;

    public void setUser(User user){
        this.user = user;
        if (topBarController != null) topBarController.setUser(user);
        loadAuctionByCategory(null);
    }

    private void loadAuctionByCategory(ItemType type) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("auctionlist.fxml"));
            Parent root = loader.load();

            AuctionListController listController = loader.getController();
            listController.setUser(user); // Truyền user xuống
            listController.loadData(type); // Bảo list load đúng loại

            scrollPane.setContent(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleCategoryClick(ActionEvent event) {
        Object source = event.getSource();
        if (source == artButton) loadAuctionByCategory(ItemType.ARTS);
        else if (source == vehicleButton) loadAuctionByCategory(ItemType.VEHICLE);
        else if (source == electronicButton) loadAuctionByCategory(ItemType.ELECTRONICS);
        else loadAuctionByCategory(null); // All
    }

    public void loadDashBoard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mikey/auction/javagui/dashboard/dashboard.fxml"));
            Parent root = loader.load();
            DashBoardController controller = loader.getController();
            controller.setUser(user);
            scrollPane.setContent(root);

        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void onSearchPerformed(ArrayList<AuctionInfo> results) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AuctionListView.fxml"));
            Parent root = loader.load();
            AuctionListController listController = loader.getController();
            listController.setUser(user);
            listController.setSearchResults(results);
            scrollPane.setContent(root);
        } catch (IOException e) { e.printStackTrace(); }
    }
}