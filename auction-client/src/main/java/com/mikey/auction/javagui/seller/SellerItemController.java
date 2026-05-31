package com.mikey.auction.javagui.seller;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mikey.auction.auction.AuctionStatus;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.dto.ItemSummary;
import com.mikey.auction.javagui.login.LoginController;
import com.mikey.auction.socket.RequestHandler;
import com.mikey.auction.socket.SocketClient;
import com.mikey.auction.socket.SocketListener;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SellerItemController implements SocketListener {
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

    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Node cardRoot;
    @FXML private VBox mainCard;

    @FXML
    public void setData(AuctionInfo i){
        ItemSummary itemSummary = i.getItemInfo();
        
        // 👉 1. Cập nhật Text hiển thị cả Loại sản phẩm và Trạng thái
        type.setText(itemSummary.getItemType().name() + " | " + i.getStatus().name());

        // 👉 2. Đổi màu Label type dựa trên trạng thái để người bán dễ nhìn
        String statusStr = i.getStatus().name();
        if ("OPEN".equals(statusStr)) {
            type.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;"); // Xanh lá
        } else if ("PENDING".equals(statusStr)) {
            type.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black;"); // Vàng
        } else {
            type.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;"); // Đỏ cho CLOSED/CANCELED
        }

        // 3. Đổ các dữ liệu còn lại
        itemName.setText(itemSummary.getTitle());
        sellerName.setText(i.getSellerUsername());
        curPrice.setText(String.format("%,.0f đ", i.getCurPrice()));
        date.setText(i.getEndTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        this.auctionInfo = i;
        
        // 4. Xử lý load ảnh (Giữ nguyên code cũ của bạn)
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
        boolean hasBidder = auctionInfo.getLastBidderName() != null && !auctionInfo.getLastBidderName().isEmpty();

        // 3. LOGIC XÓA THÔNG MINH:
        if (auctionInfo.getStatus() == AuctionStatus.OPEN && hasBidder) {
            // Trường hợp 1: Đang mở bán VÀ Đã có người trả giá -> CẤM XÓA
            Alert warning = new Alert(Alert.AlertType.WARNING, "Không thể xóa! Đã có người trả giá cho sản phẩm này.", ButtonType.OK);
            warning.showAndWait();
            return;
        } else if (auctionInfo.getStatus() == AuctionStatus.CLOSED || auctionInfo.getStatus() == AuctionStatus.CANCELED) {
            // Trường hợp 2: Đã kết thúc hoặc đã hủy -> CẤM THAO TÁC
            Alert warning = new Alert(Alert.AlertType.WARNING, "Phiên này đã kết thúc, không thể thao tác thêm!", ButtonType.OK);
            warning.showAndWait();
            return;
        }

        // Trường hợp 3: PENDING (Chưa mở) HOẶC OPEN nhưng CHƯA CÓ AI ĐẶT GIÁ -> CHO PHÉP XÓA
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Hủy sản phẩm: " + auctionInfo.getItemInfo().getTitle());
        alert.setContentText("Bạn có chắc chắn muốn hủy phiên đấu giá này không?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            // Lắng nghe Server
            SocketClient.getInstance().setListener(this);
            
            // Tạm khóa nút tránh bấm 2 lần
            if (btnDelete != null) {
                btnDelete.setText("Đang xóa...");
                btnDelete.setDisable(true);
            }
            if (btnEdit != null) btnEdit.setDisable(true);

            System.out.println("Gửi yêu cầu hủy ID: " + auctionInfo.getId());
            RequestHandler.getInstance().requestDeleteAuctionSeller(auctionInfo.getId(), this.userId); 
        }
    }

    @Override
    public void onResponseReceived(String category, String action, String jsonData) {
        if ("AUCTION".equals(category) && "DELETE".equals(action)) {
            Platform.runLater(() -> {
                if (jsonData.contains("SUCCESS")) {
                    // 👉 GỌI TRỰC TIẾP TÊN CÁI KHUNG NGOÀI CÙNG ĐỂ XÓA TẬN GỐC
                    if (mainCard != null) {
                        javafx.scene.layout.Pane parent = (javafx.scene.layout.Pane) mainCard.getParent();
                        if (parent != null) {
                            parent.getChildren().remove(mainCard);
                        }
                    }
                } else if (jsonData.contains("ERROR_SELLER_DENIED")) {
                    if (btnDelete != null) {
                        btnDelete.setText("Xóa");
                        btnDelete.setDisable(false);
                    }
                    if (btnEdit != null) btnEdit.setDisable(false);

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Không thể hủy");
                    alert.setHeaderText("Hủy phiên đấu giá thất bại!");
                    alert.setContentText("Sản phẩm đã có người trả giá. Để bảo vệ người mua, bạn không thể hủy ngang!");
                    alert.showAndWait();
                }
            });
        }
    }

    @FXML
    public void handleEdit(ActionEvent event) {
        // 1. Chặn nếu phiên đã đóng
        if (auctionInfo.getStatus() == AuctionStatus.CLOSED || auctionInfo.getStatus() == AuctionStatus.CANCELED) {
            Alert warning = new Alert(Alert.AlertType.WARNING, "Không thể sửa phiên đã kết thúc!", ButtonType.OK);
            warning.showAndWait();
            return;
        }

        try {
            // 2. Load file Seller.fxml (form nhập liệu)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mikey/auction/javagui/seller/Seller.fxml"));
            Parent root = loader.load();

            // 3. Lấy controller của form đó và gọi hàm tự động điền thông tin (EditMode)
            SellerController controller = loader.getController();
            controller.setUser(LoginController.currentUser); // Truyền user hiện tại vào
            controller.setEditMode(auctionInfo); // <--- ĐÂY LÀ HÀM TỰ ĐIỀN THÔNG TIN BẠN ĐÃ VIẾT

            // 4. Tìm cái khung ScrollPane của màn hình chính để nhét form vào (Giữ nguyên Menu)
            // Lưu ý: ID "contentScrollPane" phải khớp với ID trong file SellerHub.fxml của bạn
            javafx.scene.control.ScrollPane hubScrollPane = 
                (javafx.scene.control.ScrollPane) ((Node) event.getSource()).getScene().lookup("#contentScrollPane");
            
            if (hubScrollPane != null) {
                hubScrollPane.setContent(root);
                System.out.println("Đã chuyển sang chế độ Sửa cho sản phẩm: " + auctionInfo.getItemInfo().getTitle());
            } else {
                // Nếu không tìm thấy ScrollPane (phòng hờ), thì đè cả màn hình
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.getScene().setRoot(root);
            }

            

        } catch (Exception e) {
            System.err.println("Lỗi khi mở form sửa: " + e.getMessage());
            e.printStackTrace();
        }
    }
}