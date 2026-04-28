package Items;

import Database.ArtsDAO;
import Database.ElectronicsDAO;
import Database.VehicleDAO;

import java.io.IOException;
import java.sql.SQLException;

public class FactoryItem {

    public static Item createBuilder(ItemType type, String name, String desc, int sellerId, String imagePath) {
        switch (type.name()) {
            case ("ARTS"):
                return new Arts(name, desc, type, -1,sellerId, imagePath);
            case ("VEHICLE"):
                return new Vehicle(name, desc, type, sellerId,-1, imagePath); 
            case ("ELECTRONICS"):
                return new Electronics(name, desc, type, sellerId,-1, imagePath);          
            default:
                break;
        }
        throw new IllegalArgumentException("type không được hỗ trợ ");
    }
    public static Item findItemById(ItemType type, int id) throws SQLException{
        switch (type.name()) {
            case ("ARTS"):
                return ArtsDAO.getInstance().findById(id);
            case ("VEHICLE"):
                return VehicleDAO.getInstance().findById(id);
            case ("ELECTRONICS"):
                return ElectronicsDAO.getInstance().findById(id);
        }
        return null;
    }
}
