package com.mikey.auction.javagui.bidder;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.socket.RequestHandler;
import com.mikey.auction.socket.SocketClient;
import com.mikey.auction.socket.SocketListener;
import com.mikey.auction.user.User;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.FlowPane;

public class MyAuctionController implements SocketListener {
    @FXML private FlowPane myAuction;
    @FXML private FlowPane myInterestedAuction;

    private User user; 
    
    // ĐÃ FIX GSON
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, t, ctx) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, t, ctx) -> LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .create();

    public void initialize(){
    }

    public void loadMyAuction(User user) {
        this.user = user;
        SocketClient.getInstance().setListener(this);
        RequestHandler.getInstance().requestUserAuctions(user.getId());
    }

    private void renderAuctions(FlowPane flowPane, ArrayList<AuctionInfo> list, int userId) throws IOException {
        flowPane.getChildren().clear(); 
        for (AuctionInfo i : list) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mikey/auction/javagui/bidder/item.fxml"));
            Parent root = loader.load();
            ItemController itemController = loader.getController();
            itemController.setData(i);
            itemController.setUser(userId);
            flowPane.getChildren().add(root);
        }
    }

    @Override
    public void onResponseReceived(String category, String action, String jsonData) {
        if ("AUCTION".equals(category) && "USER".equals(action)) {
            Type listType = new TypeToken<ArrayList<AuctionInfo>>(){}.getType();
            ArrayList<AuctionInfo> list = gson.fromJson(jsonData, listType);

            Platform.runLater(() -> {
                try {
                    if (list != null) renderAuctions(myAuction, list, user.getId());
                } catch (IOException e) {
                    System.err.println("Lỗi khi render Item trong MyAuctionController:");
                    e.printStackTrace();
                }
            });
        }
    }
}