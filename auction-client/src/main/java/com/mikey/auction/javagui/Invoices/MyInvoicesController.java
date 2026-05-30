package com.mikey.auction.javagui.Invoices;

import com.mikey.auction.dto.InvoiceInfo;
import com.mikey.auction.javagui.login.LoginController;
import com.mikey.auction.socket.RequestHandler;
import com.mikey.auction.socket.SocketClient;
import com.mikey.auction.socket.SocketListener;
import com.mikey.auction.user.User;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MyInvoicesController implements SocketListener {

    @FXML private TableView<InvoiceInfo> invoiceTable;
    @FXML private TableColumn<InvoiceInfo, Integer> colId;
    @FXML private TableColumn<InvoiceInfo, Integer> colAuctionId;
    @FXML private TableColumn<InvoiceInfo, String> colAmount;
    @FXML private TableColumn<InvoiceInfo, String> colStatus;
    @FXML private TableColumn<InvoiceInfo, InvoiceInfo> colAction;

    private ObservableList<InvoiceInfo> invoiceList = FXCollections.observableArrayList();
    private final Gson gson = new com.google.gson.GsonBuilder()
            .registerTypeAdapter(java.time.LocalDateTime.class, (com.google.gson.JsonSerializer<java.time.LocalDateTime>) (src, t, ctx) -> new com.google.gson.JsonPrimitive(src.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .registerTypeAdapter(java.time.LocalDateTime.class, (com.google.gson.JsonDeserializer<java.time.LocalDateTime>) (json, t, ctx) -> java.time.LocalDateTime.parse(json.getAsString(), java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .create();

    @FXML
    public void initialize() {
        // Đăng ký nghe ngóng từ Server
        SocketClient.getInstance().setListener(this);

        // Thiết lập cách hiển thị dữ liệu cho các cột
        colId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
        colAuctionId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getAuctionId()));
        colAmount.setCellValueFactory(data -> new SimpleStringProperty(String.format("%,.0f đ", data.getValue().getAmount())));
        
        colStatus.setCellValueFactory(data -> {
            String st = data.getValue().getStatus();
            String display = st.equals("AWAITING_PAYMENT") ? "⏳ Chờ thanh toán" : (st.equals("PAID") ? "✅ Đã thanh toán" : "🚫 Đã hủy");
            return new SimpleStringProperty(display);
        });

        // TẠO NÚT "THANH TOÁN" THẦN THÁNH Ở CỘT CUỐI
        colAction.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button payBtn = new Button("Thanh toán ngay");
            {
                payBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                payBtn.setOnAction(event -> {
                    InvoiceInfo inv = getTableView().getItems().get(getIndex());
                    // Gửi lệnh thanh toán lên Server
                    payBtn.setText("Đang xử lý...");
                    payBtn.setDisable(true);
                    RequestHandler.getInstance().requestPayInvoice(inv.getId(), LoginController.currentUser.getId());
                });
            }

            @Override
            protected void updateItem(InvoiceInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    // Chỉ hiện nút Thanh toán nếu trạng thái là đang chờ
                    if ("AWAITING_PAYMENT".equals(item.getStatus())) {
                        setGraphic(payBtn);
                    } else {
                        setGraphic(null); // Giấu nút đi nếu đã thanh toán rồi
                    }
                }
            }
        });

        invoiceTable.setItems(invoiceList);

        // Vừa mở màn hình là gọi Server xin luôn danh sách Hóa đơn
        if (LoginController.currentUser != null) {
            RequestHandler.getInstance().requestUserInvoices(LoginController.currentUser.getId());
        }
    }

    @Override
    public void onResponseReceived(String category, String action, String jsonData) {
        Platform.runLater(() -> {
            try {
                if ("AUCTION".equals(category)) {
                    if ("INVOICE_GET".equals(action)) {
                        // Nhận danh sách hóa đơn và đập vào Bảng
                        java.lang.reflect.Type type = new TypeToken<ArrayList<InvoiceInfo>>(){}.getType();
                        ArrayList<InvoiceInfo> list = gson.fromJson(jsonData, type);
                        invoiceList.setAll(list);

                    } else if ("INVOICE_PAY".equals(action)) {
                        // Nhận kết quả thanh toán
                        if ("SUCCESS".equals(jsonData.replace("\"", ""))) {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Thành công");
                            alert.setHeaderText(null);
                            alert.setContentText("🎉 Thanh toán thành công! Hệ thống đang chuẩn bị giao hàng cho bạn.");
                            alert.showAndWait();
                            
                            // Load lại bảng để nút thanh toán biến mất
                            RequestHandler.getInstance().requestUserInvoices(LoginController.currentUser.getId());
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Lỗi");
                            alert.setHeaderText(null);
                            alert.setContentText("❌ Thanh toán thất bại. Vui lòng thử lại sau!");
                            alert.showAndWait();
                        }
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    private User user;

    public void setUser(User user) {
        this.user = user;
        // Gọi Server lấy hóa đơn khi đã có thông tin user
        if (this.user != null) {
            RequestHandler.getInstance().requestUserInvoices(this.user.getId());
        }
    }

    @FXML
    public void handleBack(javafx.event.ActionEvent event) {
        // Trả người dùng về giao diện Hub chính (Auction Hub)
        if (this.user != null) {
            com.mikey.auction.javagui.SceneChanger.getInstance().toBidder(this.user);
        } else {
            com.mikey.auction.javagui.SceneChanger.getInstance().toLogin();
        }
    }
}