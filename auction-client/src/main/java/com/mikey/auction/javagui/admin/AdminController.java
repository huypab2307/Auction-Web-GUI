package com.mikey.auction.javagui.admin;

import java.time.LocalDateTime;
import java.lang.reflect.Type;
import java.util.ArrayList;

import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.javagui.SceneChanger;
import com.mikey.auction.socket.RequestHandler;
import com.mikey.auction.socket.SocketClient;
import com.mikey.auction.socket.SocketListener;
import com.mikey.auction.user.User;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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
    @FXML private ScrollPane viewDashboard;
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
    @FXML private TableColumn<AuctionInfo, Void> colItemAction;
    @FXML private TableColumn<AuctionInfo, String> colItemType;
    @FXML private TextField searchItemField;

// TAB 3: Bảng theo dõi lịch sử đặt cược dòng tiền toàn cục
    @FXML private TableView<com.mikey.auction.dto.BidHistory> bidHistoryTable;
    @FXML private TableColumn<com.mikey.auction.dto.BidHistory, Integer> colHistBidId;
    @FXML private TableColumn<com.mikey.auction.dto.BidHistory, Integer> colHistAucId;
    @FXML private TableColumn<com.mikey.auction.dto.BidHistory, String> colHistBidder;
    @FXML private TableColumn<com.mikey.auction.dto.BidHistory, Double> colHistAmount;
    @FXML private TableColumn<com.mikey.auction.dto.BidHistory, LocalDateTime> colHistTime;
    @FXML private TextField searchBidField;

    


    // TAB 4: Quản lý chi tiết tài khoản khách hàng
