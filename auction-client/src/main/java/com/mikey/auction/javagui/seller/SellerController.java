package com.mikey.auction.javagui.seller;
import java.sql.*;
import java.util.HashMap;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Observable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.IOException;
import java.util.ResourceBundle;

import com.mikey.auction.database.ArtsDAO;
import com.mikey.auction.user.User;

import javafx.stage.FileChooser;
import java.io.File;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class SellerController {
    @FXML
    private ComboBox<String> type;
    @FXML
    private VBox itemInfo;

    @FXML private ImageView preview1;
    @FXML private ImageView preview2;
    @FXML private ImageView preview3;
    @FXML private ImageView preview4;
    @FXML private ImageView preview5;

    @FXML private TextField artist;
    @FXML private TextField yearOfcreation;
    @FXML private TextField dimensions;
    @FXML private TextField medium;

    @FXML
    private Button submit;
    private User user;

    private List<ImageView> previewList;

    @FXML
    public void initialize() {
        previewList = List.of(preview1, preview2, preview3, preview4, preview5);
        type.setValue("Arts");
    }
    public void setUser(User user) {
        this.user = user;
    }   
    public void loadSellerAuctions() { 
        
    }
    @FXML
    public void changeItemInfo() {
        String selected = type.getValue();
        if (selected == null) return;
        String fxmlPath = null;

        if (selected.equals("Arts")) {
            fxmlPath = "/javagui/seller/Arts.fxml";
        } else if (selected.equals("Electronics")) {
            fxmlPath = "/javagui/seller/Electronics.fxml";
        } else if (selected.equals("Vehicles")) {
            fxmlPath = "/javagui/seller/Vehicles.fxml";
        }

        if (fxmlPath == null) return;

        try {
            Parent content = FXMLLoader.load(getClass().getResource(fxmlPath));
            itemInfo.getChildren().setAll(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private VBox upload;

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();

        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        List<File> files = fileChooser.showOpenMultipleDialog(
            preview1.getScene().getWindow()
        );

        if (files == null) return;

        for (File file : files) {

            Image image = new Image(file.toURI().toString());

            for (ImageView view : previewList) {
                if (view.getImage() == null) {
                    view.setImage(image);
                    view.setPreserveRatio(true);
                    break;
                }
            }
        }

        for (ImageView view : previewList) {
            view.setOnMouseClicked(e -> view.setImage(null));
        }
    }

    @FXML
    private int handleSubmit(ActionEvent e) {
        if (type.getValue() == null) return -1;
        String itemType = type.getValue();
        if (itemType.equals("Arts")) {
            try {
                Connection conn = ArtsDAO.getInstance().getConnect();
                String sql = "INSERT INTO arts (itemId, artist, yearOfcreation, dimensions, medium) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pr;
                try {
                    pr = conn.prepareStatement(sql);
                    pr.setInt(1, 11);
                    pr.setString(2, artist.getText());
                    pr.setString(3, yearOfcreation.getText());
                    pr.setString(4, dimensions.getText());
                    pr.setString(5, medium.getText());
                    pr.executeUpdate();
                    int res = pr.executeUpdate();
                    return res;
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            finally {
                return 1;
            }
        }
        return 1;
    }
}