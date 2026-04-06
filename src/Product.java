public class Product {
    private String name;
    private String description;
    private Double price;
    private Double curPrice;

    public Product(String name, String description, Double price) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.curPrice = null;
    }
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Double getcurPrice() {
        return curPrice;
    }
    public void setcurPrice(double n_price){
        this.curPrice = n_price;
    }
    public void bid(double b_price){
        if (this.curPrice != null && this.curPrice > b_price && this.price > b_price){
            System.out.println("Invalid price input");
        }else{
            System.out.println("Bid successfully");
            this.setcurPrice(b_price);
            System.out.println("Current price: " + curPrice);
        }
    }
}
