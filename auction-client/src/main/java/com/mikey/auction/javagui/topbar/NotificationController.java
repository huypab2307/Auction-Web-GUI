package com.mikey.auction.javagui.topbar;

import com.mikey.auction.auction.Notifications;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import java.time.format.DateTimeFormatter;



public class NotificationController {
    @FXML private Text mainContent;
    @FXML private Label timeLabel;
    private int auctionId;

    @FXML
    public void setContent(Notifications notifications) {
        mainContent.setText(notifications.getMessage());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm-ss");
        String formattedTime = notifications.getCreatedAt().format(formatter);
        timeLabel.setText(formattedTime);
        auctionId = notifications.getAuctionId(); 
    }


}