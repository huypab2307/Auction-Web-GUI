package JavaGui.MainMenu;

import JustToDisplayInfo.AuctionInfo;
import JavaGui.SceneChanger;
import JustToDisplayInfo.ItemSummary;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;

public class ItemController {
    @FXML
    private ImageView itemImage;
    @FXML
    private Label type;
    @FXML
    private Label itemName;
    @FXML
    private Label sellerName;
    @FXML
    private Label date;
    @FXML
    private Label curPrice;

    private int userId;
    private AuctionInfo auctionInfo;
    @FXML
    public void setData(AuctionInfo i){
        ItemSummary itemSummary = i.getItemInfo();
        type.setText(itemSummary.getItemType().name());
        itemName.setText(itemSummary.getTitle());
        sellerName.setText(i.getSellerUsername());
        curPrice.setText(String.format("%,.0f đ", i.getCurPrice()));
        date.setText(i.getEndTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        this.auctionInfo = i;
        URL src = getClass().getResource(itemSummary.getImagePath());

        if (src != null) {
            Image img = new Image(src.toExternalForm());
            itemImage.setImage(img);
        } else {
            itemImage.setImage(new Image("/images/earth.png"));
        }
    }
    @FXML
    public void showDetailHandle() throws IOException {
        SceneChanger.getInstance().toAuction(auctionInfo, userId);
    }
    public void setUser(int userId){
        this.userId = userId;
    }
}
