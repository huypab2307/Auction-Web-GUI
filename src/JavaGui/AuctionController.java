package JavaGui;

import Auction.*;
import Database.AuctionDAO;
import User.*; 
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert; // Import Alert
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

public class AuctionController implements Initializable {

    @FXML
    private VBox itemContainer;

    // Biến lưu trữ người dùng đang đăng nhập
    private Bidder currentUser; 

    // Hàm để MainController truyền User vào
    public void setCurrentUser(Bidder user) {
        this.currentUser = user;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadAuctionList();
    }

    private void loadAuctionList() {
        itemContainer.getChildren().clear();
        ArrayList<Auction> auctions = AuctionDAO.getInstance().getAllAuctions();

        for (Auction auction : auctions) {
            HBox card = createAuctionCard(auction);
            itemContainer.getChildren().add(card);
        }
    }

    private HBox createAuctionCard(Auction auction) {
        HBox card = new HBox(20);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        VBox imageBox = new VBox();
        imageBox.setPrefSize(120, 100);
        imageBox.setStyle("-fx-background-color: #bdc3c7; -fx-background-radius: 5;");

        // --- LẤY THÔNG TIN TỪ DATABASE ---
        User seller = Database.UserDAO.getInstance().findById(auction.getSellerId());
        String sellerName = (seller != null) ? seller.getUsername() : "Không xác định";

        String bidderName = "Chưa có ai đặt giá";
        if (auction.getLastBidder() > 0) {
            User bidder = Database.UserDAO.getInstance().findById(auction.getLastBidder());
            if (bidder != null) {
                bidderName = bidder.getUsername();
            }
        }

        String[] itemInfo = Database.ArtsDAO.getInstance().getItemDisplayInfo(auction.getItemId());
        String itemTitle = itemInfo[0];
        String itemDetails = itemInfo[1];

        // --- XÂY DỰNG GIAO DIỆN CHỮ ---
        VBox infoBox = new VBox(8);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label nameLabel = new Label(itemTitle);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        nameLabel.setTextFill(Color.web("#2c3e50"));

        Label detailsLabel = new Label(itemDetails);
        detailsLabel.setTextFill(Color.web("#7f8c8d"));
        detailsLabel.setWrapText(true); 

        HBox userBox = new HBox(30);
        Label sellerLabel = new Label("Người bán: " + sellerName);
        sellerLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #95a5a6;");
        
        Label bidderLabel = new Label("Người giữ giá: " + bidderName);
        bidderLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");
        userBox.getChildren().addAll(sellerLabel, bidderLabel);

        HBox priceTimeBox = new HBox(30);
        Label priceLabel = new Label("Giá hiện tại: " + String.format("%,.0f", auction.getCurPrice()) + "đ");
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        priceLabel.setTextFill(Color.web("#e67e22"));

        Label timeLabel = new Label("Hết hạn: " + auction.getEndTime().toLocalDate().toString());
        timeLabel.setTextFill(Color.web("#c0392b"));

        priceTimeBox.getChildren().addAll(priceLabel, timeLabel);
        infoBox.getChildren().addAll(nameLabel, detailsLabel, userBox, priceTimeBox);

        // Nút Đặt giá
        Button bidBtn = new Button("ĐẶT GIÁ");
        bidBtn.setPrefSize(100, 40);
        bidBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        
        // --- XỬ LÝ SỰ KIỆN KHI BẤM NÚT ĐẶT GIÁ ---
        bidBtn.setOnAction(event -> {
            if (currentUser != null) {
                try {
                    // Gọi method placeBid có sẵn trong class Bidder của bạn
                    boolean check = currentUser.placeBid(auction.getId(),auction.getCurPrice());

                    // Hiện thông báo thành công
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    if (check){
                        alert.setTitle("Thành công");
                        alert.setHeaderText(null);
                        alert.setContentText("Bạn đã đặt giá thành công cho sản phẩm: " + itemTitle);
                    } else{
                        alert.setTitle(" Không Thành công");
                        alert.setHeaderText(null);
                        alert.setContentText("Bạn đã đặt giá Không thành công cho sản phẩm: " + itemTitle);
                    }
                    alert.showAndWait();

                    // Tải lại danh sách giao diện ngay lập tức để cập nhật giá mới
                    loadAuctionList();
                    
                } catch (Exception e) {
                    // Hiện thông báo lỗi nếu có Exception (ví dụ database lỗi)
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Lỗi đấu giá");
                    alert.setHeaderText(null);
                    alert.setContentText("Có lỗi xảy ra: " + e.getMessage());
                    alert.showAndWait();
                }
            } else {
                System.out.println("Lỗi: Không tìm thấy phiên đăng nhập của người dùng.");
            }
        });

        card.getChildren().addAll(imageBox, infoBox, bidBtn);
        return card;
    }

