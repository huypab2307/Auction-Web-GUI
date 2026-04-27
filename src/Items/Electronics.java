package Items;
public class Electronics extends Item {
    private final String brand;
    private final int power;
    private final double voltage;
    private final double current;
    private final String status;
    private final String color;
    private final double weight;


public Electronics(Builder build) {
    super(build.name,build.description,ItemType.ELECTRONICS,build.sellerId,-1,build.imagePath);
    this.brand = build.brand;
    this.power = build.power;
    this.voltage = build.voltage;
    this.current = build.current;
    this.status = build.status;
    this.color = build.color;
    this.weight = build.weight;
    }
    public String getBrand() {
        return brand;
    }

    public int getPower() {
        return power;
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
    public static class Builder {
        private String name;
        private String description;
        private int sellerId;
        private String brand;
        private int power;
        private double voltage;
        private double current;
        private String status;
        private String color;
        private double weight;
        private String imagePath;
        public Builder(String name, String description, int sellerId, String imagePath){
            this.name = name;
            this.imagePath = imagePath;
            this.description = description;
            this.sellerId = sellerId;
        }
        public Builder withBrand (String brand){
            this.brand = brand;
            return this;
        }
        public Builder withPower (int power){
            this.power = power;
            return this;
        }
        public Builder withVoltage (double voltage){
            this.voltage = voltage;
            return this;
        }
        public Builder withCurrent (double current){
            this.current = current;
            return this;
        }
        public Builder withStatus(String status){
            this.status = status;
            return this;
        }
        public Builder withColor (String color){
            this.color = color;
            return this;
        }
        public Builder withWeight (double weight){
            this.weight = weight;
            return this;
        }
        public Electronics build(){
            return new Electronics(this);
        }
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
