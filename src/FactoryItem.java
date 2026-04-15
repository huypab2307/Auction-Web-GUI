class FactoryItem {
    public static Item CreateItem(String type, String name, String description, Double price, String seller_id){
        Item item;
        switch (type.toLowerCase()) {
            case "arts":
                item = new Arts(name,description,price, seller_id);
                return item;
            case "electronics":
                item = new Electronics(name,description,price, seller_id);
                return item;
            case "vehicle":
                item = new Vehicle(name,description,price,seller_id);
                return item;
            default:
                throw new AssertionError();
        }
    }
}
