package Items;
public class FactoryItem {

    public static Item createBuilder(ItemType type, String name, String desc, int sellerId, String imagePath) {
        switch (type.name()) {
            case ("ARTS"):
                return new Arts(name, desc, type, sellerId,-1, imagePath);
            case ("VEHICLE"):
                return new Vehicle(name, desc, type, sellerId,-1, imagePath); 
            case ("ELECTRONICS"):
                return new Electronics(name, desc, type, sellerId,-1, imagePath);          
            default:
                break;
        }
        throw new IllegalArgumentException("type không được hỗ trợ ");
    }
}
