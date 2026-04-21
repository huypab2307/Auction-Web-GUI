package JavaGui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class AuctionController {

    @FXML
    private VBox itemContainer;

    @FXML
    public void handleLogout(ActionEvent event) throws IOException {
        // Chuyển về trang đăng nhập
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("main.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 400, 500);
        stage.setScene(scene);
        stage.show();
    }
}