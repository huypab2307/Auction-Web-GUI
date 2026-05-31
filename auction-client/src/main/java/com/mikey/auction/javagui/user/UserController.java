package com.mikey.auction.javagui.user;

import java.io.File;
import java.io.IOException;

import com.mikey.auction.user.User;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
        // Đợi giao diện load xong xuôi vào Scene rồi mới gắn sự kiện phím tắt
        Platform.runLater(() -> {
            // Dùng usernameField để lấy Scene cho an toàn vì biến này đã được khai báo
            if (usernameField != null && usernameField.getScene() != null) {
                usernameField.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    
                    // Xử lý khi nhấn ESCAPE -> Thoát không lưu
                    if (event.getCode() == KeyCode.ESCAPE) {
                        cancelHandle(null); 
                        event.consume(); // Ngăn sự kiện phím lan ra
                    } 
                    // Xử lý khi nhấn ENTER -> Thực hiện Lưu thay đổi và Thoát
                    else if (event.getCode() == KeyCode.ENTER) {
                        handleSave(null); 
                        event.consume(); // Ngăn sự kiện phím lan ra
                    }
                    
                });
            }
        });
    }

    public void setUser(User user) {
        this.user = user;
        
        if (usernameField != null && user != null) {
            usernameField.setText(user.getUsername());
        }
        
        if (user != null && user.getUsername() != null && avatarCircle != null) {
            // Thêm ?t= + System.currentTimeMillis() để phá cache của JavaFX
            String avatarUrl = "https://res.cloudinary.com/devnd8ndw/image/upload/auction_avatars/avatar_" 
                                + user.getUsername() + ".jpg?t=" + System.currentTimeMillis();
            
            try {
                // Tải ảnh bất đồng bộ (true) - Không chặn luồng UI chính
                Image image = new Image(avatarUrl, true);
                
                // 1. Theo dõi tiến trình tải ảnh (Khi hoàn thành thì progress = 1.0)
                image.progressProperty().addListener((observable, oldProgress, newProgress) -> {
                    if (newProgress.doubleValue() == 1.0 && !image.isError()) {
                        // ĐÃ TẢI XONG THÀNH CÔNG: Chuyển về luồng UI chính để cập nhật giao diện
                        javafx.application.Platform.runLater(() -> {
                            try {
                                avatarCircle.setFill(new javafx.scene.paint.ImagePattern(image));
                            } catch (Exception ex) {
                                System.err.println("Lỗi khi đổ ảnh vào vòng tròn: " + ex.getMessage());
                            }
                        });
                    }
                });
                
                // 2. Theo dõi nếu xảy ra lỗi (Ví dụ: 404 không tìm thấy ảnh, mất mạng...)
                image.errorProperty().addListener((observable, oldHasError, newHasError) -> {
                    if (newHasError) {
                        System.out.println("User '" + user.getUsername() + "' chưa có avatar trên Cloudinary hoặc tải lỗi. Giữ ảnh mặc định.");
                    }
                });
                
            } catch (Exception e) {
                System.err.println("Lỗi khởi tạo tiến trình Image: " + e.getMessage());
            }
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void handleChangePassword(ActionEvent actionEvent) throws IOException {
        navigate("change_password.fxml");
    }

    // THÊM MỚI: Hàm xử lý lưu tên (Username)
    public void handleSave(ActionEvent actionEvent) {
        if (usernameField != null && user != null) {
            String newUsername = usernameField.getText().trim();
            
            // Chỉ lưu nếu tên không bị bỏ trống và khác với tên cũ
            if (!newUsername.isEmpty() && !newUsername.equals(user.getUsername())) {
                System.out.println("Đang gửi yêu cầu lưu tên mới: " + newUsername);
                
                // TODO: BẠN HÃY GỌI REQUEST TỚI SERVER TẠI ĐÂY
                // Ví dụ: com.mikey.auction.socket.RequestHandler.getInstance().requestUpdateUser(user.getId(), newUsername);
            }
        }
        
        // Lưu xong thì tự động đóng cửa sổ
        cancelHandle(null);
    }

    public void cancelHandle(ActionEvent actionEvent) {
        if (usernameField != null && usernameField.getScene() != null) {
            Stage currentStage = (Stage) usernameField.getScene().getWindow();
            if (currentStage != null) {
                currentStage.close();
            }
        }
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
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        javafx.stage.Stage currentStage = (javafx.stage.Stage) ((javafx.scene.Node) actionEvent.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(currentStage);

        if (selectedFile == null) {
            return;
        }

        try {
            // 1. Gọi hàm upload mới (bản có 2 tham số) để đặt tên file theo tên tài khoản của User
            String cloudinaryUrl = com.mikey.auction.cloudinary.CloudinaryService.upload(selectedFile, user.getUsername());
            
            if (cloudinaryUrl == null || cloudinaryUrl.trim().isEmpty()) {
                System.err.println("Upload ảnh lên Cloudinary thất bại!");
                return;
            }

            // 2. Hiển thị ảnh local lên vòng tròn ngay lập tức để người dùng thấy luôn
            Image localImage = new Image(selectedFile.toURI().toString());
            avatarCircle.setFill(new ImagePattern(localImage));

            // 3. Gọi thanh TopBar ở màn hình chính tự động nạp lại ảnh mới
            if (com.mikey.auction.javagui.topbar.TopBarController.getInstance() != null) {
                com.mikey.auction.javagui.topbar.TopBarController.getInstance().updateAvatarImmediately(localImage);
            }

            System.out.println("Đã upload lên Cloudinary thành công với tên cố định theo username!");
        } catch (Exception e) {
            System.err.println("Lỗi khi xử lý ảnh: " + e.getMessage());
            e.printStackTrace();
        }
    }
}