// TAB 4: Quản lý chi tiết tài khoản khách hàng
    //  XÓA CÁC DÒNG CÓ DẤU <?> VÀ THAY BẰNG CÁC DÒNG DƯỚI ĐÂY:
    
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

    // Thành phần biểu đồ phân tích mới
    @FXML private PieChart categoryPieChart;
    @FXML private BarChart<String, Number> topAuctionsBarChart;
    @FXML private CategoryAxis barXAxis;
    @FXML private NumberAxis barYAxis;
    @FXML private ComboBox<String> cbTimeFilter;

    @FXML
    public void initialize() {
        SocketClient.getInstance().setListener(this);
        serverStatusIndicator.setStyle("-fx-fill: #38EF7D;");
        serverStatusLabel.setText("Server: Hệ thống vận hành mượt mà");
        setupTable();
        //  THÊM DÒNG NÀY ĐỂ AUTO-LOAD DỮ LIỆU KHI VỪA MỞ APP:
        RequestHandler.getInstance().requestAllAuctions();

        // Khởi tạo ComboBox Lọc dữ liệu
        if (cbTimeFilter != null) {
            cbTimeFilter.setItems(FXCollections.observableArrayList("Tất cả thời gian", "Hôm nay", "7 ngày qua"));
            cbTimeFilter.getSelectionModel().selectFirst();
            cbTimeFilter.setOnAction(e -> applyChartFilter());
        }

        RequestHandler.getInstance().requestAllBidHistory();
        // Gắn sự kiện nhấn Enter cho ô tìm kiếm Item
        if (searchItemField != null) {
            searchItemField.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                    handleSearchItem(null); // Truyền null vì hàm của bạn không sử dụng biến event
                }
            });
        }

        // Gắn sự kiện nhấn Enter cho ô tìm kiếm User
        if (searchUserField != null) {
            searchUserField.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                    handleSearchUser(null);
                }
            });
        }

        // Gắn sự kiện nhấn Enter cho ô tìm kiếm Bid
        if (searchBidField != null) {
            searchBidField.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                    handleSearchBid(null);
                }
            });
        }
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

    // 2. Map cột cho bảng Vật phẩm (TAB 2)
    colItemAucId.setCellValueFactory(new PropertyValueFactory<>("id"));
    colItemStartPrice.setCellValueFactory(new PropertyValueFactory<>("curPrice"));
    colItemEndTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));

    // =========================================================
    //  DÁN THÊM TOÀN BỘ KHỐI CODE TẠO NÚT XÓA NÀY VÀO ĐÂY
    // =========================================================
    // Ép JavaFX nhận diện cột để tránh lỗi trống
    colItemAction.setCellValueFactory(param -> new javafx.beans.property.SimpleObjectProperty<>(null));
    
    Callback<TableColumn<AuctionInfo, Void>, TableCell<AuctionInfo, Void>> cellActionFactory = param -> new TableCell<>() {
        private final Button btnDelete = new Button("Xóa (Hủy)");
        {
            btnDelete.setStyle("-fx-background-color: #FF416C; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 5;");
            btnDelete.setOnAction(event -> {
                AuctionInfo data = getTableView().getItems().get(getIndex());
                
                // Lấy ID của Admin hiện tại (nếu chưa có thì mặc định là 0 để lót đường)
                int adminId = (currentAdmin != null) ? currentAdmin.getId() : 0;
                
                System.out.println("Admin (ID: " + adminId + ") ra lệnh XÓA phiên đấu giá ID: " + data.getId());
                
                //  GỌI ĐÚNG HÀM DÀNH CHO ADMIN
                RequestHandler.getInstance().requestDeleteAuctionAdmin(data.getId(), adminId);
                
                RequestHandler.getInstance().requestAllAuctions();
            });
        }
        
        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || getTableView() == null || getTableView().getItems() == null || 
                getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                setGraphic(null);
            } else {
                AuctionInfo data = getTableView().getItems().get(getIndex());
                if (data == null) {
                    setGraphic(null);
                    return;
            }
                
                //  BỔ SUNG LOGIC ĐỔI MÀU NÚT BẤM
                if (data.getStatus() == com.mikey.auction.auction.AuctionStatus.CANCELED) {
                    btnDelete.setText("Đã Hủy");
                    btnDelete.setStyle("-fx-background-color: #A1A5B7; -fx-text-fill: white; -fx-background-radius: 5;");
                    btnDelete.setDisable(true); // Khóa nút lại, không cho bấm nữa
                } else {
                    btnDelete.setText("Xóa (Hủy)");
                    btnDelete.setStyle("-fx-background-color: #FF416C; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 5;");
                    btnDelete.setDisable(false); // Bật lại nút
                }
                
                setGraphic(btnDelete);
            }
        }
    };
    colItemAction.setCellFactory(cellActionFactory);
    // =========================================================

    // ==========================================
    //  5. CẤU HÌNH BẢNG LỊCH SỬ ĐẶT GIÁ (TAB 3)
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
        //  4. KHẮC PHỤC LỖI HIỂN THỊ SỐ E (Định dạng Tiền tệ)
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
                        if (empty || getTableView() == null || getTableView().getItems() == null || 
                            getIndex() < 0 || getIndex() >= getTableView().getItems().size()) { 
                            setGraphic(null); 
                        } else { 
                            User data = getTableView().getItems().get(getIndex());
                            if (data == null) {
                                setGraphic(null);
                                return;
                            }
                            
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
    private void navigateToTab(javafx.scene.Node targetView, Button targetButton) {
        viewDashboard.setVisible(false);
        viewAuctionItems.setVisible(false);
        viewBiddingHistory.setVisible(false);
        viewUsers.setVisible(false);
        viewSettings.setVisible(false);

        targetView.setVisible(true);

        btnDashboard.getStyleClass().remove("active-btn");
        btnAuctionItems.getStyleClass().remove("active-btn");
        btnBiddingHistory.getStyleClass().remove("active-btn");
        btnUsers.getStyleClass().remove("active-btn");
        btnSettings.getStyleClass().remove("active-btn");

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
        //  YÊU CẦU SERVER GỬI DATA KHI MỞ TAB
        RequestHandler.getInstance().requestAllUsers();
    }

    @FXML
    void showSettings(ActionEvent event) {
        navigateToTab(viewSettings, btnSettings);
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
                            String statusStr = obj.has("status") && !obj.get("status").isJsonNull() ? obj.get("status").getAsString() : "ACTIVE"; //  THÊM DÒNG NÀY
                            
                            User u = null;
                            if ("ADMIN".equals(roleStr)) u = new com.mikey.auction.user.Admin(un, pw, id);
                            else if ("SELLER".equals(roleStr)) u = new com.mikey.auction.user.Seller(un, pw, id);
                            else u = new com.mikey.auction.user.Bidder(un, pw, id);
                            
                            if (u != null) {
                                u.setStatus(statusStr); //  THÊM DÒNG NÀY để hiện đúng ACTIVE/BANNED khi load bảng
                                users.add(u);
                            }
                        }
                        
                        userListData.clear();
                        userListData.addAll(users);
                    }
                }

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
                            applyChartFilter();
                            updateOverviewCards();
                            handleSearchItem(null);
                        });
                    }
                }

                //  NHÁNH NHẬN DỮ LIỆU LỊCH SỬ ĐẶT GIÁ CHO TAB 3
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
                            updateOverviewCards();
                            handleSearchBid(null);
                        });
                    }
                }

                //  THÊM NHÁNH NÀY ĐỂ REAL-TIME ĐỔI MÀU GIAO DIỆN KHI XÓA
                if ("AUCTION".equals(category) && "UPDATE_STATUS".equals(action)) {
                    // Dữ liệu Server trả về dạng: id|CANCELED
                    String[] updateData = jsonData.split("\\|");
                    if (updateData.length == 2) {
                        int aucId = Integer.parseInt(updateData[0]);
                        String newStatus = updateData[1];
                        
                        Platform.runLater(() -> {
                            for (AuctionInfo info : itemListData) {
                                if (info.getId() == aucId) {
                                    info.setStatus(com.mikey.auction.auction.AuctionStatus.valueOf(newStatus));
                                    itemTable.refresh(); // Ép bảng vẽ lại giao diện
                                    break;
                                }
                            }
                            updateOverviewCards();
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    void handleLogout(ActionEvent event) {
        System.out.println("Admin đăng xuất!");
        // Gọi SceneChanger để quay về màn hình Login
        SceneChanger.getInstance().toLogin(); 
    }

    private void updateAnalyticsCharts(java.util.List<AuctionInfo> list) {
        if (list == null || list.isEmpty() || topAuctionsBarChart == null || categoryPieChart == null) return;

        topAuctionsBarChart.setAnimated(false);
        categoryPieChart.setAnimated(false);

        // --- XỬ LÝ BIỂU ĐỒ TRÒN (PIE CHART) ---
        int artsCount = 0, electronicsCount = 0, vehicleCount = 0;
        for (AuctionInfo info : list) {
            if (info.getItemInfo() != null && info.getItemInfo().getItemType() != null) {
                switch (info.getItemInfo().getItemType()) {
                    case ARTS -> artsCount++;
                    case ELECTRONICS -> electronicsCount++;
                    case VEHICLE -> vehicleCount++;
                }
            }
        }
        
        categoryPieChart.getData().clear();
        if (artsCount > 0) categoryPieChart.getData().add(new javafx.scene.chart.PieChart.Data("Nghệ thuật", artsCount));
        if (electronicsCount > 0) categoryPieChart.getData().add(new javafx.scene.chart.PieChart.Data("Điện tử", electronicsCount));
        if (vehicleCount > 0) categoryPieChart.getData().add(new javafx.scene.chart.PieChart.Data("Xe cộ", vehicleCount));

        // --- XỬ LÝ BIỂU ĐỒ CỘT (BAR CHART) ---
        // --- XỬ LÝ BIỂU ĐỒ CỘT (BAR CHART) ---
        java.util.List<AuctionInfo> sortedList = new java.util.ArrayList<>(list);
        sortedList.sort((a, b) -> Double.compare(b.getCurPrice(), a.getCurPrice()));

        topAuctionsBarChart.getData().clear();
        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();

        String[] gradientColors = {
            "-fx-background-color: linear-gradient(to top, #00c6ff, #0072ff);",
            "-fx-background-color: linear-gradient(to top, #f77062, #fe5196);",
            "-fx-background-color: linear-gradient(to top, #11998e, #38ef7d);",
            "-fx-background-color: linear-gradient(to top, #f2994a, #f2c94c);",
            "-fx-background-color: linear-gradient(to top, #8e2de2, #4a00e0);" 
        };

        int limit = Math.min(5, sortedList.size());
        
        //  1. TÌM GIÁ TRỊ CAO NHẤT ĐỂ QUYẾT ĐỊNH ĐƠN VỊ THÔNG MINH
        double maxPrice = limit > 0 ? sortedList.get(0).getCurPrice() : 0;
        double divisor = 1.0;
        String unitLabel = "Giá Hiện Tại (VNĐ)";

        if (maxPrice >= 1_000_000_000) {
            divisor = 1_000_000_000.0;
            unitLabel = "Giá Hiện Tại (Tỷ VNĐ)";
        } else if (maxPrice >= 1_000_000) {
            divisor = 1_000_000.0;
            unitLabel = "Giá Hiện Tại (Triệu VNĐ)";
        } else if (maxPrice >= 1_000) {
            divisor = 1_000.0;
            unitLabel = "Giá Hiện Tại (Nghìn VNĐ)";
        }

        for (int i = 0; i < limit; i++) {
            AuctionInfo info = sortedList.get(i);
            String labelX = String.valueOf(info.getId()); 
            
            //  2. SCALE GIÁ TRỊ VÀ LÀM TRÒN
            double scaledPrice = info.getCurPrice() / divisor;
            scaledPrice = Math.round(scaledPrice * 10.0) / 10.0; // Làm tròn 1 chữ số thập phân
            
            series.getData().add(new javafx.scene.chart.XYChart.Data<>(labelX, scaledPrice));
        }

        topAuctionsBarChart.getData().add(series);
        barYAxis.setForceZeroInRange(false);
        
        //  3. CẬP NHẬT TÊN TRỤC Y CHO ĐÚNG ĐƠN VỊ (Rất quan trọng)
        barYAxis.setLabel(unitLabel);

        // --- RENDER GIAO DIỆN & TÍCH HỢP SỰ KIỆN CLICK ---
        Platform.runLater(() -> {
            java.text.DecimalFormat df = new java.text.DecimalFormat("#,### đ");
            
            // 1. ÉP MÀU CHO BIỂU ĐỒ TRÒN VÀ CHÚ THÍCH (FIX LỖI LỆCH MÀU)
            String[] pieColors = {"#FF5E62", "#38EF7D", "#00C6FF"}; 
            int pIdx = 0;
            for (javafx.scene.chart.PieChart.Data data : categoryPieChart.getData()) {
                javafx.scene.Node node = data.getNode();
                if (node != null) {
                    node.setStyle("-fx-pie-color: " + pieColors[pIdx % pieColors.length] + ";");
                    
                    javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip(data.getName() + "\nSố lượng: " + (int)data.getPieValue() + " sản phẩm");
                    tooltip.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
                    tooltip.setShowDelay(javafx.util.Duration.millis(0));
                    tooltip.setHideDelay(javafx.util.Duration.millis(0));
                    javafx.scene.control.Tooltip.install(node, tooltip);
                    pIdx++;
                }
            }
            
            // Ép JavaFX cập nhật Layout ngay lập tức để bóc tách node Legend
            categoryPieChart.applyCss();
            categoryPieChart.layout();
            
            int lIdx = 0;
            for (javafx.scene.Node legendNode : categoryPieChart.lookupAll(".chart-legend-item-symbol")) {
                // Chỉ giữ lại màu gốc, xóa lớp màu trắng đè lên
                legendNode.setStyle("-fx-background-color: " + pieColors[lIdx % pieColors.length] + ";");
                lIdx++;
            }

            // 2. TÔ MÀU VÀ GẮN SỰ KIỆN CLICK CHO BAR CHART
            for (int i = 0; i < series.getData().size(); i++) {
                javafx.scene.chart.XYChart.Data<String, Number> dataNode = series.getData().get(i);
                javafx.scene.Node node = dataNode.getNode();
                AuctionInfo originalInfo = sortedList.get(i);
                
                if (node != null) {
                    node.setStyle(gradientColors[i % gradientColors.length] + " -fx-background-radius: 6 6 0 0; -fx-cursor: hand;");
                    
                    javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip("Mã Phiên: " + dataNode.getXValue() + "\nGiá trị: " + df.format(originalInfo.getCurPrice()) + "\n(Click để xem chi tiết)");
                    tooltip.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
                    tooltip.setShowDelay(javafx.util.Duration.millis(0));
                    tooltip.setHideDelay(javafx.util.Duration.millis(0));
                    javafx.scene.control.Tooltip.install(node, tooltip);
                    
                    // SỰ KIỆN CLICK ĐỂ CHUYỂN TAB VÀ TÌM KIẾM
                    node.setOnMouseClicked(event -> {
                        System.out.println("Mở chi tiết vật phẩm ID: " + originalInfo.getId());
                        // Chuyển sang Tab Quản lý vật phẩm
                        navigateToTab(viewAuctionItems, btnAuctionItems);
                        // Tự động điền mã ID vào thanh tìm kiếm và gọi hàm lọc
                        if (searchItemField != null) {
                            searchItemField.setText(String.valueOf(originalInfo.getId()));
                            handleSearchItem(null);
                        }
                    });
                }
            }
        });
    }

    private void applyChartFilter() {
        if (cbTimeFilter == null || cbTimeFilter.getValue() == null) {
            updateAnalyticsCharts(itemListData);
            return;
        }
        
        String selection = cbTimeFilter.getValue();
        java.util.List<AuctionInfo> filtered = new java.util.ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (AuctionInfo item : itemListData) {
            if ("Hôm nay".equals(selection)) {
                if (item.getEndTime() != null && item.getEndTime().toLocalDate().equals(now.toLocalDate())) {
                    filtered.add(item);
                }
            } else if ("7 ngày qua".equals(selection)) {
                if (item.getEndTime() != null && item.getEndTime().isAfter(now.minusDays(7))) {
                    filtered.add(item);
                }
            } else {
                filtered.add(item); // "Tất cả thời gian"
            }
        }
        updateAnalyticsCharts(filtered);
    }

    private void updateOverviewCards() {
        if (lblActiveAuctions == null || lblTotalVolume == null || lblNewBids == null) return;

        long activeCount = 0;
        double totalVolume = 0;
        
        for (AuctionInfo item : itemListData) {
            //  1. SỬA LỖI TRẠNG THÁI: Trong DB của bạn là OPEN chứ không phải ONGOING
            if (item.getStatus() != null && item.getStatus() == com.mikey.auction.auction.AuctionStatus.OPEN) {
                activeCount++;
            }
            
            //  2. TỔNG GIÁ TRỊ GIAO DỊCH: Chỉ cộng tiền của các phiên ĐÃ CÓ người trả giá 
            // (Tránh việc cộng nhầm giá khởi điểm của các phiên trống)
            if (item.getLastBidderName() != null && !item.getLastBidderName().isEmpty()) {
                totalVolume += item.getCurPrice(); 
            }
        }

        //  3. LƯỢT TRẢ GIÁ MỚI: Bằng chính số lượng bản ghi trong bảng bidTransactions
        int totalBids = bidHistoryData.size();

        final long finalActiveCount = activeCount;
        final double finalTotalVolume = totalVolume;
        
        // Đẩy số liệu thật lên giao diện JavaFX
        Platform.runLater(() -> {
            java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,### đ");
            lblActiveAuctions.setText(String.valueOf(finalActiveCount));
            lblTotalVolume.setText(formatter.format(finalTotalVolume));
            lblNewBids.setText(String.valueOf(totalBids));
        });
    }

    @FXML
    void handleSearchItem(ActionEvent event) {
        if (searchItemField == null) return;
        
        String filterText = searchItemField.getText().trim().toLowerCase();
        
        if (filterText.isEmpty()) {
            // Nếu ô tìm kiếm trống, trả lại toàn bộ dữ liệu gốc
            itemTable.setItems(itemListData); 
        } else {
            // Bộ lọc thông minh: Quét theo cả Tên vật phẩm VÀ Mã phiên đấu giá
            javafx.collections.ObservableList<AuctionInfo> filteredList = javafx.collections.FXCollections.observableArrayList();
            
            for (AuctionInfo info : itemListData) {
                String title = (info.getItemInfo() != null && info.getItemInfo().getTitle() != null) ? info.getItemInfo().getTitle().toLowerCase() : "";
                String idStr = String.valueOf(info.getId());
                
                if (title.contains(filterText) || idStr.contains(filterText)) {
                    filteredList.add(info);
                }
            }
            itemTable.setItems(filteredList);
            itemTable.refresh();
        }
    }

    @FXML
    void handleSearchUser(ActionEvent event) {
        if (searchUserField == null) return;
        
        String filterText = searchUserField.getText().trim().toLowerCase();
        
        if (filterText.isEmpty()) {
            userTable.setItems(userListData); 
        } else {
            // Bộ lọc thông minh: Quét theo cả Username VÀ ID người dùng
            javafx.collections.ObservableList<User> filteredList = javafx.collections.FXCollections.observableArrayList();
            
            for (User u : userListData) {
                String username = (u.getUsername() != null) ? u.getUsername().toLowerCase() : "";
                String idStr = String.valueOf(u.getId());
                
                if (username.contains(filterText) || idStr.contains(filterText)) {
                    filteredList.add(u);
                }
            }
            userTable.setItems(filteredList);
            userTable.refresh();
        }
    }

    @FXML
    void handleSearchBid(ActionEvent event) {
        if (searchBidField == null) return;
        
        String filterText = searchBidField.getText().trim().toLowerCase();
        
        if (filterText.isEmpty()) {
            // Trả lại dữ liệu gốc nếu ô tìm kiếm trống
            bidHistoryTable.setItems(bidHistoryData); 
        } else {
            javafx.collections.ObservableList<com.mikey.auction.dto.BidHistory> filteredList = javafx.collections.FXCollections.observableArrayList();
            
            for (com.mikey.auction.dto.BidHistory bid : bidHistoryData) {
                // Quét Mã phiên và Tên người dùng
                String aucIdStr = String.valueOf(bid.getAuctionId());
                String bidderName = (bid.getBidderUsername() != null) ? bid.getBidderUsername().toLowerCase() : "";
                
                // Nếu 1 trong 2 cột có chứa từ khóa thì hốt vào danh sách
                if (aucIdStr.contains(filterText) || bidderName.contains(filterText)) {
                    filteredList.add(bid);
                }
            }
            bidHistoryTable.setItems(filteredList);
        }
        bidHistoryTable.refresh(); // Ép giao diện vẽ lại ngay lập tức
    }
}