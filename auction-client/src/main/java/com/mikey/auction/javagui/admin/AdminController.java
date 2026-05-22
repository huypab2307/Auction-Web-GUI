package com.mikey.auction.javagui.admin;

import java.time.LocalDateTime;
import java.lang.reflect.Type;
import java.util.ArrayList;

import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.socket.RequestHandler;
import com.mikey.auction.socket.SocketClient;
import com.mikey.auction.socket.SocketListener;
import com.mikey.auction.user.User;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

public class AdminController implements SocketListener {

    // Nút chuyển tab Sidebar
    @FXML private Button btnDashboard;
    @FXML private Button btnAuctionItems;
    @FXML private Button btnBiddingHistory;
    @FXML private Button btnUsers;
    @FXML private Button btnSettings;

    // Thanh Header thanh trạng thái trên cùng
    @FXML private Circle serverStatusIndicator;
    @FXML private Label serverStatusLabel;
    @FXML private TextField searchField;

    // Các nhãn hiển thị số lượng ở Card Tổng quan
    @FXML private Label lblActiveAuctions;
    @FXML private Label lblTotalVolume;
    @FXML private Label lblNewBids;

    // Cấu trúc phân phối chuyển đổi view động
    @FXML private StackPane contentStack;
    @FXML private VBox viewDashboard;
    @FXML private VBox viewAuctionItems;
    @FXML private VBox viewBiddingHistory;
    @FXML private VBox viewUsers;
    @FXML private VBox viewSettings;

    // TAB 1: Bảng danh sách vật phẩm đấu giá mặc định (Dashboard)
    @FXML private TableView<AuctionInfo> auctionTable;
    @FXML private TableColumn<AuctionInfo, Integer> colId;
    @FXML private TableColumn<AuctionInfo, String> colItemName;
    @FXML private TableColumn<AuctionInfo, Double> colCurrentBid;
    @FXML private TableColumn<AuctionInfo, String> colBidder;
    @FXML private TableColumn<AuctionInfo, String> colStatus;

    // TAB 2: Các trường bổ sung của quản lý vật phẩm chi tiết
    @FXML private TableView<AuctionInfo> itemTable;
    @FXML private TableColumn<AuctionInfo, Integer> colItemAucId;
    @FXML private TableColumn<AuctionInfo, String> colItemTitle;
    @FXML private TableColumn<AuctionInfo, String> colItemSeller;
    @FXML private TableColumn<AuctionInfo, Double> colItemStartPrice;
    @FXML private TableColumn<AuctionInfo, LocalDateTime> colItemEndTime;
    @FXML private TableColumn<AuctionInfo, String> colItemType;
    @FXML private TextField searchItemField;

// TAB 3: Bảng theo dõi lịch sử đặt cược dòng tiền toàn cục
    @FXML private TableView<com.mikey.auction.dto.BidHistory> bidHistoryTable;
    @FXML private TableColumn<com.mikey.auction.dto.BidHistory, Integer> colHistBidId;
    @FXML private TableColumn<com.mikey.auction.dto.BidHistory, Integer> colHistAucId;
    @FXML private TableColumn<com.mikey.auction.dto.BidHistory, String> colHistBidder;
    @FXML private TableColumn<com.mikey.auction.dto.BidHistory, Double> colHistAmount;
    @FXML private TableColumn<com.mikey.auction.dto.BidHistory, LocalDateTime> colHistTime;

    


    // TAB 4: Quản lý chi tiết tài khoản khách hàng
// TAB 4: Quản lý chi tiết tài khoản khách hàng
    // 👉 XÓA CÁC DÒNG CÓ DẤU <?> VÀ THAY BẰNG CÁC DÒNG DƯỚI ĐÂY:
    
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colUserIdx;
    @FXML private TableColumn<User, String> colUserUsername;
    @FXML private TableColumn<User, com.mikey.auction.user.Role> colUserRole; 
    @FXML private TableColumn<User, String> colUserStatus;
    @FXML private TableColumn<User, Void> colUserAction;
    @FXML private TextField searchUserField;

