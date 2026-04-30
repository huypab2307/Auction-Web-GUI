package JavaGui.TopBar;

import Database.AuctionDAO;
import JavaGui.SceneChanger;
import User.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;

import java.io.IOException;
import java.util.ArrayList;

import DTO.AuctionInfo;

public class TopBarController {
    @FXML private TextField searchField;
    @FXML private ToggleButton searchButton;

    private SearchListener listener;
    private User user;

    public void setListener(SearchListener listener) {
        this.listener = listener;
    }

    public void searchHandle() throws IOException {
        String keyword = searchField.getText().toUpperCase();
        ArrayList<AuctionInfo> results = AuctionDAO.getInstance().searchAuction(keyword);

        if (listener != null) {
            listener.onSearchPerformed(results);
        }
    }
    public void initialize(){
        searchButton.setDisable(true);
    }
    @FXML
    public void onKeySearchHandle(){
        String text = searchField.getText();
        boolean disable = text.isEmpty() || text.trim().isEmpty();
        searchButton.setDisable(disable);
    }
    @FXML
    public void logoutHandle(){
        SceneChanger.getInstance().toLogin();
    }
    public void setUser(User user){
        this.user = user;
    }
    public void toHubHandle(ActionEvent actionEvent) {
        if (user != null) {
            SceneChanger.getInstance().toMainMenu(user);
        } else {
            // no user info available: fallback to login
            SceneChanger.getInstance().toLogin();
        }
    }

    @FXML
    public void userGuiHandle(ActionEvent actionEvent) {
        SceneChanger.getInstance().toUserGui(user);
    }
}
