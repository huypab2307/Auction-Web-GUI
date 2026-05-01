package com.mikey.auction.javagui.dashboard;

import com.mikey.auction.database.AuctionDAO;
import com.mikey.auction.javagui.mainmenu.ItemController;
import com.mikey.auction.user.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.util.ArrayList;

import com.mikey.auction.dto.AuctionInfo;

public class MyAuctionController {
    @FXML
    private FlowPane myAuction;
    @FXML
    private FlowPane myInterestedAuction;

    public void initialize(){
    }
    public void loadMyAuction(User user) throws IOException {
        AuctionDAO auctionDAO = AuctionDAO.getInstance();
        renderAuctions(myAuction, auctionDAO.searchAuctionByUserId(user.getId()), user.getId());

    }
    private void renderAuctions(FlowPane flowPane, ArrayList<AuctionInfo> list,int userId) throws IOException {
        myAuction.getChildren().clear();
        for (AuctionInfo i : list) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mikey/auction/javagui/mainmenu/item.fxml"));
            Parent root = loader.load();
            ItemController itemController = loader.getController();
            itemController.setData(i);
            itemController.setUser(userId);
            myAuction.getChildren().add(root);
        }
    }
}