    // TAB 5: Trường thông số cấu hình Server Socket mạng
    @FXML private TextField txtServerPort;
    @FXML private TextField txtMaxConnections;

    private User currentAdmin;
    
    // Thêm danh sách chứa dữ liệu đổ vào bảng
    private ObservableList<User> userListData = FXCollections.observableArrayList();
    private ObservableList<AuctionInfo> itemListData = FXCollections.observableArrayList();
    private ObservableList<com.mikey.auction.dto.BidHistory> bidHistoryData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        SocketClient.getInstance().setListener(this);
        serverStatusIndicator.setStyle("-fx-fill: #38EF7D;");
        serverStatusLabel.setText("Server: Hệ thống vận hành mượt mà");
        
        // 👉 GỌI HÀM CÀI ĐẶT BẢNG NGAY KHI MỞ APP
        setupTable();
    }

private void setupTable() {
    java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,###"); // Định dạng có dấu phẩy
// 1. Map cột cho bảng Client (đã ổn)
    colUserIdx.setCellValueFactory(new PropertyValueFactory<>("id"));
    colUserUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
    colUserRole.setCellValueFactory(new PropertyValueFactory<>("role"));
    colUserStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    userTable.setItems(userListData);

    // 2. Map cột cho bảng Vật phẩm (TAB 2)
    colItemAucId.setCellValueFactory(new PropertyValueFactory<>("id"));
    colItemStartPrice.setCellValueFactory(new PropertyValueFactory<>("curPrice"));
    colItemEndTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));

    // ==========================================
    // 👉 5. CẤU HÌNH BẢNG LỊCH SỬ ĐẶT GIÁ (TAB 3)
    // ==========================================
    colHistBidId.setCellValueFactory(new PropertyValueFactory<>("id"));
    colHistAucId.setCellValueFactory(new PropertyValueFactory<>("auctionId"));
    colHistBidder.setCellValueFactory(new PropertyValueFactory<>("bidderUsername"));
    colHistAmount.setCellValueFactory(new PropertyValueFactory<>("bidAmount"));
    colHistTime.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

    // Định dạng tiền tệ cho cột Số tiền cược (Giống với TAB 1 và TAB 2)
    colHistAmount.setCellFactory(tc -> new TableCell<com.mikey.auction.dto.BidHistory, Double>() {
        @Override
        protected void updateItem(Double amount, boolean empty) {
            super.updateItem(amount, empty);
            if (empty || amount == null) {
                setText(null);
            } else {
                setText(formatter.format(amount) + " đ");
            }
        }
    });

    // Định dạng ngày tháng cho cột Thời gian nhận gói
    colHistTime.setCellFactory(tc -> new TableCell<com.mikey.auction.dto.BidHistory, LocalDateTime>() {
        @Override
        protected void updateItem(LocalDateTime time, boolean empty) {
            super.updateItem(time, empty);
            if (empty || time == null) {
                setText(null);
            } else {
                setText(time.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            }
        }
    });

    // Đổ danh sách dữ liệu vào bảng
    bidHistoryTable.setItems(bidHistoryData);

    

    // Sử dụng Lambda để bóc dữ liệu từ đối tượng lồng nhau:
    // Lấy 'title' từ itemInfo bên trong AuctionInfo
    colItemTitle.setCellValueFactory(cellData -> {
        if (cellData.getValue().getItemInfo() != null) {
            return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getItemInfo().getTitle());
        }
        return new javafx.beans.property.SimpleStringProperty("N/A");
    });

    // Lấy 'sellerUsername' từ AuctionInfo
    colItemSeller.setCellValueFactory(cellData -> {
        String seller = cellData.getValue().getSellerUsername();
        return new javafx.beans.property.SimpleStringProperty(seller != null ? seller : "N/A");
    });

    // 2. Cột Kiểu sản phẩm (Bóc từ itemInfo -> itemType)
    colItemType.setCellValueFactory(cellData -> 
        new javafx.beans.property.SimpleStringProperty(
            (cellData.getValue().getItemInfo() != null && cellData.getValue().getItemInfo().getItemType() != null) 
            ? cellData.getValue().getItemInfo().getItemType().toString() : "N/A"
        )
    );

    // 3. Cột Client đặt giá (LastBidderName)
    colBidder.setCellValueFactory(cellData -> 
        new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().getLastBidderName() != null ? cellData.getValue().getLastBidderName() : "Chưa có"
        )
    );
    
    itemTable.setItems(itemListData);
    auctionTable.setItems(itemListData);

    // 3. Map cột cho bảng Dashboard (TAB 1)
    colId.setCellValueFactory(new PropertyValueFactory<>("id"));
    colCurrentBid.setCellValueFactory(new PropertyValueFactory<>("curPrice"));
    colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // ==========================================
        // 👉 4. KHẮC PHỤC LỖI HIỂN THỊ SỐ E (Định dạng Tiền tệ)
        // ==========================================

        // Format ép hiển thị tiền cho cột Giá ở TAB 2
        colItemStartPrice.setCellFactory(tc -> new TableCell<AuctionInfo, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(formatter.format(price) + " đ"); // Thêm chữ 'đ' cực kỳ trực quan
                }
            }
        });

        colItemEndTime.setCellFactory(tc -> new TableCell<AuctionInfo, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime time, boolean empty) {
                super.updateItem(time, empty);
                if (empty || time == null) {
                    setText(null);
                } else {
                    // Định dạng lại ngày giờ cho đẹp mắt
                    setText(time.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                }
            }
        });

        // Format ép hiển thị tiền cho cột Giá ở TAB 1 (Tổng quan)
        colCurrentBid.setCellFactory(tc -> new TableCell<AuctionInfo, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(formatter.format(price) + " đ");
                }
            }
        });

        colItemName.setCellValueFactory(cellData -> {
            if (cellData.getValue().getItemInfo() != null) {
                return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getItemInfo().getTitle());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
        
        // 2. Thuật toán sinh nút bấm động (Khóa / Mở Khóa) tùy theo trạng thái hàng dữ liệu
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                return new TableCell<>() {
                    private final Button btnToggle = new Button();
                    {
                        btnToggle.setOnAction((ActionEvent event) -> {
                            User data = getTableView().getItems().get(getIndex());
                            
                            // Tự động kiểm tra trạng thái hiện tại để rẽ nhánh lệnh Socket
                            if ("BANNED".equals(data.getStatus())) {
                                System.out.println("Đang gửi lệnh mở khóa tài khoản ID: " + data.getId());
                                RequestHandler.getInstance().requestUnbanUser(data.getId()); 
                            } else {
                                System.out.println("Đang gửi lệnh khóa tài khoản ID: " + data.getId());
                                RequestHandler.getInstance().requestBanUser(data.getId()); 
                            }
                        });
                    }
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) { 
                            setGraphic(null); 
                        } else { 
                            User data = getTableView().getItems().get(getIndex());
                            
                            // Thay đổi màu sắc và chữ hiển thị của nút dựa vào thuộc tính status của User
                            if ("BANNED".equals(data.getStatus())) {
                                btnToggle.setText("Mở Khóa");
                                btnToggle.setStyle("-fx-background-color: #38EF7D; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 5; -fx-pref-width: 100;");
                            } else {
                                btnToggle.setText("Khóa (Ban)");
                                btnToggle.setStyle("-fx-background-color: #FF5E62; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 5; -fx-pref-width: 100;");
                            }
                            setGraphic(btnToggle); 
                        }
                    }
                };
            }
        };
        colUserAction.setCellFactory(cellFactory);
        
        // Liên kết dữ liệu danh sách động với bảng hiển thị
        userTable.setItems(userListData);
    }


    public void setUser(User user) {
        this.currentAdmin = user;
    }

    /**
     * Thuật toán xương sống xử lý hoán đổi ẩn hiện các tab phân hệ quản trị
     * và quản lý danh sách class active-btn tự động từ file CSS
     */
    private void navigateToTab(VBox targetView, Button targetButton) {
        // 1. Ẩn toàn bộ các VBox layout con trong StackPane ngầm
        viewDashboard.setVisible(false);
        viewAuctionItems.setVisible(false);
        viewBiddingHistory.setVisible(false);
        viewUsers.setVisible(false);
        viewSettings.setVisible(false);

        // 2. Kích hoạt hiện thị luồng tab tương ứng được nhấn
        targetView.setVisible(true);

        // 3. Xóa sạch class đánh dấu 'active-btn' khỏi tất cả các nút điều hướng sidebar
        btnDashboard.getStyleClass().remove("active-btn");
        btnAuctionItems.getStyleClass().remove("active-btn");
        btnBiddingHistory.getStyleClass().remove("active-btn");
        btnUsers.getStyleClass().remove("active-btn");
        btnSettings.getStyleClass().remove("active-btn");

        // 4. Bơm nạp class 'active-btn' vào đúng nút bấm hiện tại để đổi màu chữ xanh mượt
        if (!targetButton.getStyleClass().contains("active-btn")) {
            targetButton.getStyleClass().add("active-btn");
        }
    }

    @FXML
    void showDashboard(ActionEvent event) {
        navigateToTab(viewDashboard, btnDashboard);
        RequestHandler.getInstance().requestAllAuctions();
    }

    @FXML
    void showAuctionItems(ActionEvent event) {
        navigateToTab(viewAuctionItems, btnAuctionItems);
        RequestHandler.getInstance().requestAllAuctions();
    }

    @FXML
    void showBiddingHistory(ActionEvent event) {
        navigateToTab(viewBiddingHistory, btnBiddingHistory);
        RequestHandler.getInstance().requestAllBidHistory();
    }

    @FXML
    void showUsers(ActionEvent event) {
        navigateToTab(viewUsers, btnUsers);
        // 👉 YÊU CẦU SERVER GỬI DATA KHI MỞ TAB
        RequestHandler.getInstance().requestAllUsers();
    }

    @FXML
    void showSettings(ActionEvent event) {
        navigateToTab(viewSettings, btnSettings);
    }

    @FXML
    void handleSearchItem(ActionEvent event) {
        String filterName = searchItemField.getText().trim();
        System.out.println("Lọc danh sách hàng hóa theo từ khóa: " + filterName);
    }

    @FXML
    void handleSearchUser(ActionEvent event) {
        String targetUser = searchUserField.getText().trim();
        System.out.println("Tìm kiếm tài khoản Client: " + targetUser);
    }

    @FXML
    void handleSaveSettings(ActionEvent event) {
        String port = txtServerPort.getText();
        String maxConn = txtMaxConnections.getText();
        System.out.println("Đã ghi nhận thông số Server mới! Cổng lắng nghe: " + port + " | Giới hạn kết nối: " + maxConn);
    }

    @FXML
    void handleRestartServer(ActionEvent event) {
        System.out.println("Đang gửi lệnh reset hệ thống luồng Socket máy chủ...");
    }

    @Override
    public void onResponseReceived(String category, String action, String jsonData) {
        Platform.runLater(() -> {
            try {
// TÌM VÀ THAY THẾ ĐOẠN IF NÀY TRONG onResponseReceived:
// SỬA DÒNG IF NÀY TRONG AdminController.java
                if ("AUCTION".equals(category) && "GET_ALL_USERS".equals(action)) {
                    if (jsonData != null && !jsonData.equals("null")) {
                        // ... (Các đoạn code bên trong giữ nguyên 100% không đổi)
                        com.google.gson.JsonArray jsonArray = com.google.gson.JsonParser.parseString(jsonData).getAsJsonArray();
                        java.util.ArrayList<User> users = new java.util.ArrayList<>();
                        
                        for (com.google.gson.JsonElement element : jsonArray) {
                            com.google.gson.JsonObject obj = element.getAsJsonObject();
                            int id = obj.get("id").getAsInt();
                            String un = obj.get("username").getAsString();
                            String pw = obj.get("password").getAsString();

                            String roleStr = obj.has("role") && !obj.get("role").isJsonNull() ? obj.get("role").getAsString() : "BIDDER";
                            String statusStr = obj.has("status") && !obj.get("status").isJsonNull() ? obj.get("status").getAsString() : "ACTIVE"; // 👉 THÊM DÒNG NÀY
                            
                            User u = null;
                            if ("ADMIN".equals(roleStr)) u = new com.mikey.auction.user.Admin(un, pw, id);
                            else if ("SELLER".equals(roleStr)) u = new com.mikey.auction.user.Seller(un, pw, id);
                            else u = new com.mikey.auction.user.Bidder(un, pw, id);
                            
                            if (u != null) {
                                u.setStatus(statusStr); // 👉 THÊM DÒNG NÀY để hiện đúng ACTIVE/BANNED khi load bảng
                                users.add(u);
                            }
                        }
                        
                        userListData.clear();
                        userListData.addAll(users);
                    }
                }

                // 👉 THÊM NHÁNH NÀY VÀO TRONG HÀM onResponseReceived ĐỂ XỬ LÝ REAL-TIME
                if ("AUCTION".equals(category) && "BAN_USER_SUCCESS".equals(action)) {
                    int bannedUserId = Integer.parseInt(jsonData.trim());
                    Platform.runLater(() -> {
                        for (User u : userListData) {
                            if (u.getId() == bannedUserId) {
                                u.setStatus("BANNED");
                                userTable.refresh(); // Lệnh ma thuật ép JavaFX vẽ lại dòng chữ BANNED ngay tức thì
                                break;
                            }
                        }
                    });
                }

                // 👉 CHÈN THÊM NHÁNH NÀY VÀO TRONG HÀM onResponseReceived ĐỂ ĐỒNG BỘ REAL-TIME MỞ KHÓA
                if ("AUCTION".equals(category) && "UNBAN_USER_SUCCESS".equals(action)) {
                    int unbannedUserId = Integer.parseInt(jsonData.trim());
                    Platform.runLater(() -> {
                        for (User u : userListData) {
                            if (u.getId() == unbannedUserId) {
                                u.setStatus("ACTIVE");
                                userTable.refresh(); // Ép bảng vẽ lại giao diện dòng này ngay lập tức
                                break;
                            }
                        }
                    });
                }

                // 👉 DÁN THÊM NHÁNH NÀY NGAY DƯỚI NHÁNH UNBAN_USER_SUCCESS
                if ("AUCTION".equals(category) && "All".equals(action)) {
                    System.out.println("📦 DỮ LIỆU VẬT PHẨM TỪ SERVER: " + jsonData);
                    if (jsonData != null && !jsonData.equals("null")) {
                        
                        // Khởi tạo Gson có khả năng đọc hiểu ngày tháng (LocalDateTime)
                        Gson gsonObj = new GsonBuilder()
                            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> 
                                LocalDateTime.parse(json.getAsString(), java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                            .create();
                            
                        // Định nghĩa kiểu dữ liệu là ArrayList<AuctionInfo>
                        Type listType = new TypeToken<ArrayList<AuctionInfo>>(){}.getType();
                        
                        // Dịch JSON thành danh sách Object
                        ArrayList<AuctionInfo> items = gsonObj.fromJson(jsonData, listType);
                        
                        // Đẩy lên giao diện JavaFX
                        Platform.runLater(() -> {
                            itemListData.clear();
                            itemListData.addAll(items);
                        });
                    }
                }

                // 👉 NHÁNH NHẬN DỮ LIỆU LỊCH SỬ ĐẶT GIÁ CHO TAB 3
                if ("AUCTION".equals(category) && "GET_ALL_BID_HISTORY".equals(action)) {
                    if (jsonData != null && !jsonData.equals("null")) {
                        // Khởi tạo Gson đọc được ngày tháng
                        Gson gsonObj = new GsonBuilder()
                            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> 
                                LocalDateTime.parse(json.getAsString(), java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                            .create();
                            
                        // Định nghĩa kiểu dữ liệu danh sách BidHistory
                        Type listType = new TypeToken<ArrayList<com.mikey.auction.dto.BidHistory>>(){}.getType();
                        ArrayList<com.mikey.auction.dto.BidHistory> historyList = gsonObj.fromJson(jsonData, listType);
                        
                        // Đẩy lên giao diện JavaFX
                        Platform.runLater(() -> {
                            bidHistoryData.clear();
                            bidHistoryData.addAll(historyList);
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}