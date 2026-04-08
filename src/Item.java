abstract class Item {
    private String name;
    private String description;
    private Double price;
    private Double curPrice;
    private static int cur_id = 00000;
    private String id;

    public Item(String name, String description, Double price,String type) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.curPrice = null;
        this.id = type + cur_id++;
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
