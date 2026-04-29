package Items;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import Database.ArtsDAO;
import Database.AuctionDAO;
import Database.VehicleDAO;

public class Vehicle extends Item {
    private double mileage; //Số km đã đi
    private int mFG;  // Ngày sản xuất
    private String brand; // nhà sản xuất
    private String model; //dòng xe
    private String trim; // phiên bản
    private String titleStatus; //Tình trạng giấy tờ

    
    @Override
    public boolean upload(Connection connection, double price, double stepPrice, int durations) throws SQLException {
        int id = VehicleDAO.getInstance().createItem(connection, this);
        return AuctionDAO.getInstance().createAuction(connection, id, this.getSellerId(), price, stepPrice, durations);
    }

    public void setVehicle(double mileage, int mFG, String brand, String model, String trim, String titleStatus) {
        this.mileage = mileage;
        this.mFG = mFG;
        this.brand = brand;
        this.model = model;
        this.trim = trim;
        this.titleStatus = titleStatus;
    }

    // Mileage
    public double getMileage() {
        return this.mileage;
    }

    public Vehicle(String name, String description, ItemType type, int sellerId,int id, String imagePath) {
        super(name, description, type, sellerId, id, imagePath);
    }

    // MFG
    public int getMFG() {
        return this.mFG;
    }

    // AutoMaker
    public String getBrand() {
        return this.brand;
    }

    // model
    public String getModel() {
        return this.model;
    }

    //trim
    public String getTrim() {
        return this.trim;
    }

    // title
    public String getTitleStatus() {
        return this.titleStatus;
    }
    public int getmFG() {
        return mFG;
    }

    @Override
    public Map<String, String> getSpecificInfo() {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("Số km đã đi", mileage + " km");
        info.put("Năm sản xuất", String.valueOf(mFG));
        info.put("Hãng xe", brand);
        info.put("Dòng xe", model);
        info.put("Phiên bản", trim);
        info.put("Giấy tờ", titleStatus);
        return info;
    }
}    
