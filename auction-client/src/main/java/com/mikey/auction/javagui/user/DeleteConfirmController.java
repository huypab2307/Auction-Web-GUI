package com.mikey.auction.javagui.user;

import com.mikey.auction.javagui.SceneChanger;
import com.mikey.auction.user.User;
import javafx.event.ActionEvent;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class DeleteConfirmController {

    public PasswordField passwordConfirmField;
    public User user;
    public Stage stage;
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public void handleCancel(ActionEvent actionEvent) {
        SceneChanger.getInstance().openSettings(stage, user);
    }

    public void handleConfirmDelete(ActionEvent actionEvent) {

    }
}
