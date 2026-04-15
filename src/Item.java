abstract class Item {
    private String name;
    private String description;
    private Double price;
    private String seller_id;
    private static int cur_id = 00000; 
    private String id; //auto-generated

    public Item(String name, String description, Double price,String type, String seller_id) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.id = type + cur_id++;
        this.seller_id = seller_id;
    }
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
