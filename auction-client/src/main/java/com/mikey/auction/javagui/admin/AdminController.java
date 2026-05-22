package com.mikey.auction.javagui.admin;

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

    // TAB 1: Bảng danh sách vật phẩm đấu giá mặc định
    @FXML private TableView<?> auctionTable;
    @FXML private TableColumn<?, ?> colId;
    @FXML private TableColumn<?, ?> colItemName;
    @FXML private TableColumn<?, ?> colCurrentBid;
    @FXML private TableColumn<?, ?> colBidder;
    @FXML private TableColumn<?, ?> colStatus;

    // TAB 2: Các trường bổ sung của quản lý vật phẩm chi tiết
    @FXML private TableView<?> itemTable;
    @FXML private TableColumn<?, ?> colItemAucId;
    @FXML private TableColumn<?, ?> colItemTitle;
    @FXML private TableColumn<?, ?> colItemSeller;
    @FXML private TableColumn<?, ?> colItemStartPrice;
    @FXML private TableColumn<?, ?> colItemEndTime;
    @FXML private TextField searchItemField;

    // TAB 3: Bảng theo dõi lịch sử đặt cược dòng tiền toàn cục
    @FXML private TableView<?> bidHistoryTable;
    @FXML private TableColumn<?, ?> colHistBidId;
    @FXML private TableColumn<?, ?> colHistAucId;
    @FXML private TableColumn<?, ?> colHistBidder;
    @FXML private TableColumn<?, ?> colHistAmount;
    @FXML private TableColumn<?, ?> colHistTime;

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

    @FXML
    public void initialize() {
        SocketClient.getInstance().setListener(this);
        serverStatusIndicator.setStyle("-fx-fill: #38EF7D;");
        serverStatusLabel.setText("Server: Hệ thống vận hành mượt mà");
        
        // 👉 GỌI HÀM CÀI ĐẶT BẢNG NGAY KHI MỞ APP
        setupTable();
    }

    private void setupTable() {
        // 1. Chỉ định cột nào lấy dữ liệu từ biến nào trong class User
        colUserIdx.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUserUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colUserRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colUserStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        // 2. Ma thuật tạo Nút Bấm trực tiếp trong ô TableView
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = new Callback<>() {
            @Override
            public javafx.scene.control.TableCell<User, Void> call(final TableColumn<User, Void> param) {
                return new javafx.scene.control.TableCell<>() {
                    private final Button btnBan = new Button("Khóa (Ban)");
                    {
                        btnBan.setStyle("-fx-background-color: #FF5E62; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 5;");
                        btnBan.setOnAction((ActionEvent event) -> {
                            User data = getTableView().getItems().get(getIndex());
                            System.out.println("Đang gửi lệnh khóa tài khoản ID: " + data.getId());
                            // Mở comment dòng dưới khi có lệnh ở RequestHandler:
                            // RequestHandler.getInstance().requestBanUser(data.getId());
                        });
                    }
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) { setGraphic(null); } 
                        else { setGraphic(btnBan); }
                    }
                };
            }
        };
        colUserAction.setCellFactory(cellFactory);
        
        // Liên kết dữ liệu với bảng
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
    }

    @FXML
    void showAuctionItems(ActionEvent event) {
        navigateToTab(viewAuctionItems, btnAuctionItems);
    }

    @FXML
    void showBiddingHistory(ActionEvent event) {
        navigateToTab(viewBiddingHistory, btnBiddingHistory);
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
                            String roleStr = obj.get("role").getAsString();
                            
                            // Tự tay phân loại đối tượng để tránh lỗi Abstract Class
                            if ("ADMIN".equals(roleStr)) {
                                users.add(new com.mikey.auction.user.Admin(un, pw, id));
                            } else if ("SELLER".equals(roleStr)) {
                                users.add(new com.mikey.auction.user.Seller(un, pw, id));
                            } else {
                                users.add(new com.mikey.auction.user.Bidder(un, pw, id));
                            }
                        }
                        
                        userListData.clear();
                        userListData.addAll(users);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}