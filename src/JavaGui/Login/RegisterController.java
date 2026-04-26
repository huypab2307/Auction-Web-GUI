package JavaGui.Login;

import Database.UserDAO;
// import JavaGui.Auction.AuctionController;
import User.User;
import javafx.animation.PauseTransition;
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
import javafx.util.Duration;

import java.io.IOException;


public class RegisterController {
    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private Button registerButton;
    @FXML
    private Label status;

    public void initialize(){
        registerButton.setDisable(true);
    }
    @FXML
    public void onHandleRegister(ActionEvent e) throws IOException {
        String text1 = username.getText();
        String text2 = password.getText();
        UserDAO userDAO = UserDAO.getInstance();
        boolean success = userDAO.register(text1,text2);
        if (success){
            status.setText("đăng ký thành công!!");
            status.setTextFill(Paint.valueOf("green"));
            username.setStyle("-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: green");
            password.setStyle("-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: green");
            PauseTransition pause = new PauseTransition(Duration.seconds(4));
            pause.setOnFinished(event -> {
                backToLogin(); 
            });
            pause.play();
        }else{
            status.setText("tên đăng nhập tồn tại");
            status.setTextFill(Paint.valueOf("red"));
            username.setStyle("-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: red");
            password.setStyle("-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: red");
            username.clear();
            password.clear();
        }



    }
    @FXML
    public void onKeyReleased(){
        String text1 = username.getText();
        String text2 = password.getText();
        boolean disable1 = text1.isEmpty() || text1.trim().isEmpty();
        boolean disable2 = text2.trim().isEmpty() || text2.isEmpty();
        if (!disable1) username.setStyle("-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: gray");
        if (!disable2) password.setStyle("-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: gray");
        registerButton.setDisable(disable1 || disable2);
    }
    @FXML
    public void backToLogin() {
        try{
            Stage stage = (Stage) registerButton.getScene().getWindow();
            Parent root = FXMLLoader.load((getClass().getResource("Login.fxml")));
            stage.setScene(new Scene(root));
            stage.show();
        }catch(IOException e){
            System.out.println(e.getMessage());
        }
    }


}