    @FXML
    public void handleLogout(ActionEvent event) throws IOException {
        currentUser = null; // Xóa phiên đăng nhập khi đăng xuất
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("main.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 400, 500);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
    @FXML
    public void showNotifications(ActionEvent event) {
        if (currentUser == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Cảnh báo");
            alert.setHeaderText(null);
            alert.setContentText("Người dùng chưa đăng nhập!");
            alert.showAndWait();
            return;
        }

        try (java.sql.Connection conn = Database.NotificationDAO.getInstance().getConnect()) {
            Database.NotificationDAO notifDAO = Database.NotificationDAO.getInstance();

            // 1. Lấy danh sách thông báo chưa đọc (isReaded = false) và đã đọc (isReaded = true)
            java.util.ArrayList<Notifications> unreadList = notifDAO.checkNotifications(conn, currentUser.getId(), false);
            java.util.ArrayList<Notifications> readList = notifDAO.checkNotifications(conn, currentUser.getId(), true);

            // Gộp chung vào 1 danh sách
            java.util.ArrayList<Notifications> allNotifs = new java.util.ArrayList<>();
            allNotifs.addAll(unreadList);
            allNotifs.addAll(readList);

            // Sắp xếp lại để thông báo mới nhất nổi lên trên cùng
            allNotifs.sort((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()));

            // 2. Tạo Dialog (Cửa sổ Pop-up)
            javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
            dialog.setTitle("Thông báo hệ thống");
            dialog.setHeaderText("Toàn bộ thông báo của bạn (" + unreadList.size() + " chưa đọc)");
            
            // Thêm nút Đóng (Close) vào Dialog
            dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);

            // 3. Tạo vùng chứa VBox để vẽ từng thông báo
            javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(10);
            vbox.setStyle("-fx-padding: 15; -fx-background-color: white;");

            if (allNotifs.isEmpty()) {
                vbox.getChildren().add(new javafx.scene.control.Label("Bạn không có thông báo nào."));
            } else {
                for (Notifications n : allNotifs) {
                    javafx.scene.layout.VBox item = new javafx.scene.layout.VBox(5);
                    
                    // Kiểm tra xem tin này chưa đọc hay đã đọc để tô màu nền
                    boolean isUnread = unreadList.stream().anyMatch(u -> u.getId() == n.getId());
                    
                    // Nền màu xanh nhạt nếu chưa đọc, màu xám nếu đã đọc
                    String bgColor = isUnread ? "#eafaf1" : "#f8f9fa"; 
                    String borderColor = isUnread ? "#2ecc71" : "#dee2e6";
                    
                    item.setStyle("-fx-background-color: " + bgColor + "; -fx-padding: 12; -fx-border-color: " + borderColor + "; -fx-border-radius: 8; -fx-background-radius: 8;");
                    
                    javafx.scene.control.Label msgLabel = new javafx.scene.control.Label(n.getMessage());
                    msgLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 14px;");
                    msgLabel.setWrapText(true); // Tự xuống dòng nếu chữ dài
                    
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    javafx.scene.control.Label timeLabel = new javafx.scene.control.Label(n.getCreatedAt().format(formatter));
                    timeLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px; -fx-font-style: italic;");
                    
                    item.getChildren().addAll(msgLabel, timeLabel);
                    vbox.getChildren().add(item);
                }
            }

            // Đưa vào ScrollPane để cuộn được nếu có quá nhiều thông báo
            javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(vbox);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(450, 400); // Khung Pop-up rộng 450x400
            scrollPane.setStyle("-fx-background-color: transparent; -fx-background: white;");

            dialog.getDialogPane().setContent(scrollPane);

            // 4. Đánh dấu đã đọc trên CSDL
            if (!unreadList.isEmpty()) {
                notifDAO.markAllAsRead(conn, currentUser.getId());
            }

            // Mở Pop-up
            dialog.showAndWait();

        } catch (java.sql.SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText("Không thể tải thông báo!");
            alert.setContentText("Có lỗi xảy ra kết nối CSDL: " + e.getMessage());
            alert.showAndWait();
        }
    }
}