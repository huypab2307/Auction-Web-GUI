package com.mikey.auction.javagui.seller;

import java.io.IOException;
import java.util.ArrayList;
import com.mikey.auction.database.AuctionDAO;
import com.mikey.auction.database.UserDAO;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.javagui.topbar.TopBarController;
import com.mikey.auction.user.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.FlowPane;

public class SellerListController {
    @FXML private FlowPane sellerItemContainer;
    @FXML private TopBarController topBarController;
    private User user;

    @FXML
    public void setUser(int userId){
        this.user = UserDAO.getInstance().findById(userId);
        if (topBarController != null) {
            topBarController.setUser(this.user);
        }
    }

    public void loadMyProducts(User user) {
        try {
            ArrayList<AuctionInfo> myProducts = AuctionDAO.getInstance().searchAuctionByUserId(user.getId());
            renderAuctions(sellerItemContainer, myProducts, user.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderAuctions(FlowPane flowPane, ArrayList<AuctionInfo> list, int userId) throws IOException {
        sellerItemContainer.getChildren().clear(); 
        for (AuctionInfo i : list) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("seller_item.fxml"));
            Parent root = loader.load();
            
            SellerItemController itemController = loader.getController();
            itemController.setData(i);
            itemController.setUser(userId);
            
            sellerItemContainer.getChildren().add(root);
        }
    }
}