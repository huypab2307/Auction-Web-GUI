package Items;

import java.sql.Connection;
import java.sql.SQLException;

import Database.ArtsDAO;
import Database.AuctionDAO;
import Database.ElectronicsDAO;

public class Electronics extends Item {
    private String brand;
    private int power;
    private double voltage;
    private double current;
    private String status;
    private String color;
    private double weight;

    public Electronics(String name, String description, ItemType type, int sellerId, String imagePath) {
        super(name, description, type, sellerId, -1, imagePath);
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
    public boolean upload(Connection connection, double price, double stepPrice, int durations) throws SQLException {
        int id = ElectronicsDAO.getInstance().createItem(connection, this);
        return AuctionDAO.getInstance().createAuction(connection, id,this.getSellerId(), price, stepPrice, durations);
    }

    @Override
    public String toString() {
    return "Electronics {" +
            "id='" + getId() + '\'' +
            ", name='" + getName() + '\'' +
            ", brand='" + brand + '\'' +
            ", power=" + power + "W" +
            ", voltage=" + voltage + "V" +
            ", current=" + current + "A" +
            ", color='" + color + '\'' +
            ", weight=" + weight + "kg" +
            ", status='" + status + '\'' +
            ", seller='" + getSellerId() + '\'' +
            '}';
    }
}
