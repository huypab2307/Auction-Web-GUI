package Items;
public abstract class Item {
    private String name;
    private String description;
    private Double price;
    private int sellerId;
    private int id;
    private ItemType type;

    public Item(String name, String description, Double price,ItemType type, int sellerId,int id) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.sellerId = sellerId;
        this.type = type;
        this.id = id;
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

    public int getSellerId() {
        return sellerId;
    }

    public int getId() {
        return id;
    }
}
