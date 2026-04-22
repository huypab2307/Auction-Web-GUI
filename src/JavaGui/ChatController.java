package JavaGui;

import Auction.Message;
import Auction.NotificationManager;
import Database.UserDAO;
import User.Bidder;
import User.User;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class ChatController {

    @FXML private TextField searchUserField;
    @FXML private VBox userListContainer;
    @FXML private Label chatUserNameLabel;
    @FXML private VBox chatHistoryBox;
    @FXML private TextField messageInput;
    @FXML private ScrollPane chatScrollPane;

    private Bidder currentUser;
    private User targetUser = null; 

    public void setCurrentUser(Bidder user) {
        this.currentUser = user;
        loadRecentChats();
    }

    // 1. Tải danh sách người đã từng nhắn tin
    private void loadRecentChats() {
        userListContainer.getChildren().clear();
        if (currentUser == null) return;

        List<Integer> friendIds = NotificationManager.getInstance().loadUserMessageList(currentUser.getId());
        if (friendIds != null) {
            for (int friendId : friendIds) {
                User friend = UserDAO.getInstance().findById(friendId);
                if (friend != null) {
                    addUserToSideBar(friend);
                }
            }
        }
    }

    private void addUserToSideBar(User friend) {
        HBox userBox = new HBox(10);
        userBox.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-radius: 10; -fx-background-radius: 10; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");
        
        Label name = new Label("👤 " + friend.getUsername());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #2c3e50;");
        userBox.getChildren().add(name);

        // Khi click vào tên -> Mở chat
        userBox.setOnMouseClicked(event -> openChatHistory(friend));
        userListContainer.getChildren().add(userBox);
    }

    // 2. Tìm kiếm người mới để chat
    @FXML
    public void handleSearchUser() {
        String username = searchUserField.getText().trim();
        if (username.isEmpty()) return;

        User foundUser = UserDAO.getInstance().findByUsername(username);
        if (foundUser != null && foundUser.getId() != currentUser.getId()) {
            openChatHistory(foundUser);
            // Có thể thêm logic kiểm tra trùng trước khi add vào Sidebar
            addUserToSideBar(foundUser);
            searchUserField.clear();
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText("Không tìm thấy người dùng này hoặc bạn đang tự tìm chính mình.");
            alert.showAndWait();
        }
    }

    // 3. Hiển thị lịch sử tin nhắn
    private void openChatHistory(User friend) {
        this.targetUser = friend;
        chatUserNameLabel.setText("Đang chat với: " + friend.getUsername());
        chatHistoryBox.getChildren().clear();

        List<Message> messages = NotificationManager.getInstance().messageList(currentUser.getId(), friend.getId());
        if (messages != null) {
            for (Message msg : messages) {
                addMessageToScreen(msg);
            }
        }
        // Tự động cuộn xuống cuối cùng
        chatScrollPane.vvalueProperty().bind(chatHistoryBox.heightProperty());
    }

    // Vẽ từng bong bóng tin nhắn
    private void addMessageToScreen(Message msg) {
        HBox row = new HBox();
        // LƯU Ý: Chữ của bạn nằm trong trường context (getContext)
        Label bubble = new Label(msg.getContext()); 
        bubble.setWrapText(true);
        bubble.setMaxWidth(350);

        // Mình gửi (Bên phải, Xanh dương)
        if (msg.getSenderId() == currentUser.getId()) {
            row.setAlignment(Pos.CENTER_RIGHT);
            bubble.setStyle("-fx-background-color: #0084ff; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 15; -fx-font-size: 14;");
        } 
        // Người khác gửi (Bên trái, Xám)
        else {
            row.setAlignment(Pos.CENTER_LEFT);
            bubble.setStyle("-fx-background-color: #f1f0f0; -fx-text-fill: black; -fx-padding: 10 15; -fx-background-radius: 15; -fx-font-size: 14;");
        }

        row.getChildren().add(bubble);
        chatHistoryBox.getChildren().add(row);
    }

    // 4. Gửi tin nhắn mới
    @FXML
    public void handleSendMessage() {
        if (targetUser == null) return;
        
        String content = messageInput.getText().trim();
        if (content.isEmpty()) return;

        // Lưu vào DB
        boolean success = NotificationManager.getInstance().sendChatMessage(currentUser.getId(), targetUser.getId(), content);
        if (success) {
            messageInput.clear();
            openChatHistory(targetUser); // Load lại để hiện tin nhắn vừa gửi
        }
    }
}