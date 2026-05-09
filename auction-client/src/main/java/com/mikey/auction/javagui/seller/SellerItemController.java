package com.mikey.auction.javagui.seller;

import com.mikey.auction.javagui.SceneChanger;
import com.mikey.auction.javagui.topbar.TopBarController;
import com.mikey.auction.user.User;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;

import com.mikey.auction.database.UserDAO;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.dto.ItemSummary;

public class SellerItemController {
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
    @FXML private TopBarController topBarController;
    private User user;

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
    public void setUser(int userId){
        this.user = UserDAO.getInstance().findById(userId);
        if (topBarController != null) {
            topBarController.setUser(this.user);
        }
    }


    @FXML
    public void handleEdit(ActionEvent event) {
        System.out.println("Nút Sửa được bấm cho sản phẩm: " + auctionInfo.getItemInfo().getTitle());
    }

    @FXML
    public void handleDelete(ActionEvent event) {
        System.out.println("Nút Xóa được bấm cho sản phẩm: " + auctionInfo.getItemInfo().getTitle());
    }
}