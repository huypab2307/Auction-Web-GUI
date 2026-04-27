package JavaGui.Auction;

import Auction.Auction;
import Auction.AuctionInfo;
import Database.AuctionDAO;
import Items.ItemType;
import User.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class AuctionController {
    @FXML
    private FlowPane mainContainer;
    @FXML
    private ToggleButton allButton;
    @FXML
    private ToggleButton artButton;
    @FXML
    private ToggleButton electronicButton;
    @FXML
    private ToggleButton vehicleButton;

    private User user;


    public void setUser(User user){
        this.user = user;
    }
    private void renderAuctions(ArrayList<AuctionInfo> list) throws IOException {
        mainContainer.getChildren().clear();
        for (AuctionInfo i : list) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/JavaGui/Auction/item.fxml"));
            Parent root = loader.load();
            ItemController itemController = loader.getController();
            itemController.setData(i);
            mainContainer.getChildren().add(root);
        }
    }

    public void loadAuction() throws IOException {
        AuctionDAO auctionDAO = AuctionDAO.getInstance();
        ArrayList<AuctionInfo> auctionArrayList = auctionDAO.getAllAuctions();
        renderAuctions(auctionArrayList);
    }

    @FXML
    public void loadAuctionType(ActionEvent event) throws IOException {
        Object source = event.getSource();
        ItemType itemType;

        if (source == artButton) {
            itemType = ItemType.ARTS;
        } else if (source == vehicleButton) {
            itemType = ItemType.VEHICLE;
        } else if (source == electronicButton) {
            itemType = ItemType.ELECTRONICS;
        } else {
            loadAuction(); // Nếu bấm "Tất cả"
            return;
        }

        AuctionDAO auctionDAO = AuctionDAO.getInstance();
        ArrayList<AuctionInfo> auctionArrayList = auctionDAO.getAuctionsType(itemType);
        renderAuctions(auctionArrayList);
    }
}
