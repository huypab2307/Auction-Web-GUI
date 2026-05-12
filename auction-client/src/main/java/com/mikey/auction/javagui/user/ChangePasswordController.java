package com.mikey.auction.javagui.user;

import com.mikey.auction.database.UserDAO;
import com.mikey.auction.javagui.SceneChanger;
import com.mikey.auction.user.User;
import javafx.event.ActionEvent;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class ChangePasswordController  {

    public PasswordField oldPasswordField;
    public PasswordField newPasswordField;
    public User user;
    public Stage stage;
    public void setUser(User user) {
        this.user = user;
    }
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void handleCancel(ActionEvent actionEvent) {

        SceneChanger.getInstance().openSettings(stage, user);
    }

    public void handleUpdatePassword(ActionEvent actionEvent) {
        UserDAO userController = UserDAO.getInstance();
        if ( userController.checkPassword( user.getId(), oldPasswordField.getText())){
            userController.changePassword(user.getId(), newPasswordField.getText());
        }else{

        }
    }
}
