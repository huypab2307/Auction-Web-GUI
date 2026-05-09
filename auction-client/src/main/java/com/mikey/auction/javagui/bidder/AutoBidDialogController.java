package com.mikey.auction.javagui.bidder;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AutoBidDialogController {
    @FXML
    private TextField maxPriceField;
    private double maxPrice = -1;
    private Stage stage;

    public void setStage(Stage stage) { this.stage = stage; }
    public double getMaxPrice() { return maxPrice; }

    @FXML
    private void handleConfirm() {
        try {
            maxPrice = Double.parseDouble(maxPriceField.getText().replace(",", ""));
            stage.close();
        } catch (NumberFormatException e) {
            maxPriceField.setStyle("-fx-border-color: red; -fx-background-radius: 10; -fx-border-radius: 10;");
        }
    }

    @FXML
    private void handleCancel() {
        maxPrice = -1;
        stage.close();
    }
}