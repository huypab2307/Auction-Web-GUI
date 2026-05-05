package com.mikey.auction.manager;


import com.mikey.auction.database.ArtsDAO;
import com.mikey.auction.database.ElectronicsDAO;
import com.mikey.auction.database.VehicleDAO;
import com.mikey.auction.items.*;
import com.mikey.auction.database.BaseDAO;
import com.mikey.auction.items.ItemType;
import com.mikey.auction.items.Item;
import java.sql.Connection;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

public class ItemManager {
    private static final ItemManager instance = new ItemManager();
    private ItemManager() {
    }
    public static ItemManager getInstance() {
        return instance;
    }

    public Item createItem(ItemType type, String name, String desc, int sellerId, String imagePath) {
        switch (type.name()) {
            case ("ARTS"):
                return new Arts(name, desc, type, sellerId, -1, imagePath);
            case ("VEHICLE"):
                return new Vehicle(name, desc, type, sellerId, -1, imagePath); 
            case ("ELECTRONICS"):
                return new Electronics(name, desc, type, sellerId, -1, imagePath);          
            default:
                break;
        }
        throw new IllegalArgumentException("type không được hỗ trợ ");
    }
    public Item findItemById(ItemType type, int id) throws SQLException{
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

    // public int createArts(Arts art) throws SQLException {
    //     ArtsDAO artsDAO = ArtsDAO.getInstance();
    //     try (Connection con = artsDAO.getConnect()) {
    //         return ArtsDAO.getInstance().createItem(con, art);
    //     } catch (SQLException e) {
    //         throw new SQLException("Không thể tạo arts: " + e.getMessage(), e);
    //     }
    // }

    // public int createVehicle(Vehicle vehicle) throws SQLException {
    //     VehicleDAO vehicleDAO = VehicleDAO.getInstance();
    //     try (Connection con = vehicleDAO.getConnect()) {
    //         return VehicleDAO.getInstance().createItem(con, vehicle);
    //     } catch (SQLException e) {
    //         throw new SQLException("Không thể tạo vehicle: " + e.getMessage(), e);
    //     }
    // }

    // public int createElectronics(Electronics electronics) throws SQLException {
    //     ElectronicsDAO electronicsDAO = ElectronicsDAO.getInstance();
    //     try (Connection con = electronicsDAO.getConnect()) {
    //         return ElectronicsDAO.getInstance().createItem(con, electronics);
    //     } catch (SQLException e) {
    //         throw new SQLException("Không thể tạo electronics: " + e.getMessage(), e);
    //     }
    // }

    public Item preProcessing(HashMap<String, String> itemData) {
        String name = itemData.get("name");
        String description = itemData.get("description");
        ItemType type = ItemType.valueOf(itemData.get("type").toUpperCase());
        int sellerId = Integer.parseInt(itemData.get("sellerId"));
        String imagePath = itemData.get("imagePath");
        Item item = createItem(type, name, description, sellerId, imagePath);

        switch (item.getType()) {
            case ARTS:
                Arts arts = (Arts) item;
                setArt(arts, itemData);
                return arts;

            case VEHICLE:
                Vehicle vehicle = (Vehicle) item;
                setVehicle(vehicle, itemData);
                return vehicle;

            case ELECTRONICS:
                Electronics electronics = (Electronics) item;
                setElectronics(electronics, itemData);
                return electronics;

            default:
                throw new IllegalArgumentException("Loại sản phẩm không hợp lệ");
        }
    }

    public void setArt(Item item, HashMap<String, String> itemData) {
        Arts arts = (Arts) item;
        arts.setArts(itemData.get("artist"), Integer.valueOf(itemData.get("year")) , itemData.get("medium"), itemData.get("dimensions"));
    }

    public void setVehicle(Item item, HashMap<String, String> itemData) {
        Vehicle vehicle = (Vehicle) item;
        vehicle.setVehicle(Double.valueOf(itemData.get("mileage")), Integer.valueOf(itemData.get("mFG")), itemData.get("brand"), itemData.get("model"), itemData.get("trim"), itemData.get("titleStatus"));
    }

    public void setElectronics(Item item, HashMap<String, String> itemData) {
        Electronics electronics = (Electronics) item;
        electronics.setElectronics(itemData.get("brand"), Integer.valueOf(itemData.get("power")), Double.valueOf(itemData.get("voltage")), Double.valueOf(itemData.get("current")), itemData.get("status"), itemData.get("color"), Double.valueOf(itemData.get("weight")));
    }

    public Item uploadItem(Connection connection, Item item) throws SQLException {
        try {
            int generatedId = -1;
            switch (item.getType()) {
                case ARTS:
                    generatedId = ArtsDAO.getInstance().createItem(connection, (Arts) item);
                    break;
                case VEHICLE:
                    generatedId = VehicleDAO.getInstance().createItem(connection, (Vehicle) item);
                    break;
                case ELECTRONICS:
                    generatedId = ElectronicsDAO.getInstance().createItem(connection, (Electronics) item);
                    break;
                default:
                    throw new IllegalArgumentException("Loại sản phẩm không hợp lệ");
            }
            item.setId(generatedId);
            return item;
        } catch (SQLException e) {
            throw new SQLException("Lỗi khi tải sản phẩm lên: " + e.getMessage(), e);
        }
    }
}
