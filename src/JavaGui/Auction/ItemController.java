package JavaGui.Auction;

import Auction.AuctionInfo;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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
    @FXML
    public void showDetailHandle(){
        //chưa xử lý
    }
    public void setData(AuctionInfo i){
        type.setText(i.getItemType().name());
        itemName.setText(i.getItemTitle());
        sellerName.setText(i.getSellerUsername());
        curPrice.setText(String.format("%,.0f đ", i.getCurPrice()));
        date.setText(i.getEndTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        URL src = getClass().getResource(i.imagePath());

        if (src != null) {
            Image img = new Image(src.toExternalForm());
            itemImage.setImage(img);
        } else {
            itemImage.setImage(new Image("/images/earth.png"));
        }
    }
}
