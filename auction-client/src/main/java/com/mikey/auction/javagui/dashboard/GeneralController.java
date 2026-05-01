package com.mikey.auction.javagui.dashboard;

import com.mikey.auction.user.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;

import java.io.IOException;

public class GeneralController {
//    @FXML
//    private ToggleButton genaralButton;
//    @FXML
//    private ToggleButton myInterestedAuctionButton;
//    @FXML
//    private ToggleButton userConfigButton;
//    @FXML
//    private ToggleButton myAuctionButton;
    @FXML
    private ScrollPane mainContainer;
    @FXML
    private com.mikey.auction.javagui.topbar.TopBarController topBarController;

    private User user;

    public void setUser(User user){
        this.user = user;
        if (topBarController != null){
            topBarController.setUser(user);
        }

    }
    public void initialize(){

    }
    @FXML
    public void generalButton(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard.fxml"));
            Parent root = loader.load();
            mainContainer.setContent(root);
        }catch (IOException e){
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    @FXML
    public void myAuctionButton(){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("myAuction.fxml"));
            Parent root = loader.load();
            MyAuctionController myAuctionController = loader.getController();
            myAuctionController.loadMyAuction(user);
            mainContainer.setContent(root);
        }catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}