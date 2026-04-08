public class Vehicle extends Item {
    private double mileage; //Số km đã đi
    private int mFG;  // Ngày sản xuất
    private String autoMaker; // nhà sản xuất
    private String model; //dòng xe
    private String trim; // phiên bản
    public Vehicle(double mileage, int mFG, String autoMaker, String model, String trim,String name, String description, Double price) {
        super(name, description, price, "V");
        this.mileage = mileage;
        this.mFG = mFG;
        this.autoMaker = autoMaker;
        this.model = model;
        this.trim = trim;
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
    public String getAutoMaker() {
        return this.autoMaker;
    }
    public void setAutoMaker(String autoMaker) {
        this.autoMaker = autoMaker;
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

}
