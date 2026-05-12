package com.mikey.auction.javagui.seller;

import java.io.IOException;
import com.mikey.auction.database.UserDAO;
import com.mikey.auction.javagui.topbar.TopBarController;
import com.mikey.auction.user.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;

public class SellerHubController {
    @FXML private ToggleButton myAuctionsBtn, addAuctionBtn;
    @FXML private ScrollPane contentScrollPane;
    @FXML private TopBarController topBarController;

    private User user;


    public void setUser(User user){
        this.user = user;
        if (topBarController != null) {
            topBarController.setUser(this.user);
        }
        showMyAuctions();
    }

    @FXML
    public void showMyAuctions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("seller_list_view.fxml"));
            Parent root = loader.load(); 
            SellerListController listController = loader.getController();
            if (this.user != null) {
                listController.loadMyProducts(this.user); 
            }

            contentScrollPane.setContent(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    public void showCreateAuction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Seller.fxml"));
            Parent root = loader.load();
            SellerController sellerController = loader.getController();
        
            if (this.user != null) {
                sellerController.setUser(this.user);
            } else {
                System.err.println("Warning: Attempted to create auction without a logged-in user.");
            }
            contentScrollPane.setContent(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    public void backToHome() {
    }
}