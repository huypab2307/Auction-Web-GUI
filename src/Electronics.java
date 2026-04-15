public class Electronics extends Item {
    private String brand;
    private int power;
    private double voltage;
    private double current;
    private String status;
    private String color;
    private double weight;


    public Electronics(String name, String description, double price, String brand, int power, double voltage, double current, String status, String color, double weight,String sellerId) {
        super(name,description,price,"E",sellerId);
        this.brand = brand;
        this.power = power;
        this.voltage = voltage;
        this.current = current;
        this.status = status;
        this.color = color;
        this.weight = weight;
    }
    public Electronics(String name, String description, Double price, String sellerId) {
        super(name, description, price,"E", sellerId);
    }
    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public double getVoltage() {
        return voltage;
    }

    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }

    public double getCurrent() {
        return current;
    }

    public void setCurrent(double current) {
        this.current = current;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
