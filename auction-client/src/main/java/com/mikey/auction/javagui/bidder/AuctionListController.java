package com.mikey.auction.javagui.bidder;

import com.mikey.auction.database.AuctionDAO;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.items.ItemType;
import com.mikey.auction.javagui.SceneChanger;
import com.mikey.auction.user.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.util.ArrayList;

public class AuctionListController {
    @FXML public FlowPane mainContainer;
    private User user;

    public void setUser(User user) { this.user = user; }

    public void loadData(ItemType type) {
        try {
            ArrayList<AuctionInfo> list;
            if (type == null) {
                list = AuctionDAO.getInstance().getAllAuctions();
            } else {
                list = AuctionDAO.getInstance().getAuctionsType(type);
            }
            renderAuctions(list);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void setSearchResults(ArrayList<AuctionInfo> results) {
        try { renderAuctions(results); } catch (IOException e) { e.printStackTrace(); }
    }

    private void renderAuctions(ArrayList<AuctionInfo> list) throws IOException {
        mainContainer.getChildren().clear();
        for (AuctionInfo i : list) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("item.fxml"));
            Parent root = loader.load();
            ItemController itemController = loader.getController();
            itemController.setData(i);
            if (user != null) itemController.setUser(user.getId());
            mainContainer.getChildren().add(root);
        }
    }
}
