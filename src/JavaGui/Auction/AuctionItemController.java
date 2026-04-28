package JavaGui.Auction;

import Auction.*;
import Database.UserDAO;
import JavaGui.SceneChanger;
import User.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;



public class AuctionItemController implements JavaGui.TopBar.SearchListener {
    @FXML
    private Label sellerName;
    @FXML
    private Label description;
    @FXML
    private Label curPrice;
    @FXML
    private Label curBidder;
    @FXML
    private Label datetime;
    @FXML
    private Label bidStep;
    @FXML
    private Label title;
    @FXML
    private ImageView image;
    private User user;
    @FXML
    private JavaGui.TopBar.TopBarController topBarController;

    @FXML
    public void initialize() {
        if (topBarController != null) {
            topBarController.setListener(this);
        }
    }

    public void setUser(int userId){
        this.user = UserDAO.getInstance().findById(userId);
        if (topBarController != null) {
            topBarController.setUser(this.user);
        }
    }

    @Override
    public void onSearchPerformed(ArrayList<AuctionInfo> results) {
        SceneChanger.getInstance().toMainMenu(user, results);
    }
    public void renderAuction(AuctionInfo auctionInfo){
        sellerName.setText(sellerName.getText() +"  " + auctionInfo.getSellerUsername());
        description.setText("");
        title.setText(auctionInfo.getItemTitle());
        curPrice.setText(auctionInfo.getCurPrice() +"đ");
        bidStep.setText("");
        curBidder.setText("");
        datetime.setText(auctionInfo.getEndTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        URL src = getClass().getResource(auctionInfo.imagePath());
        if (src != null) {
            Image img = new Image(src.toExternalForm());
            image.setImage(img);
        } else {
            image.setImage(new Image("/images/earth.png"));
        }
    }
}
