package com.mikey.auction.javagui.auction;

import com.mikey.auction.database.AuctionDAO;
import com.mikey.auction.database.UserDAO;
import com.mikey.auction.manager.AuctionManager;
import com.mikey.auction.manager.ItemManager;
import com.mikey.auction.javagui.RandomHelper;
import com.mikey.auction.javagui.SceneChanger;
import com.mikey.auction.user.User;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import com.mikey.auction.user.Bidder;
import com.mikey.auction.user.Role;
import javafx.util.Duration;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;

import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.dto.ItemSummary;
import com.mikey.auction.factory.UserFactory;
import com.mikey.auction.javagui.topbar.TopBarController;
import com.mikey.auction.manager.ItemManager;
import com.mikey.auction.javagui.topbar.SearchListener;


public class AuctionItemController implements SearchListener {
    @FXML
    private StackPane mainStackPane;
    @FXML
    private ImageView congratulation;
    @FXML
    private Label startTime;
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
    @FXML
    private TilePane attributeBox;
    private User user;
    @FXML
    private TopBarController topBarController;
    @FXML
    private Label type;
    private AuctionInfo auctionInfo;
    @FXML
    private Button bidButton;
    @FXML
    private Pane pane;

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
    public void setUser(User user){
        this.user = user;

    }
    public void setAuctionInfo(AuctionInfo auctionInfo){
        this.auctionInfo = auctionInfo;
    }

    @Override
    public void onSearchPerformed(ArrayList<AuctionInfo> results) {
        SceneChanger.getInstance().toMainMenu(user, results);
    }

    public void renderStaticInfo() {
        try {
            ItemSummary itemSummary = auctionInfo.getItemInfo();

            title.setText(itemSummary.getTitle());
            description.setText(itemSummary.getDescription());
            sellerName.setText("Người bán: " + auctionInfo.getSellerUsername());
            bidStep.setText("• Bước giá: " + auctionInfo.getBidStep() + "đ");
            datetime.setText(auctionInfo.getEndTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            startTime.setText(auctionInfo.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            type.setText("Loại: " + itemSummary.getItemType().name());

            URL src = getClass().getResource(itemSummary.getImagePath());
            image.setImage(src != null ? new Image(src.toExternalForm()) : new Image("/images/earth.png"));
            pane.setStyle("-fx-padding: 40 400 40 100;" + RandomHelper.randomColorPicker());
            attributeBox.getChildren().clear();
            Map<String, String> itemInfo = ItemManager.getInstance().findItemById(itemSummary.getItemType(), itemSummary.getItemId()).getSpecificInfo();
            itemInfo.forEach((label, value) -> {
                attributeBox.getChildren().add(new Label(label + ": " + value));
            });
        }catch (Exception e){
            System.err.println(e.getMessage());
        }
    }
    public void updateDynamicInfo() {
        curPrice.setText(auctionInfo.getCurPrice() + "đ");
        curBidder.setText(auctionInfo.getLastBidderName() != null ? "người giữ giá: " + auctionInfo.getLastBidderName() : "Chưa có người ra giá");

    }

    @FXML
    public void onBidHandle(ActionEvent actionEvent) {
        Button bidButton = (Button) actionEvent.getSource();
        bidButton.setDisable(true);
        bidButton.setText("Đang xử lý...");

        try {
            Bidder bidder = (Bidder) UserFactory.createUser(Role.BIDDER, user);
            if (AuctionManager.getInstance().placeBid(bidder, auctionInfo.getId(), auctionInfo.getCurPrice())) {
                auctionInfo = AuctionDAO.getInstance().searchAuctionById(auctionInfo.getId());
                showCongratulationEffect(2.5);

                System.out.println("Đặt giá thành công!");
            } else {
                System.err.println("Đặt giá thất bại!");
                auctionInfo = AuctionDAO.getInstance().searchAuctionById(auctionInfo.getId());
            }
        } catch (Exception e) {
            System.err.println("Lỗi hệ thống: " + e.getMessage());
        }

        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(e -> {
            bidButton.setDisable(false);
            updateDynamicInfo();
            bidButton.setText("Đặt giá ngay");
        });
        pause.play();
    }

    private void showCongratulationEffect(double seconds) {
        ImageView animImg = new ImageView();
        try {
            URL imgUrl = getClass().getResource("/images/congratulation.gif");
            if (imgUrl != null) {
                animImg.setImage(new Image(imgUrl.toExternalForm()));
            }
        } catch (Exception e) {
            System.err.println("Không tìm thấy ảnh GIF chúc mừng!");
            return;
        }

        animImg.setFitWidth(900);
        animImg.setPreserveRatio(true);
        animImg.setMouseTransparent(true);

        mainStackPane.getChildren().add(animImg);

        PauseTransition cleanup = new PauseTransition(Duration.seconds(seconds));
        cleanup.setOnFinished(event -> {
            mainStackPane.getChildren().remove(animImg);
        });
        cleanup.play();
    }
}