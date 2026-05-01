package com.mikey.auction.items;



import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;


public class Electronics extends Item {
    private String brand;
    private int power;
    private double voltage;
    private double current;
    private String status;
    private String color;
    private double weight;

    public Electronics(String name, String description, ItemType type, int sellerId,int id, String imagePath) {
        super(name, description, type, sellerId, id, imagePath);
    }

    public void setElectronics( String brand, int power, double voltage, double current, String status, String color, double weight) {
        this.brand = brand;
        this.power = power;
        this.voltage = voltage;
        this.current = current;
        this.status = status;
        this.color = color;
        this.weight = weight;
    }


    public int getPower() {
        return power;
    }
    public String getBrand() {
        return brand;
    }

    public double getVoltage() {
        return voltage;
    }

    public double getCurrent() {
        return current;
    }

    public String getStatus() {
        return status;
    }

    public String getColor() {
        return color;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public Map<String, String> getSpecificInfo() {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("Thương hiệu", brand);
        info.put("Công suất", power + " W");
        info.put("Điện áp", voltage + " V");
        info.put("Dòng điện", current + " A");
        info.put("Tình trạng", status);
        info.put("Màu sắc", color);
        info.put("Trọng lượng", weight + " kg");
        return info;
    }
}
