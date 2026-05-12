package com.mikey.auction.javagui.seller;
import java.util.ArrayList;
import java.util.HashMap;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;

import com.mikey.auction.cloudinary.CloudinaryService;
import com.mikey.auction.dto.AuctionInfo;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.animation.PauseTransition;
import com.mikey.auction.user.User;
import com.mikey.auction.auction.Auction;
import com.mikey.auction.auction.AuctionStatus;
import com.mikey.auction.javagui.SceneChanger;
import com.mikey.auction.javagui.topbar.TopBarController;
import com.mikey.auction.manager.AuctionManager;
import com.mikey.auction.manager.ItemManager;
import com.mikey.auction.socket.RequestHandler;

import javafx.stage.FileChooser;
import java.io.File;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TextArea;
import javafx.util.Duration;

public class SellerController {
    @FXML private StackPane mainStackPane;
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
    private Label errorArtist, errorYear, errorDimensions, errorMedium;

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
        } else if (selected.equals("Vehicle")) {
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
        checkItemName();
        checkPrice();
        checkStepPrice();
        checkFinalPrice();
        checkDates();

        boolean hasError = !errorItemName.getText().isEmpty() ||
                !errorPrice.getText().isEmpty() ||
                !errorStepPrice.getText().isEmpty() ||
                !errorFinalPrice.getText().isEmpty() ||
                !errorStartTime.getText().isEmpty() ||
                !errorEndTime.getText().isEmpty();

        String selected = type.getValue();
        if (selected == null) {
            System.out.println("Chưa chọn danh mục!");
            return;
        }

        if (selected.equals("Arts")) {
            checkArtist(); checkYearOfCreation(); checkDimensions(); checkMedium();
            if (hasDynamicError(new String[]{"#errorArtist", "#errorYear", "#errorDimensions", "#errorMedium"})) hasError = true;
        } else if (selected.equals("Electronics")) {
            checkEBrand(); checkEPower(); checkEVoltage(); checkECurrent(); checkEStatus(); checkEColor(); checkEWeight();
            if (hasDynamicError(new String[]{"#errorEBrand", "#errorEPower", "#errorEVoltage", "#errorECurrent", "#errorEStatus", "#errorEColor", "#errorEWeight"})) hasError = true;
        } else if (selected.equals("Vehicle")) {
            checkVMileage(); checkVMFG(); checkVBrand(); checkVModel(); checkVTrim(); checkVTitleStatus();
            if (hasDynamicError(new String[]{"#errorVMileage", "#errorVMFG", "#errorVBrand", "#errorVModel", "#errorVTrim", "#errorVTitleStatus"})) hasError = true;
        }

        if (hasError) {
            System.out.println("Form còn lỗi, Dương kiểm tra lại nhé!");
            return;
        }

        AuctionInfo newAuction = new AuctionInfo(null, currentEditingId, selected, selected, currentEditingId, null, null, null, currentEditingId);
        RequestHandler.getInstance().requestCreateAuction(newAuction);
        submit.setDisable(true);
        System.out.println("Đang xử lý dữ liệu và đẩy ảnh lên Cloudinary...");

        String imagePath = "https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg";

        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            String cloudUrl = CloudinaryService.upload(selectedFiles.get(0));
            if (cloudUrl != null) {
                imagePath = cloudUrl.replace("/upload/", "/upload/ar_16:9,c_fill,w_1000,g_auto/");
                System.out.println("Link đã được 'phẫu thuật' thẩm mỹ: " + imagePath);
            }
        }
        HashMap<String, String> itemData = new HashMap<>();
        itemData.put("type", selected);
        itemData.put("name", itemName.getText());
        itemData.put("description", itemDescription.getText());
        itemData.put("sellerId", String.valueOf(user.getId()));
        itemData.put("imagePath", imagePath);

        switch (selected) {
            case "Arts" -> findArtworkData(itemData);
            case "Electronics" -> findElectronicsData(itemData);
            case "Vehicle" -> findVehicleData(itemData);
        }

        try {
            double startPrice = Double.parseDouble(price.getText());
            double step = Double.parseDouble(stepPrice.getText());
            var startT = startTime.getValue().atStartOfDay();
            var endT = endTime.getValue().atStartOfDay();

            AuctionManager.getInstance().uploadItem(
                    ItemManager.getInstance().preProcessing(itemData),
                    startPrice,
                    step,
                    startT,
                    endT
            );

            showCongratulationEffect(2.5);
            System.out.println("Đăng đấu giá thành công rực rỡ!");

            if (currentEditingId != -1) {
                System.out.println("Updating ID: " + currentEditingId);
            } else {
                System.out.println("Creating new auction");
            }

        } catch (Exception ex) {
            System.err.println("Lỗi khi gửi dữ liệu lên Server: " + ex.getMessage());
        } finally {
            submit.setDisable(false);
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
    private void showCongratulationEffect(double seconds) {
        ImageView animImg = new ImageView();
        try {
            URL imgUrl = getClass().getResource("/images/congratulation.gif");
            if (imgUrl != null) {
                animImg.setImage(new Image(imgUrl.toExternalForm()));
            }
        } catch (Exception e) {
            System.err.println("Không tìm thấy ảnh GIF chúc mừng!");
            return;
        }

        animImg.setFitWidth(900);
        animImg.setPreserveRatio(true);
        animImg.setMouseTransparent(true);

        mainStackPane.getChildren().add(animImg);

        PauseTransition cleanup = new PauseTransition(Duration.seconds(seconds));
        cleanup.setOnFinished(event -> {
            mainStackPane.getChildren().remove(animImg);
        });
        cleanup.play();
    }

    @FXML
    private Label errorItemName, errorPrice, errorStepPrice, errorFinalPrice, errorStartTime, errorEndTime;

private void setTextFieldError(TextField field, Label label, String message) {
        if (!field.getStyleClass().contains("input-error")) {
            field.getStyleClass().add("input-error");
        }
        if (!label.getStyleClass().contains("label-error")) {
            label.getStyleClass().add("label-error");
        }
        label.setText(message);
    }

    private void clearTextFieldError(TextField field, Label label) {
        field.getStyleClass().remove("input-error");
        label.getStyleClass().remove("label-error");
        label.setText("");
    }

    private void setDatePickerError(DatePicker field, Label label, String message) {
        if (!field.getStyleClass().contains("input-error")) {
            field.getStyleClass().add("input-error");
        }
        if (!label.getStyleClass().contains("label-error")) {
            label.getStyleClass().add("label-error");
        }
        label.setText(message);
    }

    private void clearDatePickerError(DatePicker field, Label label) {
        field.getStyleClass().remove("input-error");
        label.getStyleClass().remove("label-error");
        label.setText("");
    }

    private void hideLabels(String[] ids) {
        for (String id : ids) {
            Label lbl = (Label) itemInfo.lookup(id);
            if (lbl != null) {
                lbl.setText("");
                lbl.getStyleClass().remove("label-error");
            }
        }
    }

    private boolean hasDynamicError(String[] ids) {
        for (String id : ids) {
            Label lbl = (Label) itemInfo.lookup(id);
            if (lbl != null) {
                if (!lbl.getText().isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void clearDynamicLabels(String selected) {
        if (selected.equals("Arts")) {
            String[] ids = {"#errorArtist", "#errorYear", "#errorDimensions", "#errorMedium"};
            hideLabels(ids);
        } else if (selected.equals("Electronics")) {
            String[] ids = {"#errorEBrand", "#errorEPower", "#errorEVoltage", "#errorECurrent", "#errorEStatus", "#errorEColor", "#errorEWeight"};
            hideLabels(ids);
        } else if (selected.equals("Vehicle")) {
            String[] ids = {"#errorVMileage", "#errorVMFG", "#errorVBrand", "#errorVModel", "#errorVTrim", "#errorVTitleStatus"};
            hideLabels(ids);
        }
    }

    @FXML
    public void checkItemName() {
        String text = itemName.getText();
        if (text.isEmpty()) {
            setTextFieldError(itemName, errorItemName, "Không được bỏ trống!");
        } else if (text.length() < 5) {
            setTextFieldError(itemName, errorItemName, "Tên phải từ 5 kí tự trở lên!");
        } else {
            clearTextFieldError(itemName, errorItemName);
        }
    }

    @FXML
    public void checkPrice() {
        String text = price.getText();
        if (text.isEmpty()) {
            setTextFieldError(price, errorPrice, "Không được bỏ trống!");
            return;
        }

        try {
            double p = Double.parseDouble(text);
            if (p <= 0) {
                setTextFieldError(price, errorPrice, "Giá phải lớn hơn 0!");
            } else {
                clearTextFieldError(price, errorPrice);
            }
        } catch (NumberFormatException e) {
            setTextFieldError(price, errorPrice, "Chỉ được nhập số!");
        }
    }

    @FXML
    public void checkStepPrice() {
        String text = stepPrice.getText();
        if (text.isEmpty()) {
            setTextFieldError(stepPrice, errorStepPrice, "Không được bỏ trống!");
            return;
        }

        try {
            double s = Double.parseDouble(text);
            if (s <= 0) {
                setTextFieldError(stepPrice, errorStepPrice, "Bước giá phải lớn hơn 0!");
            } else {
                clearTextFieldError(stepPrice, errorStepPrice);
            }
        } catch (NumberFormatException e) {
            setTextFieldError(stepPrice, errorStepPrice, "Chỉ được nhập số!");
        }
    }

    @FXML
    public void checkFinalPrice() {
        String text = finalPrice.getText();
        if (text.isEmpty()) {
            setTextFieldError(finalPrice, errorFinalPrice, "Không được bỏ trống!");
            return;
        }

        try {
            double f = Double.parseDouble(text);
            double startP = 0;

            if (!price.getText().isEmpty()) {
                try {
                    startP = Double.parseDouble(price.getText());
                } catch (Exception e) {
                    startP = 0;
                }
            }

            if (f <= 0) {
                setTextFieldError(finalPrice, errorFinalPrice, "Giá cuối phải lớn hơn 0!");
            } else if (startP > 0) {
                if (f <= startP) {
                    setTextFieldError(finalPrice, errorFinalPrice, "Giá cuối phải lớn hơn giá khởi điểm!");
                } else {
                    clearTextFieldError(finalPrice, errorFinalPrice);
                }
            } else {
                clearTextFieldError(finalPrice, errorFinalPrice);
            }
        } catch (NumberFormatException e) {
            setTextFieldError(finalPrice, errorFinalPrice, "Chỉ được nhập số!");
        }
    }

    @FXML
    public void checkDates() {
        boolean hasEmptyDate = false;

        if (startTime.getValue() == null) {
            setDatePickerError(startTime, errorStartTime, "Chưa chọn ngày bắt đầu!");
            hasEmptyDate = true;
        } else {
            clearDatePickerError(startTime, errorStartTime);
        }

        if (endTime.getValue() == null) {
            setDatePickerError(endTime, errorEndTime, "Chưa chọn ngày kết thúc!");
            hasEmptyDate = true;
        } else {
            clearDatePickerError(endTime, errorEndTime);
        }

        if (!hasEmptyDate) {
            if (startTime.getValue().isBefore(LocalDate.now())) {
                setDatePickerError(startTime, errorStartTime, "Ngày bắt đầu phải từ ngày hôm nay!");
            }

            if (endTime.getValue().isBefore(startTime.getValue())) {
                setDatePickerError(endTime, errorEndTime, "Ngày kết thúc phải sau ngày bắt đầu!");
            }
        }
    }

    public void checkArtist() {
        TextField field = (TextField) itemInfo.lookup("#artist");
        Label error = (Label) itemInfo.lookup("#errorArtist");
        if (field == null || error == null) return;

        if (field.getText().isEmpty()) {
            setTextFieldError(field, error, "Không được bỏ trống!");
        } else {
            clearTextFieldError(field, error);
        }
    }

    public void checkYearOfCreation() {
        TextField field = (TextField) itemInfo.lookup("#yearOfcreation");
        Label error = (Label) itemInfo.lookup("#errorYear");
        if (field == null || error == null) return;

        String text = field.getText();
        if (text.isEmpty()) {
            setTextFieldError(field, error, "Không được bỏ trống!");
            return;
        }

        try {
            int val = Integer.parseInt(text);
            int currentYear = java.time.Year.now().getValue();

            if (val <= 0) {
                setTextFieldError(field, error, "Năm không hợp lệ!");
            } else if (val > currentYear) {
                setTextFieldError(field, error, "Năm không hợp lệ!");
            } else {
                clearTextFieldError(field, error);
            }
        } catch (NumberFormatException e) {
            setTextFieldError(field, error, "Chỉ được nhập số!");
        }
    }

    public void checkDimensions() {
        TextField field = (TextField) itemInfo.lookup("#dimensions");
        Label error = (Label) itemInfo.lookup("#errorDimensions");
        if (field == null || error == null) return;

        if (field.getText().isEmpty()) {
            setTextFieldError(field, error, "Không được bỏ trống!");
        } else {
            clearTextFieldError(field, error);
        }
    }

    public void checkMedium() {
        TextField field = (TextField) itemInfo.lookup("#medium");
        Label error = (Label) itemInfo.lookup("#errorMedium");
        if (field == null || error == null) return;

        if (field.getText().isEmpty()) {
            setTextFieldError(field, error, "Không được bỏ trống!");
        } else {
            clearTextFieldError(field, error);
        }
    }

    public void checkEBrand() {
        TextField field = (TextField) itemInfo.lookup("#brand");
        Label error = (Label) itemInfo.lookup("#errorEBrand");
        if (field == null || error == null) return;

        if (field.getText().isEmpty()) {
            setTextFieldError(field, error, "Không được bỏ trống!");
        } else {
            clearTextFieldError(field, error);
        }
    }

    public void checkEPower() {
        TextField field = (TextField) itemInfo.lookup("#power");
        Label error = (Label) itemInfo.lookup("#errorEPower");
        if (field == null || error == null) return;

        String text = field.getText();
        if (text.isEmpty()) {
            setTextFieldError(field, error, "Không được bỏ trống!");
            return;
        }

        try {
            double val = Double.parseDouble(text);
            if (val <= 0) {
                setTextFieldError(field, error, "Công suất phải lớn hơn 0!");
            } else {
                clearTextFieldError(field, error);
            }
        } catch (NumberFormatException e) {
            setTextFieldError(field, error, "Chỉ được nhập số!");
        }
    }

    public void checkEVoltage() {
        TextField field = (TextField) itemInfo.lookup("#voltage");
        Label error = (Label) itemInfo.lookup("#errorEVoltage");
        if (field == null || error == null) return;

        String text = field.getText();
        if (text.isEmpty()) {
            setTextFieldError(field, error, "Không được bỏ trống!");
            return;
        }

        try {
            double val = Double.parseDouble(text);
            if (val <= 0) {
                setTextFieldError(field, error, "Điện áp phải lớn hơn 0!");
            } else {
                clearTextFieldError(field, error);
            }
        } catch (NumberFormatException e) {
            setTextFieldError(field, error, "Chỉ được nhập số!");
        }
    }

    public void checkECurrent() {
        TextField field = (TextField) itemInfo.lookup("#current");
        Label error = (Label) itemInfo.lookup("#errorECurrent");
        if (field == null || error == null) return;

        String text = field.getText();
        if (text.isEmpty()) {
            setTextFieldError(field, error, "Không được bỏ trống!");
            return;
        }

        try {
            double val = Double.parseDouble(text);
            if (val <= 0) {
                setTextFieldError(field, error, "Cường độ phải lớn hơn 0!");
            } else {
                clearTextFieldError(field, error);
            }
        } catch (NumberFormatException e) {
            setTextFieldError(field, error, "Chỉ được nhập số!");
        }
    }

    public void checkEStatus() {
        TextField field = (TextField) itemInfo.lookup("#status");
        Label error = (Label) itemInfo.lookup("#errorEStatus");
        if (field == null || error == null) return;

        if (field.getText().isEmpty()) {
            setTextFieldError(field, error, "Không được bỏ trống!");
        } else {
            clearTextFieldError(field, error);
        }
    }

    public void checkEColor() {
        TextField field = (TextField) itemInfo.lookup("#color");
        Label error = (Label) itemInfo.lookup("#errorEColor");
        if (field == null || error == null) return;

        if (field.getText().isEmpty()) {
            setTextFieldError(field, error, "Không được bỏ trống!");
        } else {
            clearTextFieldError(field, error);
        }
    }

    public void checkEWeight() {
        TextField field = (TextField) itemInfo.lookup("#weight");
        Label error = (Label) itemInfo.lookup("#errorEWeight");
        if (field == null || error == null) return;

        String text = field.getText();
        if (text.isEmpty()) {
            setTextFieldError(field, error, "Không được bỏ trống!");
            return;
        }

        try {
            double val = Double.parseDouble(text);
            if (val <= 0) {
                setTextFieldError(field, error, "Trọng lượng phải lớn hơn 0!");
            } else {
                clearTextFieldError(field, error);
            }
        } catch (NumberFormatException e) {
            setTextFieldError(field, error, "Chỉ được nhập số!");
        }
    }

    public void checkVMileage() {
        TextField field = (TextField) itemInfo.lookup("#mileage");
        Label error = (Label) itemInfo.lookup("#errorVMileage");
        if (field == null || error == null) return;

        String text = field.getText();
        if (text.isEmpty()) {
            setTextFieldError(field, error, "Không được bỏ trống!");
            return;
        }

        try {
            double val = Double.parseDouble(text);
            if (val < 0) {
                setTextFieldError(field, error, "Số km không được âm!");
            } else {
                clearTextFieldError(field, error);
            }
        } catch (NumberFormatException e) {
            setTextFieldError(field, error, "Chỉ được nhập số!");
        }
    }

    public void checkVMFG() {
        TextField field = (TextField) itemInfo.lookup("#mFG");
        Label error = (Label) itemInfo.lookup("#errorVMFG");
        if (field == null || error == null) return;

        String text = field.getText();
        if (text.isEmpty()) {
            setTextFieldError(field, error, "Không được bỏ trống!");
            return;
        }

        try {
            int val = Integer.parseInt(text);
            if (val <= 0) {
                setTextFieldError(field, error, "Năm phải lớn hơn 0!");
            } else {
                clearTextFieldError(field, error);
            }
        } catch (NumberFormatException e) {
            setTextFieldError(field, error, "Chỉ được nhập số!");
        }
    }

    public void checkVBrand() {
        TextField field = (TextField) itemInfo.lookup("#brand");
        Label error = (Label) itemInfo.lookup("#errorVBrand");
        if (field == null || error == null) return;

        if (field.getText().isEmpty()) {
            setTextFieldError(field, error, "Không được bỏ trống!");
        } else {
            clearTextFieldError(field, error);
        }
    }

    public void checkVModel() {
        TextField field = (TextField) itemInfo.lookup("#model");
        Label error = (Label) itemInfo.lookup("#errorVModel");
        if (field == null || error == null) return;

        if (field.getText().isEmpty()) {
            setTextFieldError(field, error, "Không được bỏ trống!");
        } else {
            clearTextFieldError(field, error);
        }
    }

    public void checkVTrim() {
        TextField field = (TextField) itemInfo.lookup("#trim");
        Label error = (Label) itemInfo.lookup("#errorVTrim");
        if (field == null || error == null) return;

        if (field.getText().isEmpty()) {
            setTextFieldError(field, error, "Không được bỏ trống!");
        } else {
            clearTextFieldError(field, error);
        }
    }

    public void checkVTitleStatus() {
        TextField field = (TextField) itemInfo.lookup("#titleStatus");
        Label error = (Label) itemInfo.lookup("#errorVTitleStatus");
        if (field == null || error == null) return;

        if (field.getText().isEmpty()) {
            setTextFieldError(field, error, "Không được bỏ trống!");
        } else {
            clearTextFieldError(field, error);
        }
    }

    private int currentEditingId = -1;
    
    public void setEditMode(AuctionInfo info) {
        this.currentEditingId = info.getId();
        submit.setText("Cập nhật sản phẩm");

        // Đổ dữ liệu cũ vào các ô nhập
        itemName.setText(info.getItemInfo().getTitle());
        price.setText(String.valueOf(info.getCurPrice()));
        stepPrice.setText(String.valueOf(info.getBidStep()));
        itemDescription.setText(info.getItemInfo().getDescription());
        type.setValue(info.getItemInfo().getItemType().toString());
        
        // Mặc định là mở khóa tất cả (dành cho PENDING)
        itemName.setDisable(false);
        price.setDisable(false);
        stepPrice.setDisable(false);
        itemDescription.setDisable(false);
        type.setDisable(false);
        // startTimeField.setDisable(false); // Nếu bạn có ô chọn giờ bắt đầu
        
        // KIỂM TRA LOGIC NGHIỆP VỤ
        if (info.getStatus() == AuctionStatus.OPEN) {
            
            // Khóa thời gian bắt đầu vì phiên đã chạy rồi
            // startTimeField.setDisable(true); 

            // Giả sử getIdNgườiBidCuoiCùng() trả về 0 hoặc null nếu chưa có ai bid
            boolean hasBids = (info.getLastBidderName() != null && !info.getLastBidderName().isEmpty()); 

            if (hasBids) {
                // KỊCH BẢN: ĐANG MỞ VÀ ĐÃ CÓ NGƯỜI ĐẤU GIÁ
                // Khóa toàn bộ thông số tài chính và định danh cốt lõi
                itemName.setDisable(true);
                price.setDisable(true);
                stepPrice.setDisable(true);
                type.setDisable(true);
                // endTimeField.setDisable(true); 
                
                Tooltip tooltip = new Tooltip("Đã có người đấu giá, chỉ được phép cập nhật thêm mô tả!");
                price.setTooltip(tooltip);
                submit.setText("Cập nhật mô tả");
                
            } else {
                // KỊCH BẢN: ĐANG MỞ NHƯNG CHƯA AI ĐẤU GIÁ
                // Vẫn cho sửa giá để kích cầu, chỉ hiện nhắc nhở
                Tooltip tooltip = new Tooltip("Chưa có ai đấu giá, bạn vẫn có thể sửa giá khởi điểm.");
                price.setTooltip(tooltip);
            }
        }
    }
}