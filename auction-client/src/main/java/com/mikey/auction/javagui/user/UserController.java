package com.mikey.auction.javagui.user;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

import com.mikey.auction.socket.RequestHandler;
import com.mikey.auction.user.User;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class UserController {
    public TextField usernameField;
    private User user;
    private Stage stage;
    public Circle avatarCircle;

    public void initialize() {
    }

    public void setUser(User user) {
        this.user = user;
        usernameField.setText(user.getUsername());
        // Hiển thị ảnh đại diện nếu có
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
        try {
            // Giải mã chuỗi Base64 ngược lại thành mảng byte
            byte[] imageBytes = java.util.Base64.getDecoder().decode(user.getAvatar());
            // Tạo Image từ mảng byte
            Image image = new Image(new java.io.ByteArrayInputStream(imageBytes));
            // Đổ ảnh vào Circle
            avatarCircle.setFill(new ImagePattern(image));
        } catch (Exception e) {
            System.err.println("Lỗi hiển thị ảnh đại diện cũ: " + e.getMessage());
        }
    }
    }
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void handleChangePassword(ActionEvent actionEvent) throws IOException {
        navigate("change_password.fxml");
    }

    public void handleDeleteAccount(ActionEvent actionEvent) throws IOException {
        navigate("delete_confirm.fxml");
    }

    public void cancelHandle(ActionEvent actionEvent) {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();

    }
    public void navigate(String path) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
        Parent root = loader.load();
        ChangePasswordController controller = loader.getController();
        controller.setUser(user);
        controller.setStage(stage);
        Scene scene = new Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        stage.setScene(scene);
        stage.show();
    }
    public void handleUploadPicture(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh đại diện");
        
        // Chỉ cho phép chọn các định dạng file ảnh
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        // Mở hộp thoại chọn file
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile == null) {
            return;
        }

        try {
            // Lấy đường dẫn file và tạo đối tượng Image
            Image image = new Image(selectedFile.toURI().toString());
            avatarCircle.setFill(new ImagePattern(image));

            // Đọc file thành mảng byte
            byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
            // Mã hóa thành chuỗi văn bản Base64
            String base64Image = Base64.getEncoder().encodeToString(fileContent);

            // Gửi yêu cầu lên Server (giống lúc đổi mật khẩu)
            RequestHandler.getInstance().requestUpdateAvatar(user.getId(), base64Image);

            // Cập nhật avatar local để hiển thị lại khi cần
            user.setAvatar(base64Image);

            System.out.println("Đã gửi ảnh lên Server thành công!");
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file ảnh: " + e.getMessage());
        }
    }
}
