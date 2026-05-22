package com.mikey.auction.javagui.user;

import java.io.IOException;

import com.mikey.auction.user.User;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class UserController {
    public TextField usernameField;
    private User user;
    private Stage stage;

    public void initialize() {
    }

    public void setUser(User user) {
        this.user = user;
        usernameField.setText(user.getUsername());
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
}
