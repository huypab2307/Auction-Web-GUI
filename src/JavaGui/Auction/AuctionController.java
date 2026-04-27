package JavaGui.Auction;

import Auction.AuctionInfo;
import Database.AuctionDAO;
import Items.ItemType;
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
    @FXML
    private TextField searchField;
    @FXML
    private ToggleButton searchButton;

    private User user;


    public void setUser(User user){
        this.user = user;
    }
    public void initialize(){
        searchButton.setDisable(true);
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
            loadAuction();
            return;
        }

        AuctionDAO auctionDAO = AuctionDAO.getInstance();
        ArrayList<AuctionInfo> auctionArrayList = auctionDAO.getAuctionsType(itemType);
        renderAuctions(auctionArrayList);
    }
    @FXML
    public void onKeySearchHandle(){
        String text = searchField.getText();
        boolean disable = text.isEmpty() || text.trim().isEmpty();
        searchButton.setDisable(disable);
    }
    public void searchHandle() throws IOException{
        AuctionDAO auctionDAO = AuctionDAO.getInstance();
        ArrayList<AuctionInfo> auctionArrayList = auctionDAO.searchAuction(searchField.getText().toUpperCase());
        renderAuctions(auctionArrayList);
    }

    @FXML
    public void logoutHandle() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(("/JavaGui/Login/Login.fxml")));
        Parent root = loader.load();
        Stage stage = (Stage) mainContainer.getScene().getWindow();
        setUser(null);
        stage.setTitle("Login");
        stage.setScene(new Scene(root));
        stage.show();

    }
}
