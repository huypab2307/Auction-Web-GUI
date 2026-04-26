package JavaGui.Login;

import java.io.IOException;

import Database.UserDAO;
// import JavaGui.Auction.AuctionController;
import User.User;
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

    public void initialize(){
        loginButton.setDisable(true);
    }
    @FXML
    public void onHandleLogin(ActionEvent e){
        String text1 = username.getText();
        String text2 = password.getText();
        UserDAO userDAO = UserDAO.getInstance();
        User user = userDAO.login(text1,text2);
        if (user != null){
            // try{
            //     FXMLLoader loader = new FXMLLoader(getClass().getResource("JavaGui/Auction/auction.fxml"));
            //     Parent root = loader.load();
            //     Stage stage = (Stage) loginButton.getScene().getWindow();
            //     AuctionController auctionController= loader.getController();
            //     auctionController.setUser(user);
            //     auctionController.loadAuction();
            // }
            // catch (Exception ex){

            // }
        }else{
            loginButton.setDisable(true);
            username.clear();
            password.clear();
            username.setStyle("-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: red");
            password.setStyle("-fx-background-color: white; -fx-border-radius: 20; -fx-border-color: red");
            status.setTextFill(Paint.valueOf("red"));
        }
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
