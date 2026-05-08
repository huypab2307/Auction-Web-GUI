package com.mikey.auction.javagui.dashboard;

import com.mikey.auction.manager.AuctionManager;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.javagui.bidder.ItemController;
import com.mikey.auction.user.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.FlowPane;
import java.io.IOException;
import java.util.ArrayList;

public class DashBoardController {
    @FXML
    private FlowPane myInterestedAuction; // Khớp với fx:id trong FXML

    private User user;

    public void setUser(User user) {
        this.user = user;
        loadFollowedAuctions();
    }

    private void loadFollowedAuctions() {
        if (user == null) return;
        AuctionManager manager = new AuctionManager();
        ArrayList<AuctionInfo> followedList = manager.getFollowedAuctions(user.getId());

        if (followedList == null || followedList.isEmpty()) {
            System.out.println("User chưa theo dõi phiên nào.");
            return;
        }

        myInterestedAuction.getChildren().clear();
        for (AuctionInfo info : followedList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mikey/auction/javagui/bidder/item.fxml"));
                Parent itemCard = loader.load();
                ItemController controller = loader.getController();
                controller.setData(info);
                controller.setUser(user.getId());
                myInterestedAuction.getChildren().add(itemCard);

            } catch (IOException e) {
                System.err.println("Lỗi load item card trong Dashboard:");
                e.printStackTrace();
            }
        }
    }
}