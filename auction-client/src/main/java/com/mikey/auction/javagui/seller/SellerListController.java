package com.mikey.auction.javagui.seller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mikey.auction.socket.RequestHandler;
import com.mikey.auction.socket.SocketClient;
import com.mikey.auction.socket.SocketListener;
import javafx.application.Platform;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.javagui.topbar.TopBarController;
import com.mikey.auction.user.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.FlowPane;

// THÊM THƯ VIỆN NGÀY THÁNG
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

public class SellerListController implements SocketListener {
    @FXML private FlowPane sellerItemContainer;
    @FXML private TopBarController topBarController;
    private User user;
    
    // ĐÃ FIX LỖI GSON CRASH APP
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, t, ctx) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, t, ctx) -> LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .create();

    public void loadMyProducts(User user) {
        this.user = user;
        if (topBarController != null) topBarController.setUser(this.user);

        SocketClient.getInstance().setListener(this);
        RequestHandler.getInstance().requestUserAuctions(user.getId());
    }

    private void renderAuctions(FlowPane flowPane, ArrayList<AuctionInfo> list, int userId) throws IOException {
        flowPane.getChildren().clear(); 
        for (AuctionInfo i : list) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("seller_item.fxml"));
            Parent root = loader.load();
            SellerItemController itemController = loader.getController();
            itemController.setData(i);
            itemController.setUser(userId);
            flowPane.getChildren().add(root);
        }
    }

    @Override
    public void onResponseReceived(String category, String action, String jsonData) {
        if ("AUCTION".equals(category) && "USER".equals(action)) {
            Type listType = new TypeToken<ArrayList<AuctionInfo>>(){}.getType();
            ArrayList<AuctionInfo> myProducts = gson.fromJson(jsonData, listType);

            Platform.runLater(() -> {
                try {
                    if (myProducts != null) renderAuctions(sellerItemContainer, myProducts, user.getId());
                } catch (IOException e) {
                    System.err.println("Lỗi render danh sách sản phẩm của Seller: " + e.getMessage());
                }
            });
        }
    }
}