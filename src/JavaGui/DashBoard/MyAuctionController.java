package JavaGui.DashBoard;

import DTO.AuctionInfo;
import Database.AuctionDAO;
import JavaGui.MainMenu.ItemController;
import User.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.util.ArrayList;

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/JavaGui/MainMenu/item.fxml"));
            Parent root = loader.load();
            ItemController itemController = loader.getController();
            itemController.setData(i);
            itemController.setUser(userId);
            myAuction.getChildren().add(root);
        }
    }
}
