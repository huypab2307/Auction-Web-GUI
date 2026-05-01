package JavaGui.Login;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.InputStreamReader;
import com.google.gson.Gson;
import Database.UserDAO;
// import JavaGui.Auction.AuctionController;
import JavaGui.SceneChanger;
import User.User;
import User.Admin;
import User.Bidder;
import User.Seller;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private Button loginButton;
    @FXML
    private Label status;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Gson gson = new Gson();


    public void initialize(){
        loginButton.setDisable(true); // Vô hiệu hóa nút login ban đầu
        new Thread(() -> {
            while (true) {  // Vòng lặp retry liên tục cho đến khi kết nối thành công
                try {
                    socket = new Socket("localhost", 12345);
                    out = new PrintWriter(socket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    
                    // Khi kết nối thành công mới mở nút login
                    Platform.runLater(() -> {
                        status.setText("Đã kết nối server");
                        loginButton.setDisable(false);
                    });
                    break;  // Thoát vòng lặp khi kết nối thành công
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        status.setText("Không thể kết nối Server! Đang thử lại...");
                        loginButton.setDisable(true);
                    });
                    try {
                        Thread.sleep(5000);  // Chờ 5 giây trước khi retry
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }).start();
    }
    @FXML
    public void onHandleLogin(ActionEvent e){
        String text1 = username.getText();
        String text2 = password.getText();

        if (socket == null || socket.isClosed() || out == null || in == null) {
            status.setText("Chưa kết nối server hoặc kết nối đã mất.");
            loginButton.setDisable(true);
            return;
        }
        
        // Đã xóa dấu cách thừa sau dấu | để chuẩn định dạng với Server
        out.println("LOGIN|" + text1 + "|" + text2); 

        new Thread(() -> {
            try {
                // Đợi phản hồi từ server
                String response = in.readLine(); 

                if ("SUCCESS".equals(response)) {
                    // Đọc tiếp dòng JSON
                    String userJson = in.readLine();
                    
                    // --- BẮT ĐẦU ĐOẠN XỬ LÝ GSON MỚI ---
                    com.google.gson.JsonObject jsonObject = com.google.gson.JsonParser.parseString(userJson).getAsJsonObject();
                    String roleStr = jsonObject.get("role").getAsString();

                    User user = null;
                    if ("ADMIN".equals(roleStr)) {
                        user = gson.fromJson(userJson, Admin.class); 
                    } else if ("SELLER".equals(roleStr)) {
                        user = gson.fromJson(userJson, Seller.class);
                    } else if ("BIDDER".equals(roleStr)) {
                        user = gson.fromJson(userJson, Bidder.class);
                    }
                    // --- KẾT THÚC ĐOẠN XỬ LÝ GSON ---

                    // Ép sang biến final để truyền an toàn vào Platform.runLater
                    final User finalUser = user; 

                    // QUAN TRỌNG: Cập nhật UI phải bọc trong Platform.runLater
                    Platform.runLater(() -> {
                        try {
                            if (finalUser != null) {
                                SceneChanger.getInstance().toMainMenu(finalUser);
                            }
                        } catch (Exception ex) {
                            System.out.println("Lỗi chuyển cảnh: " + ex.getMessage());
                        }
                    });

                } else {
                    // Xử lý đoạn ELSE (Đăng nhập thất bại)
                    Platform.runLater(() -> {
                        // Mở lại nút login nếu trước đó bạn đã disable nó khi bấm
                        loginButton.setDisable(false); 
                        
                        username.clear();
                        password.clear();
                        status.setText("Sai tài khoản hoặc mật khẩu!");
                        
                        String errorStyle = "-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: red";
                        username.setStyle(errorStyle);
                        password.setStyle(errorStyle);
                        status.setTextFill(javafx.scene.paint.Color.RED); 
                    });
                }
            } catch (Exception ex) { // Dùng Exception để bắt cả lỗi IOException lẫn lỗi JSON nếu có
                Platform.runLater(() -> status.setText("Lỗi kết nối hoặc xử lý dữ liệu!"));
                ex.printStackTrace();
            }
        }).start();
    }
    public void onKeyReleased(){
        String text1 = username.getText();
        String text2 = password.getText();
        boolean disable1 = text1.isEmpty() || text1.trim().isEmpty();
        boolean disable2 = text2.trim().isEmpty() || text2.isEmpty();
        if (!disable1 || !disable2){ 
            username.setStyle("-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: gray");
            password.setStyle("-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: gray");
        }
        loginButton.setDisable(disable1 || disable2);
    }
    public void onRegisterHandle() throws IOException {
        Parent root = FXMLLoader.load((getClass().getResource("register.fxml")));
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
