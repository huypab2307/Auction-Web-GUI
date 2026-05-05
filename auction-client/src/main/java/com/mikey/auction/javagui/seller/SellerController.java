package com.mikey.auction.javagui.seller;
import java.sql.*;
import java.util.ArrayList;
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
import javafx.scene.control.DatePicker;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.IOException;
import java.util.ResourceBundle;

import com.mikey.auction.auction.Auction;
import com.mikey.auction.database.ArtsDAO;
import com.mikey.auction.user.User;
import com.mysql.cj.xdevapi.InsertStatement;
import com.zaxxer.hikari.util.ClockSource.Factory;
import com.mikey.auction.javagui.SceneChanger;
import com.mikey.auction.javagui.topbar.TopBarController;
import com.mikey.auction.database.ElectronicsDAO;
import com.mikey.auction.database.VehicleDAO;
import com.mikey.auction.manager.AuctionManager;
import com.mikey.auction.manager.ItemManager;
import com.mikey.auction.items.*;

import javafx.stage.FileChooser;
import java.io.File;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;

public class SellerController {
    private List<File> selectedFiles = new ArrayList<>();
    @FXML private ComboBox<String> type;
    @FXML private VBox itemInfo;
    @FXML private TextField itemName;
    @FXML private TextArea itemDescription;
    @FXML private TextField price;
    @FXML private TextField stepPrice;
    @FXML private DatePicker startTime;
    @FXML private DatePicker endTime;
    @FXML private TextField finalPrice;
    @FXML private TopBarController topBarController;
    @FXML private ImageView congratulation;


    @FXML private ImageView preview1;
    @FXML private ImageView preview2;
    @FXML private ImageView preview3;
    @FXML private ImageView preview4;
    @FXML private ImageView preview5;

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
        if (topBarController != null) {
            topBarController.setUser(user);
        }
    }   
    public void loadSellerAuctions() { 
        
    }
    @FXML
    public void changeItemInfo() {
        String selected = type.getValue();
        if (selected == null) return;
        String fxmlPath = null;

        if (selected.equals("Arts")) {
            fxmlPath = "Arts.fxml";
        } else if (selected.equals("Electronics")) {
            fxmlPath = "Electronics.fxml";
        } else if (selected.equals("Vehicles")) {
            fxmlPath = "Vehicles.fxml";
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

        if (files == null || files.isEmpty()) return;
        selectedFiles = files;

        for (ImageView view : previewList) {
            view.setImage(null);
        }

        int index = 0;
        for (File file : files) {
            if (index >= previewList.size()) break;

            Image image = new Image(file.toURI().toString());
            previewList.get(index).setImage(image);
            previewList.get(index).setPreserveRatio(true);

            index++;
        }

        for (ImageView view : previewList) {
            view.setOnMouseClicked(e -> view.setImage(null));
        }
    }

    @FXML
    private void handleSubmit(ActionEvent e) {
        String name = itemName.getText();
        String description = itemDescription.getText();
        ItemType selected = ItemType.valueOf(type.getValue().toUpperCase());

        String imagePath = null;

        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            imagePath = saveImage(selectedFiles.get(0)); 
        }

        HashMap<String, String> itemData = new HashMap<>();
        itemData.put("type", "ARTS");
        itemData.put("name", name);
        itemData.put("description", description);
        itemData.put("sellerId", String.valueOf(user.getId()));
        itemData.put("imagePath", imagePath);


        if (selected == null) {
            System.out.println("Vui lòng chọn loại sản phẩm.");
            return;
        }
        switch (selected) {
            case ARTS:
                findArtworkData(itemData);
                break;
            case ELECTRONICS:
                findElectronicsData(itemData);
                break;
            case VEHICLE:
                findVehicleData(itemData);
                break;
            default:
                System.out.println("Loại sản phẩm không hợp lệ.");
                return ;
        }
        AuctionManager.getInstance().uploadItem(ItemManager.getInstance().preProcessing(itemData), Double.parseDouble(price.getText()), Double.parseDouble(stepPrice.getText()), startTime.getValue().atStartOfDay(), endTime.getValue().atStartOfDay());

        congratulation.setDisable(false);
        congratulation.setVisible(true);

        PauseTransition pause = new PauseTransition(javafx.util.Duration.seconds(3));
        pause.setOnFinished(event -> { 

            SceneChanger.getInstance().toMainMenu(user);
        });
        pause.play();
        
    }


    private String saveImage(File file) {
        try {
            String folder = "..\\auction-common\\src\\main\\resources\\images\\";
            File dir = new File(folder);

            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = System.currentTimeMillis() + "_" + file.getName();
            File dest = new File(folder + fileName);

            java.nio.file.Files.copy(
                file.toPath(),
                dest.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            return "/images/" + fileName;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void findArtworkData(HashMap<String, String> itemData) {
        TextField artistField = (TextField) itemInfo.lookup("#artist");
        TextField yearField = (TextField) itemInfo.lookup("#yearOfcreation");
        TextField dimensionsField = (TextField) itemInfo.lookup("#dimensions");
        TextField mediumField = (TextField) itemInfo.lookup("#medium");

        String artist = artistField.getText();
        int year = Integer.parseInt(yearField.getText());
        String dim = dimensionsField.getText();
        String med = mediumField.getText();

        itemData.put("artist", artist);
        itemData.put("year", String.valueOf(year));
        itemData.put("dimensions", dim);
        itemData.put("medium", med);
    }
    public void findElectronicsData(HashMap<String, String> itemData) {
        TextField brandField = (TextField) itemInfo.lookup("#brand");
        TextField powerField = (TextField) itemInfo.lookup("#power");
        TextField voltageField = (TextField) itemInfo.lookup("#voltage");
        TextField currentField = (TextField) itemInfo.lookup("#current");
        TextField statusField = (TextField) itemInfo.lookup("#status");
        TextField colorField = (TextField) itemInfo.lookup("#color");
        TextField weightField = (TextField) itemInfo.lookup("#weight");

        String brand = brandField.getText();
        int power = Integer.parseInt(powerField.getText());
        double voltage = Double.parseDouble(voltageField.getText());
        double current = Double.parseDouble(currentField.getText());
        String status = statusField.getText();
        String color = colorField.getText();    
        double weight = Double.parseDouble(weightField.getText());

        itemData.put("brand", brand);
        itemData.put("power", String.valueOf(power));
        itemData.put("voltage", String.valueOf(voltage));
        itemData.put("current", String.valueOf(current));
        itemData.put("status", status);
        itemData.put("color", color);
        itemData.put("weight", String.valueOf(weight));
    }
    
    public void findVehicleData(HashMap<String, String> itemData) {
        TextField mileageField = (TextField) itemInfo.lookup("#mileage");
        TextField mFGField = (TextField) itemInfo.lookup("#mFG");
        TextField brandField = (TextField) itemInfo.lookup("#brand");
        TextField modelField = (TextField) itemInfo.lookup("#model");
        TextField trimField = (TextField) itemInfo.lookup("#trim");
        TextField titleStatusField = (TextField) itemInfo.lookup("#titleStatus");

        double mileage = Double.parseDouble(mileageField.getText());
        int mFG = Integer.parseInt(mFGField.getText());
        String brand = brandField.getText();
        String model = modelField.getText();
        String trim = trimField.getText();
        String titleStatus = titleStatusField.getText();

        itemData.put("mileage", String.valueOf(mileage));
        itemData.put("mFG", String.valueOf(mFG));
        itemData.put("brand", brand);
        itemData.put("model", model);
        itemData.put("trim", trim);
        itemData.put("titleStatus", titleStatus);
    }
}