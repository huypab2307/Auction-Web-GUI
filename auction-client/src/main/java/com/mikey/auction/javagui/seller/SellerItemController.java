package com.mikey.auction.javagui.seller;

import com.mikey.auction.javagui.SceneChanger;
import com.mikey.auction.javagui.topbar.TopBarController;
import com.mikey.auction.user.User;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;

import com.mikey.auction.auction.AuctionStatus;
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
        String imagePath = itemSummary.getImagePath();

        if (imagePath != null && imagePath.startsWith("http")) {
            itemImage.setImage(new Image(imagePath, true));
        } else {
            try {
                URL src = getClass().getResource(imagePath != null ? imagePath : "/images/earth.png");
                if (src != null) {
                    itemImage.setImage(new Image(src.toExternalForm()));
                } else {
                    itemImage.setImage(new Image(getClass().getResourceAsStream("/images/earth.png")));
                }
            } catch (Exception e) {
                System.err.println("Không load được ảnh: " + imagePath);
                itemImage.setImage(new Image(getClass().getResourceAsStream("/images/earth.png")));
            }
        }
    }


    @FXML
    public void setUser(int userId){
        this.userId = userId;
    }


    @FXML
    public void handleDelete(ActionEvent event) {
        if (auctionInfo.getStatus() == AuctionStatus.OPEN) {
            Alert warning = new Alert(Alert.AlertType.WARNING, "Không thể xóa phiên đấu giá đang diễn ra!", ButtonType.OK);
            warning.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Xóa sản phẩm: " + auctionInfo.getItemInfo().getTitle());
        alert.setContentText("Hành động này không thể hoàn tác. Bạn có chắc chắn muốn xóa không?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            Node sourceButton = (Node) event.getSource();
            Node cardRoot = sourceButton.getParent().getParent();

            FadeTransition fade = new FadeTransition(Duration.millis(300), cardRoot);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setOnFinished(e -> {
                cardRoot.setVisible(false);
                cardRoot.setManaged(false);
            });
            fade.play();
            
            System.out.println("Deleted ID: " + auctionInfo.getId());
        }
    }

    @FXML
    public void handleEdit(ActionEvent event) {
        if (auctionInfo.getStatus() == AuctionStatus.CLOSED || auctionInfo.getStatus() == AuctionStatus.CANCELED) {
            Alert warning = new Alert(Alert.AlertType.WARNING, "Không thể sửa phiên đấu giá đã kết thúc hoặc bị hủy!", ButtonType.OK);
            warning.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mikey/auction/javagui/seller/Seller.fxml"));
            Parent root = loader.load();

            SellerController controller = loader.getController();
            controller.setEditMode(auctionInfo);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}