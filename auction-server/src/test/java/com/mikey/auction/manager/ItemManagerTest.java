package com.mikey.auction.manager;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.mikey.auction.items.Arts;
import com.mikey.auction.items.Electronics;
import com.mikey.auction.items.Item;
import com.mikey.auction.items.ItemType;
import com.mikey.auction.items.Vehicle;

public class ItemManagerTest {

    @Test
    public void testCreateItemArts() {
        // Kiểm tra tạo item loại Arts
        Item item = ItemManager.getInstance().createItem(ItemType.ARTS, "Mona Lisa", "Painting", 1, "path");

        assertNotNull(item, "Item không được null");
        assertEquals(ItemType.ARTS, item.getType(), "Loại item phải là ARTS");
        assertEquals("Mona Lisa", item.getName(), "Tên item phải đúng");
        assertEquals(1, item.getSellerId(), "Seller ID phải đúng");
    }

    @Test
    public void testCreateItemVehicle() {
        // Kiểm tra tạo item loại Vehicle
        Item item = ItemManager.getInstance().createItem(ItemType.VEHICLE, "Honda Civic", "Car", 2, "path");

        assertNotNull(item, "Item không được null");
        assertEquals(ItemType.VEHICLE, item.getType(), "Loại item phải là VEHICLE");
        assertEquals("Honda Civic", item.getName(), "Tên item phải đúng");
    }

    @Test
    public void testCreateItemElectronics() {
        // Kiểm tra tạo item loại Electronics
        Item item = ItemManager.getInstance().createItem(ItemType.ELECTRONICS, "Laptop", "Device", 3, "path");

        assertNotNull(item, "Item không được null");
        assertEquals(ItemType.ELECTRONICS, item.getType(), "Loại item phải là ELECTRONICS");
        assertEquals("Laptop", item.getName(), "Tên item phải đúng");
    }

    @Test
    public void testCreateItemInvalidType() {
        // Kiểm tra exception khi loại item không hợp lệ
        assertThrows(IllegalArgumentException.class, () -> {
            ItemManager.getInstance().createItem(null, "Test", "Desc", 1, "path");
        }, "Phải ném IllegalArgumentException cho loại item không hợp lệ");
    }

    @Test
    public void testPreprocessingArts() {
        // Kiểm tra tiền xử lý dữ liệu Arts từ HashMap
        HashMap<String, String> data = new HashMap<>();
        data.put("name", "Starry Night");
        data.put("description", "Van Gogh painting");
        data.put("type", "ARTS");
        data.put("sellerId", "1");
        data.put("imagePath", "/images/art");
        data.put("artist", "Vincent van Gogh");
        data.put("year", "1889");
        data.put("medium", "Oil on canvas");
        data.put("dimensions", "100x120");

        Item item = ItemManager.getInstance().preProcessing(data);

        assertNotNull(item, "Item không được null");
        assertEquals(ItemType.ARTS, item.getType(), "Loại item phải là ARTS");
        assertEquals("Starry Night", item.getName(), "Tên item phải đúng");
        assertInstanceOf(Arts.class, item, "Item phải là instance của Arts");
    }

    @Test
    public void testPreprocessingVehicle() {
        // Kiểm tra tiền xử lý dữ liệu Vehicle
        HashMap<String, String> data = new HashMap<>();
        data.put("name", "BMW 3 Series");
        data.put("description", "Luxury car");
        data.put("type", "VEHICLE");
        data.put("sellerId", "2");
        data.put("imagePath", "/images/car");
        data.put("mileage", "50000");
        data.put("mFG", "2020");
        data.put("brand", "BMW");
        data.put("model", "3 Series");
        data.put("trim", "Sport");
        data.put("titleStatus", "Clean");

        Item item = ItemManager.getInstance().preProcessing(data);

        assertNotNull(item, "Item không được null");
        assertEquals(ItemType.VEHICLE, item.getType(), "Loại item phải là VEHICLE");
        assertInstanceOf(Vehicle.class, item, "Item phải là instance của Vehicle");
    }

    @Test
    public void testPreprocessingElectronics() {
        // Kiểm tra tiền xử lý dữ liệu Electronics
        HashMap<String, String> data = new HashMap<>();
        data.put("name", "MacBook Pro");
        data.put("description", "Laptop");
        data.put("type", "ELECTRONICS");
        data.put("sellerId", "3");
        data.put("imagePath", "/images/laptop");
        data.put("brand", "Apple");
        data.put("power", "100");
        data.put("voltage", "220.0");
        data.put("current", "0.45");
        data.put("status", "New");
        data.put("color", "Space Gray");
        data.put("weight", "2.0");

        Item item = ItemManager.getInstance().preProcessing(data);

        assertNotNull(item, "Item không được null");
        assertEquals(ItemType.ELECTRONICS, item.getType(), "Loại item phải là ELECTRONICS");
        assertInstanceOf(Electronics.class, item, "Item phải là instance của Electronics");
    }

    @Test
    public void testFindItemById() {
        // Kiểm tra tìm item theo ID
        assertDoesNotThrow(() -> {
            Item item = ItemManager.getInstance().findItemById(ItemType.ELECTRONICS, 1);
            // Item có thể null nếu ID không tồn tại
        }, "Find item không được ném exception");
    }

    @Test
    public void testSetArt() {
        // Kiểm tra set thông tin Arts
        Item item = new Arts("Test", "Desc", ItemType.ARTS, 1, -1, "path");
        HashMap<String, String> data = new HashMap<>();
        data.put("artist", "Artist Name");
        data.put("year", "2023");
        data.put("medium", "Oil");
        data.put("dimensions", "50x50");

        assertDoesNotThrow(() -> {
            ItemManager.getInstance().setArt(item, data);
        }, "Set art info không được ném exception");
    }

    @Test
    public void testSetVehicle() {
        // Kiểm tra set thông tin Vehicle
        Item item = new Vehicle("Car", "Desc", ItemType.VEHICLE, 1, -1, "path");
        HashMap<String, String> data = new HashMap<>();
        data.put("mileage", "10000");
        data.put("mFG", "2022");
        data.put("brand", "Toyota");
        data.put("model", "Camry");
        data.put("trim", "LE");
        data.put("titleStatus", "Clean");

        assertDoesNotThrow(() -> {
            ItemManager.getInstance().setVehicle(item, data);
        }, "Set vehicle info không được ném exception");
    }

    @Test
    public void testSetElectronics() {
        // Kiểm tra set thông tin Electronics
        Item item = new Electronics("Phone", "Desc", ItemType.ELECTRONICS, 1, -1, "path");
        HashMap<String, String> data = new HashMap<>();
        data.put("brand", "Samsung");
        data.put("power", "25");
        data.put("voltage", "5.0");
        data.put("current", "2.0");
        data.put("status", "Used");
        data.put("color", "Black");
        data.put("weight", "0.2");

        assertDoesNotThrow(() -> {
            ItemManager.getInstance().setElectronics(item, data);
        }, "Set electronics info không được ném exception");
    }
}
