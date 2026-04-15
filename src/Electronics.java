public class Electronics extends Item {
    private final String brand;
    private final int power;
    private final double voltage;
    private final double current;
    private final String status;
    private final String color;
    private final double weight;


    public Electronics(Builder build) {
        super(build.name,build.description,build.price,"E",build.seller_id);
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
    static class Builder {
        private String name;
        private String description;
        private double price;
        private String seller_id;
        private String brand;
        private int power;
        private double voltage;
        private double current;
        private String status;
        private String color;
        private double weight;
        public Builder(String name, String description, double price, String seller_id){
            this.name = name;
            this.description = description;
            this.price = price;
            this.sellerId = sellerId;
        }
        public void withBrand (String brand){
            this.brand = brand;
        }
        public void withPower (int power){
            this.power = power;
        }
        public void withVoltage (double voltage){
            this.voltage = voltage;
        }
        public void withCurrent (double current){
            this.current = current;
        }
        public void withStatus(String status){
            this.status = status;
        }
        public void withColor (String color){
            this.color = color;
        }
        public void withWeight (double weight){
            this.weight = weight;
        }
        public Item build(){
            return new Electronics(this);
        }
    }
}
