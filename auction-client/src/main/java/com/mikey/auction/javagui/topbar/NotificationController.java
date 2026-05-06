package com.mikey.auction.javagui.topbar;

import com.mikey.auction.auction.Notifications;

import com.mikey.auction.database.AuctionDAO;
import com.mikey.auction.database.UserDAO;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.javagui.SceneChanger;
import com.mikey.auction.manager.NotificationManager;
import com.mikey.auction.user.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import java.time.format.DateTimeFormatter;



public class NotificationController {
    @FXML private Text mainContent;
    @FXML private Label timeLabel;
    private int auctionId;
    private int userId;
    private int notificationId;

    @FXML
    public void setContent(Notifications notifications) {
        mainContent.setText(notifications.getMessage());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm-ss");
        String formattedTime = notifications.getCreatedAt().format(formatter);
        userId = notifications.getUserId();
        notificationId = notifications.getId();
        timeLabel.setText(formattedTime);
        auctionId = notifications.getAuctionId(); 
    }


    public void handleCardClick(MouseEvent mouseEvent) {
        User user = UserDAO.getInstance().findById(userId);
        AuctionInfo auctionInfo = AuctionDAO.getInstance().searchAuctionById(auctionId);
        NotificationManager.getInstance().markAsRead(userId, notificationId);
        SceneChanger.getInstance().toAuction(auctionInfo, user.getId());

    }
}