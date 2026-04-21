package JavaGui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Vì main.fxml nằm cùng package JavaGui với file này nên để "main.fxml" là chuẩn nhất
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("main.fxml")));

        Scene scene = new Scene(root, 400, 500);

        primaryStage.setTitle("Hệ Thống Đấu Giá Trực Tuyến - Đăng Nhập");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); 
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}