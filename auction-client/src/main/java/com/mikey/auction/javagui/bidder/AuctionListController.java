package com.mikey.auction.javagui.bidder;

import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.items.ItemType;
import com.mikey.auction.user.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.FlowPane;
import javafx.application.Platform;

import java.io.IOException;
import java.util.ArrayList;
import java.lang.reflect.Type;

// THƯ VIỆN SOCKET & GSON
import com.mikey.auction.socket.RequestHandler;
import com.mikey.auction.socket.SocketClient;
import com.mikey.auction.socket.SocketListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

// Bổ sung implements SocketListener
public class AuctionListController implements SocketListener {
    @FXML public FlowPane mainContainer;
    private User user;
    
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, t, ctx) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, t, ctx) -> LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .create();

    public void setUser(User user) { this.user = user; }

    public void loadData(ItemType type) {
        // Đăng ký nhận dữ liệu từ Server
        SocketClient.getInstance().setListener(this);
        
        // CẮT BỎ AuctionDAO, gửi yêu cầu qua Socket!
        // Lưu ý: Đảm bảo bên Server của bạn có code để xử lý 2 Request này nhé!
        if (type == null) {
            // VD: Server sẽ gửi về "AUCTION|ALL|[{...}]"
            RequestHandler.getInstance().requestAllAuctions(); // Thay tên hàm cho đúng với hàm bạn đã viết trong RequestHandler
        } else {
            // VD: Server sẽ gửi về "AUCTION|TYPE|[{...}]"
            RequestHandler.getInstance().requestAuctionsByType(type); // Thay tên hàm cho đúng
        }
    }

    public void setSearchResults(ArrayList<AuctionInfo> results) {
        try { renderAuctions(results); } catch (IOException e) { e.printStackTrace(); }
    }

    private void renderAuctions(ArrayList<AuctionInfo> list) throws IOException {
        mainContainer.getChildren().clear();
        for (AuctionInfo i : list) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("item.fxml"));
            Parent root = loader.load();
            ItemController itemController = loader.getController();
            itemController.setData(i);
            if (user != null) itemController.setUser(user.getId());
            mainContainer.getChildren().add(root);
        }
    }

    // HỨNG DỮ LIỆU TỪ SERVER VÀ RENDER
    @Override
    public void onResponseReceived(String category, String action, String jsonData) {
        // Bạn nhớ chỉnh lại chữ "ALL" hoặc "TYPE" cho khớp với hành động Server gửi về nhé
        if ("AUCTION".equals(category) && ("All".equals(action) || "TYPE".equals(action))) {
            Type listType = new TypeToken<ArrayList<AuctionInfo>>(){}.getType();
            ArrayList<AuctionInfo> list = gson.fromJson(jsonData, listType);

            Platform.runLater(() -> {
                try {
                    if (list != null) renderAuctions(list);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        else if ("AUCTION".equals(category) && "UPDATE_STATUS".equals(action)) {
        
        // CÁCH THIÊN TÀI: Không cần tự xóa thẻ UI bằng tay cho mệt. 
        // Bắt RequestHandler gửi lệnh xin lại danh sách mới nhất từ Server!
        // Khi Server gửi lại danh sách mới (đã mất cái bị xóa), Nhánh 1 sẽ tự động chạy và vẽ lại UI sạch sẽ.
        RequestHandler.getInstance().requestAllAuctions();
        }
    }

    
}