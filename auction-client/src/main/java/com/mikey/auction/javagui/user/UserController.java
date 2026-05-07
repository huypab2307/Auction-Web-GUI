package com.mikey.auction.javagui.user;

import com.mikey.auction.user.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("change_Password.fxml"));
        Parent root = loader.load();
        ChangePasswordController controller = loader.getController();
        controller.setUser(user);
        controller.setStage(stage);
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void handleDeleteAccount(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("delete_confirm.fxml"));
        Parent root = loader.load();
        DeleteConfirmController controller = loader.getController();
        controller.setUser(user);
        controller.setStage(stage);
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void cancelHandle(ActionEvent actionEvent) {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();


    }
}
