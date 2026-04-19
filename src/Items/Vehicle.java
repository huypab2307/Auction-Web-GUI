package Items;
public class Vehicle extends Item {
    private final double mileage; //Số km đã đi
    private final int mFG;  // Ngày sản xuất
    private final String brand; // nhà sản xuất
    private final String model; //dòng xe
    private final String trim; // phiên bản
    private final String titleStatus; //Tình trạng giấy tờ
    
    public Vehicle(Builder build) {
        super(build.name,build.description,ItemType.VEHICLE,build.sellerId,-1);
        this.mileage = build.mileage;
        this.mFG = build.mFG;
        this. brand = build.brand;
        this.model = build.model;
        this.trim = build.trim;
        this.titleStatus = build.titleStatus;
    }

    public static class Builder {
        private double mileage;
        private int mFG;
        private String brand;
        private String model;
        private String trim;
        private String titleStatus;
        private String name;
        private String description;
        private int sellerId;

        public Builder(String name, String description, int sellerId) {
            this.name = name;
            this.description = description;
            this.sellerId = sellerId; 
        }

        public Builder withMileage(double mileage) {
            this.mileage = mileage;
            return this;
        }

        public Builder withMFG(int mFG) {
            this.mFG = mFG;
            return this;
        }

        public Builder withBrand(String brand) {
            this.brand = brand;
            return this;
        }

        public Builder withModel(String model) {
            this.model = model;
            return this;
        }

        public Builder withTrim(String trim) {
            this.trim = trim;
            return this;
        }

        public Builder withTitleStatus(String titleStatus) {
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
    public int getmFG() {
        return mFG;
    }
    @Override
    public String toString() {
        return "Vehicle {" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", trim='" + trim + '\'' +
                ", year=" + mFG +
                ", mileage=" + mileage + " km" +
                ", status='" + titleStatus + '\'' +
                ", seller='" + getSellerId() + '\'' +
                '}';
    }
}    
