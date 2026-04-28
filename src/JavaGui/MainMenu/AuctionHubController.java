package JavaGui.MainMenu;

import Auction.AuctionInfo;
import Database.AuctionDAO;
import Items.ItemType;
import JavaGui.SceneChanger;
import JavaGui.TopBar.SearchListener;
import JavaGui.TopBar.TopBarController;
import User.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class AuctionHubController implements SearchListener {
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
    @FXML
    private TopBarController topBarController;

    @FXML
    public void initialize() {
        if (topBarController != null) {
            topBarController.setListener(this);
        }
    }

    public void setUser(User user){
        this.user = user;
        if (topBarController != null) {
            topBarController.setUser(user);
        }
    }
    private void renderAuctions(ArrayList<AuctionInfo> list) throws IOException {
        mainContainer.getChildren().clear();
        for (AuctionInfo i : list) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/JavaGui/MainMenu/item.fxml"));
            Parent root = loader.load();
            ItemController itemController = loader.getController();
            itemController.setData(i);
            itemController.setUser(user.getId());
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
            loadAuction();
            return;
        }

        AuctionDAO auctionDAO = AuctionDAO.getInstance();
        ArrayList<AuctionInfo> auctionArrayList = auctionDAO.getAuctionsType(itemType);
        renderAuctions(auctionArrayList);
    }


    @FXML
    public void logoutHandle() throws IOException {
        SceneChanger.getInstance().toLogin();

    }
    @Override
    public void onSearchPerformed(ArrayList<AuctionInfo> results) {
        try {
            renderAuctions(results);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
