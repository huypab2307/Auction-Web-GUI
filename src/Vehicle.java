public class Vehicle extends Item {
    private final double mileage; //Số km đã đi
    private final int mFG;  // Ngày sản xuất
    private final String brand; // nhà sản xuất
    private final String model; //dòng xe
    private final String trim; // phiên bản
    private final String titleStatus; //Tình trạng giấy tờ
    
    public Vehicle(Builder build) {
        super(build.name,build.description,build.price,"V",build.sellerId);
        this.mileage = build.mileage;
        this.mFG = build.mFG;
        this. brand = build.brand;
        this.model = build.model;
        this.trim = build.trim;
        this.titleStatus = build.titleStatus;
    }

    static class Builder {
        private double mileage;
        private int mFG;
        private String brand;
        private String model;
        private String trim;
        private String titleStatus;
        private String name;
        private String description;
        private double price;
        private String sellerId;

        public Builder(String name, String description, double price, String sellerId) {
            this.name = name;
            this.description = description;
            this.price = price;
            this.sellerId = sellerId; 
        }

        public Builder withMileage(double mileage) {
            this.mileage = mileage;
            return this;
        }

        public Builder withMFG(double mileage) {
            this.mFG = mFG;
            return this;
        }

        public Builder withBrand(double mileage) {
            this.brand = brand;
            return this;
        }

        public Builder withModel(double mileage) {
            this.model = model;
            return this;
        }

        public Builder withTrim(double mileage) {
            this.trim = trim;
            return this;
        }

        public Builder withTitleStatus(double mileage) {
            this.titleStatus = titleStatus;
            return this;
        }

        public Vehicle build() {
            return new Vehicle(this);
        }
    }

    // Mileage
    public double getMileage() {
        return this.mileage;
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
}    
