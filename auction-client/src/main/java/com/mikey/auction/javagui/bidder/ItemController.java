package com.mikey.auction.javagui.bidder;

import com.mikey.auction.javagui.SceneChanger;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;

import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.dto.ItemSummary;

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

        type.setText(itemSummary.getItemType().name() + " | " + i.getStatus().name());
        
        // Đổi màu Label type dựa trên trạng thái
        if (i.getStatus().name().equals("OPEN")) {
            type.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;"); // Xanh lá cho OPEN
        } else if (i.getStatus().name().equals("PENDING")) {
            type.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black;"); // Vàng cho PENDING
        } else {
            type.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;"); // Đỏ cho CLOSED/CANCELED
        }

        itemName.setText(itemSummary.getTitle());
        sellerName.setText(i.getSellerUsername());
        curPrice.setText(String.format("%,.0f đ", i.getCurPrice()));
        date.setText(i.getEndTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        String path = itemSummary.getImagePath();
        if (path != null && path.startsWith("http")) {
            itemImage.setImage(new Image(path, true));
        } else {
            URL src = getClass().getResource(path != null ? path : "/images/earth.png");
            itemImage.setImage(src != null ? new Image(src.toExternalForm()) : new Image("/images/earth.png"));
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