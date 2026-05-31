package com.mikey.auction.javagui.seller;
import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mikey.auction.auction.AuctionStatus;
import com.mikey.auction.cloudinary.CloudinaryService;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.dto.ItemSummary;
import com.mikey.auction.items.ItemType;
import com.mikey.auction.javagui.topbar.TopBarController;
import com.mikey.auction.socket.RequestHandler;
import com.mikey.auction.socket.SocketClient;
import com.mikey.auction.socket.SocketListener;
import com.mikey.auction.user.User;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;

public class SellerController implements SocketListener {
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

        // Dùng toUpperCase() để bao trọn mọi trường hợp
        String typeUpper = selected.toUpperCase();
        if (typeUpper.equals("ARTS")) fxmlPath = "Arts.fxml";
        else if (typeUpper.equals("ELECTRONICS")) fxmlPath = "Electronics.fxml";
        else if (typeUpper.equals("VEHICLE")) fxmlPath = "Vehicles.fxml";

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
        checkDates();

        boolean hasError = !errorItemName.getText().isEmpty() ||
                !errorPrice.getText().isEmpty() ||
                !errorStepPrice.getText().isEmpty() ||
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
            System.out.println("Form còn lỗi. Vui lòng kiểm tra lại!");
            return;
        }

        submit.setDisable(true);
        System.out.println("Đang xử lý dữ liệu và đẩy ảnh lên Cloudinary...");

        // Xử lý ảnh (Giữ nguyên logic của bạn)
        String imagePath = "https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg";
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            String cloudUrl = CloudinaryService.upload(selectedFiles.get(0));
            if (cloudUrl != null) {
                imagePath = cloudUrl.replace("/upload/", "/upload/ar_16:9,c_fill,w_1000,g_auto/");
                System.out.println("Link: " + imagePath);
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
        // 1. Lấy dữ liệu thực từ giao diện
        String name = itemName.getText();
        String desc = itemDescription.getText();
        double startPrice = Double.parseDouble(price.getText());
        double step = Double.parseDouble(stepPrice.getText());

        // 👉 ĐÃ RÚT GỌN: Dùng trực tiếp LocalDateTime
        LocalDateTime startT = startTime.getValue().atStartOfDay();
        LocalDateTime endT = endTime.getValue().atStartOfDay();

        String selectedType = type.getValue();

        // 2. Tạo đối tượng ItemSummary (Rút gọn tên class)
        ItemSummary itemSum = new ItemSummary();
        itemSum.setTitle(name);
        itemSum.setDescription(desc);
        itemSum.setImagePath(imagePath);
        itemSum.setItemType(ItemType.valueOf(selectedType.toUpperCase()));

        // 3. Khởi tạo AuctionInfo để gửi đi
        // currentEditingId giúp Server biết đây là lệnh UPDATE cho sản phẩm số 3
        AuctionInfo updateData = new AuctionInfo(
            itemSum,
            currentEditingId,
            user.getUsername(),
            null,
            startPrice, // Giá khởi điểm mới
            AuctionStatus.PENDING,
            startT,
            endT,
            step        // Bước giá mới
        );

        // 👉 QUAN TRỌNG: Đưa itemData (chứa Artist, Brand...) vào gói tin
        // Biến itemData bạn đã khai báo và fill ở dòng 159-166 rồi đấy!
        updateData.setExtraData(itemData);
        submit.setDisable(true);
        submit.setText("Đang xử lý...");
        SocketClient.getInstance().setListener(this);

        if (this.currentEditingId == -1) {
            System.out.println("Gửi yêu cầu TẠO MỚI phiên đấu giá lên Server...");
            RequestHandler.getInstance().requestCreateAuction(updateData);
        } else {
                System.out.println("Gửi yêu cầu CẬP NHẬT phiên đấu giá lên Server...");
                RequestHandler.getInstance().requestUpdateAuction(updateData); 
        }

        } catch (Exception ex) {
            System.err.println("Lỗi khi đóng gói dữ liệu: " + ex.getMessage());
            // CHỈ mở khóa nếu code ở try bị lỗi (chưa kịp gửi lên Server)
            submit.setDisable(false);
            submit.setText(this.currentEditingId == -1 ? "Tạo sản phẩm" : "Cập nhật sản phẩm");
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
    private Label errorItemName, errorPrice, errorStepPrice, errorStartTime, errorEndTime;

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

        // 1. Đổ dữ liệu cơ bản
        itemName.setText(info.getItemInfo().getTitle());
        price.setText(String.valueOf(info.getCurPrice()));
        stepPrice.setText(String.valueOf(info.getBidStep()));
        itemDescription.setText(info.getItemInfo().getDescription());



        if (info.getStartTime() != null) {
            startTime.setValue(info.getStartTime().toLocalDate());
        }
        if (info.getEndTime() != null) {
            endTime.setValue(info.getEndTime().toLocalDate());
        }
        
        // 2. Set danh mục và ÉP form load ngay lập tức các ô nhập chi tiết
        type.setValue(info.getItemInfo().getItemType().name());
        changeItemInfo(); 

        // 3. Yêu cầu Server trả về thông tin chi tiết (Nghệ sĩ, Kích thước...)
        SocketClient.getInstance().setListener(this);
        RequestHandler.getInstance().requestFindItem(info.getItemInfo().getItemType(), info.getItemInfo().getItemId());

        // 4. Load ảnh cũ
        String imgPath = info.getItemInfo().getImagePath();
        if (imgPath != null && !imgPath.isEmpty()) {
            if (imgPath.startsWith("http")) preview1.setImage(new Image(imgPath, true));
            else preview1.setImage(new Image(getClass().getResourceAsStream(imgPath)));
        }

        // 5. KIỂM TRA LOGIC NGHIỆP VỤ (Khóa form)
        itemName.setDisable(false); 
        price.setDisable(false); 
        stepPrice.setDisable(false);
        itemDescription.setDisable(false); 
        itemInfo.setDisable(false); // Mở khóa khung chi tiết
        type.setDisable(true);



        if (info.getStatus() == AuctionStatus.OPEN) {
            boolean hasBids = (info.getLastBidderName() != null && !info.getLastBidderName().isEmpty()); 

            if (hasBids) {
                // ĐÃ CÓ NGƯỜI ĐẤU GIÁ: Khóa hết thông số tài chính & thuộc tính sản phẩm
                itemName.setDisable(true);
                price.setDisable(true);
                stepPrice.setDisable(true);
                type.setDisable(true);
                itemInfo.setDisable(true); // KHÓA TOÀN BỘ Ô NHẬP NGHỆ SĨ, KÍCH THƯỚC...



                price.setTooltip(new Tooltip("Đã có người đấu giá, chỉ được phép cập nhật thêm mô tả!"));
                submit.setText("Cập nhật mô tả");
            } else {
                price.setTooltip(new Tooltip("Chưa có ai đấu giá, bạn vẫn có thể sửa giá và thông tin."));
            }
        }
    }

    @Override
    public void onResponseReceived(String category, String action, String jsonData) {
        if ("AUCTION".equals(category) && ("CREATE".equals(action) || "UPDATE".equals(action))) {
            Platform.runLater(() -> {
                if (jsonData != null && !jsonData.contains("ERROR")) {
                    // TẠO THÀNH CÔNG: Chạy hiệu ứng chúc mừng
                    System.out.println("Tạo sản phẩm thành công, đang chuyển về Seller Hub...");
                    showCongratulationEffect(1.5);
                    
                    // Đợi 1.5s chạy xong GIF thì chuyển trang
                    PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
                    pause.setOnFinished(event -> {
                        try {
                            com.mikey.auction.javagui.SceneChanger.getInstance().toSellerHubGui(user);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    pause.play();
                } else {
                    // TẠO THẤT BẠI: Mở lại nút bấm để người dùng thử lại
                    System.err.println("Lỗi từ Server: " + jsonData);
                    submit.setDisable(false);
                    submit.setText(this.currentEditingId == -1 ? "Tạo sản phẩm" : "Cập nhật sản phẩm");
                }
            });
            return; // Thoát hàm để không chạy xuống phần bên dưới
        }

        if ("ITEM".equals(category) && "FIND".equals(action)) {
            Platform.runLater(() -> {
                try {
                    if (jsonData == null || jsonData.equals("null")) return;
                    JsonObject itemObj = JsonParser.parseString(jsonData).getAsJsonObject();
                    String itemType = type.getValue().toUpperCase();

                    // Mẹo: Dùng hàm getJsonStr để lấy an toàn không bị lỗi Null
                    if (itemType.equals("ARTS")) {
                        ((TextField) itemInfo.lookup("#artist")).setText(getJsonStr(itemObj, "artist"));
                        ((TextField) itemInfo.lookup("#yearOfcreation")).setText(getJsonStr(itemObj, "year", "yearOfCreation"));
                        ((TextField) itemInfo.lookup("#dimensions")).setText(getJsonStr(itemObj, "dimensions"));
                        ((TextField) itemInfo.lookup("#medium")).setText(getJsonStr(itemObj, "medium"));
                    } 
                    else if (itemType.equals("ELECTRONICS")) {
                        ((TextField) itemInfo.lookup("#brand")).setText(getJsonStr(itemObj, "brand"));
                        ((TextField) itemInfo.lookup("#power")).setText(getJsonStr(itemObj, "power"));
                        ((TextField) itemInfo.lookup("#voltage")).setText(getJsonStr(itemObj, "voltage"));
                        ((TextField) itemInfo.lookup("#current")).setText(getJsonStr(itemObj, "current"));
                        ((TextField) itemInfo.lookup("#status")).setText(getJsonStr(itemObj, "status"));
                        ((TextField) itemInfo.lookup("#color")).setText(getJsonStr(itemObj, "color"));
                        ((TextField) itemInfo.lookup("#weight")).setText(getJsonStr(itemObj, "weight"));
                    } 
                    else if (itemType.equals("VEHICLE")) {
                        ((TextField) itemInfo.lookup("#mileage")).setText(getJsonStr(itemObj, "mileage"));
                        ((TextField) itemInfo.lookup("#mFG")).setText(getJsonStr(itemObj, "mFG", "mfg"));
                        ((TextField) itemInfo.lookup("#brand")).setText(getJsonStr(itemObj, "brand"));
                        ((TextField) itemInfo.lookup("#model")).setText(getJsonStr(itemObj, "model"));
                        ((TextField) itemInfo.lookup("#trim")).setText(getJsonStr(itemObj, "trim"));
                        ((TextField) itemInfo.lookup("#titleStatus")).setText(getJsonStr(itemObj, "titleStatus"));
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi khi bóc tách chi tiết sản phẩm: " + e.getMessage());
                }
            });
        }
    }

    // Hàm hỗ trợ bóc tách JSON an toàn
    private String getJsonStr(JsonObject obj, String... possibleKeys) {
        for (String key : possibleKeys) {
            if (obj.has(key) && !obj.get(key).isJsonNull()) {
                return obj.get(key).getAsString();
            }
        }
        return "";
    }
}