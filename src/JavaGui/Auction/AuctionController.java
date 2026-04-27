package JavaGui.Auction;

import Auction.Auction;
import Auction.AuctionInfo;
import Database.AuctionDAO;
import User.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class AuctionController {
    @FXML
    private FlowPane mainContainer;
    private User user;

    public void setUser(User user){
        this.user = user;
    }

    public void loadAuction() throws IOException {
        AuctionDAO auctionDAO = AuctionDAO.getInstance();
        ArrayList<AuctionInfo> auctionArrayList = auctionDAO.getAllAuctions();
        mainContainer.getChildren().clear();
        for (AuctionInfo i : auctionArrayList){
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/JavaGui/Auction/item.fxml"));
            Parent root = loader.load();
            ItemController itemController = loader.getController();
            itemController.setData(i);
            mainContainer.getChildren().add(root);

        }
    }
}
