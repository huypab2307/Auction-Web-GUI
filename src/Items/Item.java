package Items;
abstract class Item {
    private String name;
    private String description;
    private Double price;
    private String sellerId;
    private static int cur_id = 00000; 
    private String id; //auto-generated

    public Item(String name, String description, Double price,String type, String sellerId) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.id = type + cur_id++;
        this.sellerId = sellerId;
    }
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Double getPrice() {
        return price;
    }

    public String getSellerId() {
        return sellerId;
    }

    public static int getCur_id() {
        return cur_id;
    }

    public String getId() {
        return id;
    }
}
