public class Vehicle extends Item {
    private double mileage; //Số km đã đi
    private int mFG;  // Ngày sản xuất
    private String brand; // nhà sản xuất
    private String model; //dòng xe
    private String trim; // phiên bản
    private String titleStatus; //Tình trạng giấy tờ
    public Vehicle(double mileage, int mFG, String brand, String model, String trim,String titleStatus,String name, String description, Double price) {
        super(name, description, price, "V");
        this.mileage = mileage;
        this.mFG = mFG;
        this. brand = brand;
        this.model = model;
        this.trim = trim;
        this.titleStatus = titleStatus;
    }
    // Mileage
    public double getMileage() {
        return this.mileage;
    }
    public void setMileage(double mileage) {
        this.mileage = mileage;
    }
    // MFG
    public int getMFG() {
        return this.mFG;
    }
    public void setMFG(int mFG) {
        this.mFG = mFG;
    }
    // AutoMaker
    public String getBrand() {
        return this.brand;
    }
    public void setBrand(String brand) {
        this.brand = brand;
    }
    // model
    public String getModel() {
        return this.model;
    }
    public void setModel(String model) {
        this.model = model;
    }
    //trim
    public String getTrim() {
        return this.trim;
    }
    public void setTrim(String trim) {
        this.trim = trim;
    }
    // title
    public String getTitleStatus() {
        return this.titleStatus;
    }
    public void setTitleStatus(String titleStatus) {
        this.titleStatus = titleStatus;
    }
}    
