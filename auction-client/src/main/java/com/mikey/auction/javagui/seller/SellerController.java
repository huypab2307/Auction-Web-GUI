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
import javafx.scene.text.Text;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.IOException;
import java.util.ResourceBundle;

import com.mikey.auction.database.ArtsDAO;
import com.mikey.auction.items.Arts;
import com.mikey.auction.items.Electronics;
import com.mikey.auction.items.ItemType;
import com.mikey.auction.user.User;
import com.mikey.auction.javagui.SceneChanger;
import com.mikey.auction.database.ElectronicsDAO;
import com.mikey.auction.database.VehicleDAO;
import com.mikey.auction.items.Vehicle;

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
    @FXML private ComboBox<String> type;
    @FXML private VBox itemInfo;
    @FXML private TextField itemName;
    @FXML private TextArea itemDescription;

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
        String name = itemName.getText();
        String description = itemDescription.getText();
        String selected = type.getValue();
        
        if (selected == null) {
            System.out.println("Vui lòng chọn loại sản phẩm.");
            return -1;
        }

        if (selected.equals("Arts")) {
            String imagePath = "/auction-common/src/main/resources/images/earth.png";
            TextField artistField = (TextField) itemInfo.lookup("#artist");
            TextField yearField = (TextField) itemInfo.lookup("#yearOfcreation");
            TextField dimensionsField = (TextField) itemInfo.lookup("#dimensions");
            TextField mediumField = (TextField) itemInfo.lookup("#medium");

            String artistStr = artistField.getText();
            int year = Integer.parseInt(yearField.getText());
            String dim = dimensionsField.getText();
            String med = mediumField.getText();

            Arts art = new Arts(name, description, ItemType.ARTS, user.getId(), -1, imagePath);
            art.setArts(artistStr, year, dim, med);

            ArtsDAO artsDAO = ArtsDAO.getInstance();
            try (Connection conn = artsDAO.getConnect()) {
                int itemId = artsDAO.createItem(conn, art);
                if (itemId != -1) {
                    conn.commit();
                    System.out.println("Tạo tác phẩm 1 thành công!");
                    return itemId;
                }
            } catch (SQLException ex) {
                System.out.println("Lỗi khi tạo tác phẩm: " + ex.getMessage());
                ex.printStackTrace();
            }

        } else if (selected.equals("Electronics")) {
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
            String imagePath = "/auction-common/src/main/resources/images/earth.png";
            Electronics electronic = new Electronics(name, description, ItemType.ELECTRONICS, user.getId(), -1, imagePath);
            electronic.setElectronics(brand, power, voltage, current, status, color, weight);

            ElectronicsDAO electronicsDAO = ElectronicsDAO.getInstance();
            try (Connection conn = electronicsDAO.getConnect()) {
                int itemId = electronicsDAO.createItem(conn, electronic);
                if (itemId != -1) {
                    System.out.println("Tạo sản phẩm điện tử thành công!");
                    return itemId;
                }
            } catch (SQLException ex) {
                System.out.println("Lỗi khi tạo sản phẩm điện tử: " + ex.getMessage());
                ex.printStackTrace();
            }

        } else if (selected.equals("Vehicles")) {
            String imagePath = "/auction-common/src/main/resources/images/earth.png";
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

            Vehicle vehicle = new Vehicle(name, description, ItemType.VEHICLE, user.getId(), -1, imagePath);
            vehicle.setVehicle(mileage, mFG, brand, model, trim, titleStatus);

            VehicleDAO vehicleDAO = VehicleDAO.getInstance();
            try (Connection conn = vehicleDAO.getConnect()) {
                int itemId = vehicleDAO.createItem(conn, vehicle);
                if (itemId != -1) {
                    System.out.println("Tạo phương tiện thành công!");
                    return itemId;
                }
            } catch (SQLException ex) {
                System.out.println("Lỗi khi tạo phương tiện: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
        
        return -1;
    }
}